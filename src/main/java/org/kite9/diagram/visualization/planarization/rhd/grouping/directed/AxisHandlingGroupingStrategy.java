package org.kite9.diagram.visualization.planarization.rhd.grouping.directed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.primitives.Container;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult.ContainerStateInfo;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.MergeOption;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState.GroupContainerState;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;


/**
 * For directed merges, this strategy allows groups to be created which run x-axis-first 
 * merging and y-axis-first merging, and then once directed merges are exhausted for both,
 * combines them together.
 * 
 * 
 * @author robmoffat
 * 
 */
public abstract class AxisHandlingGroupingStrategy extends AbstractRuleBasedGroupingStrategy {

	
	protected BasicMergeState ms;
	
	public AxisHandlingGroupingStrategy(BasicMergeState ms) {
		super();
		if (debug_write_file) {
			File f = new File("merges.txt");
			f.delete();
		}
		this.ms = ms;
	}

	static Kite9Log hack = new Kite9Log(new Logable() {

		@Override
		public boolean isLoggingEnabled() {
			return true;
		}

		@Override
		public String getPrefix() {
			return "DGA ";
		}
	});

	static boolean debug_write_file = !hack.go();

	@Override
	protected void groupChangedContainer(BasicMergeState ms, final Group g) {
		g.getLinkManager().notifyContainerChange();
		
		g.processAllLeavingLinks(true, g.getLinkManager().allMask(), new LinkProcessor() {
			
			@Override
			public void process(Group originatingGroup, Group destinationGroup, LinkDetail ld) {
				destinationGroup.getLinkManager().notifyContainerChange(g);
			}
		});
	}

	protected void initLeafGroupAxis(LeafGroup toAdd) {
	}

	@Override
	public DirectedGroupAxis createAxis() {
		return new DirectedGroupAxis(hack);
	}
	
	@Override
	public LinkManager createLinkManager() {
		return new DirectedLinkManager(ms);
	}

	protected CompoundGroup createCompoundGroup(GroupPhase gp, BasicMergeState ms, MergeOption mo) {
		DirectedGroupAxis used = buildCompoundAxis(mo.mk.a, mo.mk.b, mo.alignedDirection);
		LinkManager lm = createLinkManager();
		CompoundGroup out = gp.new CompoundGroup(mo.mk.a, mo.mk.b, used, lm, false);
		identifyGroupDirection(out, gp,
				ms);
		log.send(log.go() ? null : "Compound Group " + out.getGroupNumber() + " created: \n\t" + out.getA() + "\n\t"
				+ out.getB() + "\n\t" + out.getLayout() + "\n\taxis:" + out.getAxis() + "\n\tlinks:", ((DirectedLinkManager)lm).links);
		writeGroup(out, mo);
		return out;
	}
	
	private void checkForInternalContradictions(final CompoundGroup out, LinkDetail ld1, LinkDetail ld2, final BasicMergeState ms) {
		if ((ld1==null) && (ld2==null)) {
			return;
		}
		
		// added for contradictions - break the least important one first.
		LinkDetail ld = ld1.getLinkRank() > ld2.getLinkRank() ? ld2 : ld1; 

		// check that internals don't contradict the axis of the group
		if (isLinkAgainstAxis(out, ld)) {
			Map<Container, GroupContainerState> aContainerMap = ms.getContainersFor(out.getA());
			Map<Container, GroupContainerState> bContainerMap = ms.getContainersFor(out.getB());
			final Set<Container> expandingContainers = new HashSet<Container>(aContainerMap.keySet());
			expandingContainers.retainAll(bContainerMap.keySet());
			
			LinkProcessor axisChecker = new LinkProcessor() {
				
				/**
				 * The aim of this test is to make sure we don't allow a container to be non-square
				 */
				@Override
				public void process(Group originatingGroup, Group destinationGroup, LinkDetail ld) {
					if (isLinkAgainstAxis(out, ld)) {
						Container from = ((LeafGroup)originatingGroup).getContainer();
						Container to = ((LeafGroup)destinationGroup).getContainer();
						
						Container fromExpanded = getFirstExpandingContainer(ms, from);
						Container toExpanded = getFirstExpandingContainer(ms, to);
						
						if ((fromExpanded!=null) && (!isParentOrSelf(to, fromExpanded))) {
							// from has been expanded, from outside itself
							ms.getContradictionHandler().setContradicting(ld.getConnections());
							return;
						}
						
						if ((toExpanded!=null) && (!isParentOrSelf(from, toExpanded))) {
							// from has been expanded, from outside itself
							ms.getContradictionHandler().setContradicting(ld.getConnections());
							return;
						}
						
					}
				}

				private boolean isParentOrSelf(Container x, Container parent) {
					if (x==parent) {
						return true;
					} else if (x == null) {
						return false;
					} else {
						return isParentOrSelf(((Contained)x).getContainer(), parent);
					}
				}

				private Container getFirstExpandingContainer(BasicMergeState ms, Container from) {
					if (expandingContainers.contains(from)) {
						return from;
					} else if (from instanceof Contained) {
						return getFirstExpandingContainer(ms, ((Contained)from).getContainer());
					} else {
						return null;
					}
				}
			};
			
			ld1.processLowestLevel(axisChecker);
		}
		
	}

	private boolean isLinkAgainstAxis(CompoundGroup out, LinkDetail ld) {
		return ((DirectedGroupAxis.getState(out)==MergePlane.X_FIRST_MERGE) && (GroupPhase.isVerticalDirection(ld.getDirection()))) ||
		 ((DirectedGroupAxis.getState(out)==MergePlane.Y_FIRST_MERGE) && (GroupPhase.isHorizontalDirection(ld.getDirection())));
	}

	public void identifyGroupDirection(CompoundGroup out, GroupPhase gp, BasicMergeState ms) {
		Container c = getCommonContainer(out);
		Layout layoutDirection = getAxisLayoutForContainer(c);

		LinkDetail lda = out.getInternalLinkA();
		LinkDetail ldb = out.getInternalLinkB();
		
		checkForInternalContradictions(out, out.getInternalLinkA(), out.getInternalLinkB(), ms);
		
		if ((lda != null) && (ldb != null)) {
			Layout layout = GroupPhase.getLayoutForDirection(
					ms.getContradictionHandler().checkContradiction(
							lda.getDirection(), lda.isOrderingLink(), lda.getLinkRank(), lda.getConnections(), 
							Direction.reverse(ldb.getDirection()), ldb.isOrderingLink(), ldb.getLinkRank(), ldb.getConnections(), 
							layoutDirection ));
			out.setLayout(layout);
		} else if (lda != null) {
			out.setLayout(GroupPhase.getLayoutForDirection(lda.getDirection()));
		} else if (ldb != null){
			out.setLayout(GroupPhase.getLayoutForDirection(ldb.getDirection()));
		} 
		
		
		// we may be able to establish a layout from one or more of the 
		// containers that the groups are in.  Layout will be horizontal or vertical,
		// unless there is a contradiction.
		boolean layoutNeeded = c != null;
		
		if (!layoutNeeded && (out.getLayout() == null)) {
			DirectedGroupAxis.getType(out).setLayoutRequired(false);
		} else if (out.getLayout() == null) {
			out.setLayout(layoutDirection);
		} else if ((out.getLayout() == Layout.UP) || (out.getLayout() == Layout.DOWN)) {
			if (layoutDirection == Layout.HORIZONTAL) {
				out.setLayout(Layout.HORIZONTAL);
			}
		} else if ((out.getLayout() == Layout.LEFT) || (out.getLayout() == Layout.RIGHT)) {
			if (layoutDirection == Layout.VERTICAL) {
				out.setLayout(Layout.VERTICAL);
			}
		} else if (out.getLayout() != layoutDirection) {
			throw new LogicException("Layout contradiction");
		}
		
	}
	
	private Layout getAxisLayoutForContainer(Container c) {
		if (c==null)
			return null;
		Layout layoutDirection = c.getLayoutDirection();
		
		// sanitize to a single axis
		if (layoutDirection != null) {
			switch (layoutDirection) {
			case LEFT:
			case RIGHT:
				layoutDirection = Layout.HORIZONTAL;
				break;
			case UP:
			case DOWN:
				layoutDirection  = Layout.VERTICAL;
				break;
			default:
			}
		}
		
		return layoutDirection;
	}

	/**
	 * Attempts to find a container shared by both a and b, in which both a and b actually have contents.  
	 * If there is no common content, then it returns false.  
	 */
	private Container getCommonContainer(CompoundGroup out) {
		Map<Container, GroupContainerState> a2cs = ms.getContainersFor(out.getA());
		Map<Container, GroupContainerState> commonContainers = ms.getContainersFor(out.getB());
		
		Container common = null;

		for (Entry<Container, GroupContainerState> containerEntry : a2cs.entrySet()) {
			if ((containerEntry.getValue().hasContent()) && (ms.isContainerLive(containerEntry.getKey()))) {
				Container container = containerEntry.getKey();
				GroupContainerState bContained = commonContainers.get(container);
				if ((bContained != null) && (bContained.hasContent())) {
	
					if ((common == null) || (common.getLayoutDirection() == null)) {
						common = container;
					}
				}
			}
		}
		
		return common;
	}
	
	private void writeGroup(CompoundGroup g, MergeOption mo) {
		if (debug_write_file) {
			try {
				File out = new File("merges.txt");
				Writer os = new FileWriter(out, true);
				os.write(g.getGroupNumber()+"\t"+g.getA()+"\t"+g.getB()+"\t"+g.getAxis()+"\t"+mo.getPriority()+"\n");
				os.close();
			} catch (IOException e) {
				throw new LogicException("Couldn't write file");
			}
		}
	}

	
	/**
	 * Once all the directed merges have been completed for a group, (say X_FIRST) it should 
	 * be merged with the group going the other way (Y_FIRST) if possible.
	 */
	protected void checkForCompleteAxisMerges(GroupPhase gp, BasicMergeState ms, Group a) {
		DirectedMergeState dms = (DirectedMergeState)ms;
		if (dms.completedDirectionalMerge(a)) {
			Group b = dms.getCompoundGroupWithSameContents(a);
			
			if ((b != null) && (b!=a)) {
				// perform the merge in a fairly normal way.
				DirectedGroupAxis type = createAxis();
				type.setHorizontal(false);
				type.setVertical(false);
				type.state = MergePlane.UNKNOWN;
				LinkManager lm = createLinkManager();
				CompoundGroup out = gp.new CompoundGroup(a, b, type, lm, true);
				log.send(log.go() ? null : "Compound Group " + out.getGroupNumber() + " created: \n\t" + out.getA() + "\n\t"
						+ out.getB() + "\n\t" +" NON-LAYOUT "+ "\n\taxis:" + out.getAxis() + "\n\tlinks:", ((DirectedLinkManager)lm).links);

				out.setSize(a.getSize());
				setBothParents(out, (DirectedGroupAxis) a.getAxis());
				setBothParents(out, (DirectedGroupAxis) b.getAxis());

				doCompoundGroupInsertion(gp, ms, out, true);
			}
		}
	}

	@Override
	protected void doCompoundGroupInsertion(GroupPhase gp, BasicMergeState ms, CompoundGroup combined, boolean skipContainerCompletionCheck) {
		super.doCompoundGroupInsertion(gp, ms, combined, skipContainerCompletionCheck);
		if (!skipContainerCompletionCheck) {
			checkForCompleteAxisMerges(gp, ms, combined);
		}
	}

	private DirectedGroupAxis buildCompoundAxis(Group a, Group b, Direction alignedDirection) {
		DirectedGroupAxis used = createAxis();
		MergePlane axis = DirectedGroupAxis.getMergePlane(a, b);
		
		if (axis == MergePlane.UNKNOWN) {
			if (alignedDirection != null) {
				switch (alignedDirection) {
				case UP:
				case DOWN:
					axis = MergePlane.Y_FIRST_MERGE;
					break;
				case LEFT:
				case RIGHT:
					axis = MergePlane.X_FIRST_MERGE;
					break;
				}
				
			}
		}

		if (axis == null) {
			throw new LogicException("Illegal merge");
		}

		switch (axis) {
		case X_FIRST_MERGE:
			used.state = MergePlane.X_FIRST_MERGE;
			used.setHorizontal(false);
			used.setVertical(true);
			break;
		case Y_FIRST_MERGE:
			used.state = MergePlane.Y_FIRST_MERGE;
			used.setHorizontal(true);
			used.setVertical(false);
			break;
		case UNKNOWN:
			used.state = MergePlane.UNKNOWN;
			used.setVertical(true);
			used.setHorizontal(true);
		}
		return used;
	}

	/**
	 * When a merge option creates a horizontal or vertical combined group, then
	 * the underlying groups are not removed necessarily - they are kept around
	 * so that they can be merged in the other direction.
	 */
	@Override
	protected void removeOldGroups(GroupPhase gp, BasicMergeState ms, CompoundGroup combined) {
		checkRemoveGroup(gp, combined.getA(), combined, ms);
		checkRemoveGroup(gp, combined.getB(), combined, ms);
	}

	private void checkRemoveGroup(GroupPhase gp, Group a, CompoundGroup cg, BasicMergeState ms) {
		DirectedGroupAxis aType = DirectedGroupAxis.getType(a);
		DirectedGroupAxis cgType = DirectedGroupAxis.getType(cg);

		if (cgType.state == MergePlane.X_FIRST_MERGE) {
			aType.vertParentGroup = cg;
			if ((aType.state == MergePlane.X_FIRST_MERGE) || (aType.state == MergePlane.Y_FIRST_MERGE)) {
				aType.active = false;
				if (aType.horizParentGroup == null) {
					aType.horizParentGroup = cg;
				}
			} else {
				aType.state = MergePlane.Y_FIRST_MERGE;
				axisChanged(a);
			}
		} else if (cgType.state == MergePlane.Y_FIRST_MERGE) {
			aType.horizParentGroup = cg;
			if ((aType.state == MergePlane.X_FIRST_MERGE) || (aType.state == MergePlane.Y_FIRST_MERGE)) {
				aType.active = false;
				if (aType.vertParentGroup == null) {
					aType.vertParentGroup = cg;
				}
			} else {
				aType.state = MergePlane.X_FIRST_MERGE;
				axisChanged(a);
			}
		} else {
			// undirected / single axis merge
			setBothParents(cg, aType);
		}

		if (aType.active == false) {
			ms.removeLiveGroup(a);
		} else {
			// group is being kept, so we need to make sure it's in the group
			// lists
			ms.addLiveGroup(a);
		}

	}

	private void setBothParents(CompoundGroup cg, DirectedGroupAxis aType) {
		aType.horizParentGroup = cg;
		aType.vertParentGroup = cg;
		aType.active = false;
	}

	private void axisChanged(Group a) {
		a.getLinkManager().notifyAxisChange();
	}

	@Override
	protected boolean isContainerCompleteInner(Container c, BasicMergeState ms) {
		ContainerStateInfo csi = ms.getStateFor(c);
		
		if (csi.contents.size()<2) {
			csi.done = true;
			return true;
		}
		
		if (csi.contents.size() > 2) {
			return false;
		}
		
		// test one of each axis
		Iterator<Group> groups = csi.contents.iterator();
		Group first = groups.next();
		Group second = groups.next();
		
		MergePlane fax = DirectedGroupAxis.getState(first);
		MergePlane sax = DirectedGroupAxis.getState(second);
		
		boolean done = (((fax==MergePlane.X_FIRST_MERGE) && (sax==MergePlane.Y_FIRST_MERGE)) ||
		((fax==MergePlane.Y_FIRST_MERGE) && (sax==MergePlane.X_FIRST_MERGE)));
		
		return done;
	}


}