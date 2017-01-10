package org.kite9.diagram.visualization.planarization.rhd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.PlanarizationBuilder;
import org.kite9.diagram.visualization.planarization.grid.GridPositioner;
import org.kite9.diagram.visualization.planarization.mapping.CornerVertices;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.GeneratorBasedGroupingStrategyImpl;
import org.kite9.diagram.visualization.planarization.rhd.layout.DirectionLayoutStrategy;
import org.kite9.diagram.visualization.planarization.rhd.layout.LayoutStrategy;
import org.kite9.diagram.visualization.planarization.rhd.layout.MostNetworkedFirstLayoutQueue;
import org.kite9.diagram.visualization.planarization.rhd.links.BasicContradictionHandler;
import org.kite9.diagram.visualization.planarization.rhd.links.ConnectionManager;
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler;
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos;
import org.kite9.diagram.visualization.planarization.rhd.position.VertexPositioner;
import org.kite9.diagram.visualization.planarization.rhd.position.VertexPositionerImpl;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Rob's Hierarchical Decomposition Planarization Builder is a 4-phase process:
 * <ul>
 * <li>grouping
 * <li>layout
 * <li>positioning 
 * <li>vertex ordering
 * </ul>
 * 
 * The grouping phase divides the orderables at each level into hierarchical groups
 * based on how many connections they have with other groups.
 * 
 * The layout phase takes a group and works out whether the two merge groups
 * within it should be placed horizontally or vertically within their container.
 * This means there are 4^c options for this merge where c is the number of
 * containers with parts of both groups.
 * 
 * Positioning is the process of assigning x/y positions to the leaf groups and containers.
 * 
 * Finally, the results of this are used to create the list of vertices to
 * return, with their positions set by the layout strategy.  We also add vertices for any container corners at this stage.
 * 
 * @author moffatr
 * 
 */
public abstract class RHDPlanarizationBuilder implements PlanarizationBuilder, Logable, Comparator<DiagramElement> {


	private Kite9Log log = new Kite9Log(this);

	protected ElementMapper em;
	protected GridPositioner gridHelp;
	protected RoutableHandler2D rh;
	private VertexPositioner vp;
	
	public RHDPlanarizationBuilder(ElementMapper em, GridPositioner gridHelp) {
		super();
		this.em = em;
		this.gridHelp = gridHelp;
	}
	
	public RoutableReader getRoutableReader() {
		return rh;
	}

	protected abstract Planarization buildPlanarization(Diagram c, List<Vertex> vertexOrder, Collection<BiDirectional<Connected>> initialUninsertedConnections, Map<Container, List<DiagramElement>> sortedContainerContents);

	static enum PlanarizationRun { FIRST, REDO, DONE }
	
	public Planarization planarize(Diagram c) {
		final int[] elements = new int[1];

		new DiagramElementVisitor().visit(c, new VisitorAction() {

			@Override
			public void visit(DiagramElement de) {
				if (de instanceof Connected) {
					elements[0]++;
				}
			}
		});
		PlanarizationRun run = PlanarizationRun.FIRST;
		List<Vertex> out = new ArrayList<Vertex>(elements[0] * 2);
		ConnectionManager connections = null;
		Map<Container, List<DiagramElement>> sortedContainerContents = null;
		try {
			
			while (run != PlanarizationRun.DONE) {
				rh =  new PositionRoutableHandler2D();
				vp = new VertexPositionerImpl(em, rh, this);
				ContradictionHandler ch = new BasicContradictionHandler(em);
				GroupingStrategy strategy = new GeneratorBasedGroupingStrategyImpl(ch);
				
				// Grouping
				GroupPhase gp = new GroupPhase(log, c, elements[0], strategy, ch, gridHelp, em);
				GroupResult mr = strategy.group(gp);
				
				if (!log.go()) {
					log.send("Created Groups:", mr.groups());
				}

				if (mr.groups().size() > 1) {
					throw new LogicException("Should end up with a single group");
				} 
				
				Group topGroup = mr.groups().iterator().next();
					
				if (!log.go()) {
					outputGroupInfo(topGroup, 0);
				}
				
				// Layout
				LayoutStrategy layout = new DirectionLayoutStrategy(rh);
				layout.layout(gp, mr, new MostNetworkedFirstLayoutQueue(gp.groupCount));
				
				// positioning
				connections = new RankBasedConnectionQueue(rh);
				buildPositionMap(topGroup, connections);
				if (!log.go()) {
					outputGroupInfo(topGroup, 0);
				}
				
				if (connections.hasContradictions()) {
					if (!checkLayoutIsConsistent(c)) {
						if (run == PlanarizationRun.FIRST) {
							log.send("Contradiction forces regroup");
							run = PlanarizationRun.REDO;
							continue;
						}
					}
				}
					
				// vertex ordering
				sortedContainerContents = new HashMap<Container, List<DiagramElement>>(gp.containerCount * 2);
				instantiateContainerVertices(c);
				buildVertexList(null, c, null, out, sortedContainerContents);
				sortContents(out, rh.getTopLevelBounds(true), rh.getTopLevelBounds(false));
				run = PlanarizationRun.DONE;
			}
		} finally {
			if (!log.go()) {
				TestingEngine.drawPositions(out, RHDPlanarization.class, "positions", "vertex.png");
			}
		}

		Planarization planOut = buildPlanarization(c, out, connections, sortedContainerContents);
		((RHDPlanarizationImpl)planOut).setRoutableReader(rh);
		
		return planOut;
	}

	/**
	 * This makes sure all the container vertices have the correct anchors before we position them.
	 */
	private void instantiateContainerVertices(DiagramElement c) {
		if (em.requiresPlanarizationCornerVertices(c)) {
			em.getOuterCornerVertices(c);
			
			if (c instanceof Container) {
				for (DiagramElement de : ((Container)c).getContents()) {
					instantiateContainerVertices(de);
				}
			}
		}
	}

	/**
	 * Potentially expensive, but checks to make sure that none of the positions overlap.
	 */
	private boolean checkLayoutIsConsistent(Container c) {
		List<DiagramElement> contents = c.getContents();
		for (int i = 0; i < contents.size(); i++) {
			DiagramElement ci = contents.get(i);
			for (int j = 0; j < i; j++) {
				DiagramElement cj = contents.get(j);
				if (overlaps(ci, cj)) {
					log.error("Overlap in positions of: "+ci+"  "+cj);
					return false;
				}
			}
			
			if (em.requiresPlanarizationCornerVertices(ci)) {
				if (!checkLayoutIsConsistent((Container) ci)) {
					return false;
				}
			}
		}		
		
		return true;
	}

	private boolean overlaps(DiagramElement a, DiagramElement b) {
		RoutingInfo ria = rh.getPlacedPosition(a);
		RoutingInfo rib = rh.getPlacedPosition(b);
		return (rh.overlaps(ria, rib));
	}


	public void outputGroupInfo(Group g, int spc) {
		StringBuilder sb = new StringBuilder(spc);
		for (int i = 0; i < spc; i++) {
			sb.append(" ");
		}
		DirectedGroupAxis axis = DirectedGroupAxis.getType(g);
		Layout l = g.getLayout();
		log.send(sb.toString() + g.getGroupNumber() +
				" "+ ((g instanceof LeafGroup) ? g.toString() : axis) 
				+ "   " + rh.getPlacedPosition(g)+"  "+l+" "+(g.getAxis().isLayoutRequired() ? "LR " : " ")
				+ ((g instanceof CompoundGroup) ? (((CompoundGroup)g).getA().groupNumber)+" "+(((CompoundGroup)g).getB().groupNumber) : "" ));
		
		if (g instanceof CompoundGroup) {		
//			if ((l==Layout.UP) || (l==Layout.LEFT)) {
//				outputGroupInfo(((CompoundGroup) g).getB(), spc+1);
//				outputGroupInfo(((CompoundGroup) g).getA(), spc+1);
//			} else {
				outputGroupInfo(((CompoundGroup) g).getA(), spc+1);
				outputGroupInfo(((CompoundGroup) g).getB(), spc+1);
//			}
		}
	}	
	
	
	public void explain(Group g, int spc) {
		StringBuilder sb = new StringBuilder(spc);
		for (int i = 0; i < spc; i++) {
			sb.append(" ");
		}
		DirectedGroupAxis axis = DirectedGroupAxis.getType(g);
		log.send(sb.toString() + g.getGroupNumber() + "   " + rh.getPlacedPosition(g));

		Group hParent = axis.horizParentGroup;
		Group vParent = axis.vertParentGroup;
		if (vParent != hParent) {
			if (hParent != null) {
				log.send(sb.toString()
						+ "horiz: "
						+ ((g == ((CompoundGroup) hParent).getB()) ? hParent.getLayout() : Layout.reverse(hParent
								.getLayout())));
				explain(hParent, spc + 1);
			}
			if (vParent != null) {
				log.send(sb.toString()
						+ "vert: "
						+ ((g == ((CompoundGroup) vParent).getB()) ? vParent.getLayout() : Layout.reverse(vParent
								.getLayout())));
				explain(vParent, spc + 1);
			}

		} else {
			if (vParent != null) {
				log.send(sb.toString()
						+ "pos: "
						+ ((g == ((CompoundGroup) vParent).getB()) ? vParent.getLayout() : Layout.reverse(vParent
								.getLayout())));
				explain(hParent, spc + 1);
			}
		}

	}	
	
	private void buildPositionMap(Group start, ConnectionManager connections) {
		if (start instanceof CompoundGroup) {
			CompoundGroup cg = (CompoundGroup) start;
			log.send("Processing Group: "+start.getGroupNumber());
			buildPositionMap(cg.getA(), connections);
			buildPositionMap(cg.getB(), connections);
			connections.handleLinks(cg);
		} else {
			LeafGroup lg = (LeafGroup) start;
			log.send("Processing Group: " + lg);
			// g is a leaf group.  can we place it?
			Connected l = lg.getContained();
			Container c = lg.getContainer();
			
			// sizing
			RoutingInfo ri = lg.getAxis().getPosition(rh, false);
			vp.checkMinimumGridSizes(ri);
			if (l != null) {
				rh.setPlacedPosition(l, ri);
			}
			ensureContainerBoundsAreLargeEnough(ri, c, lg);
		}	
	}
	
	private void ensureContainerBoundsAreLargeEnough(RoutingInfo ri, Container c, LeafGroup lg) {
		Connected l;
		while (c != null) {
			// make sure container bounds are big enough for the contents
			RoutingInfo cri = rh.getPlacedPosition(c);
			if (cri == null) {
				cri = rh.emptyBounds();
			}

			RoutingInfo cri2 = rh.increaseBounds(cri, ri);
			if (!cri2.equals(cri)) {
				log.send("Increased bounds of "+c+" to "+cri2+" due to "+lg);
			}
			rh.setPlacedPosition(c, cri2);
			l = (Connected) c;
			c = l.getContainer();
		}
	}

	/**
	 * Constructs the list of vertices in no particular order.
	 */
	private void buildVertexList(Connected before, DiagramElement c, Connected after, List<Vertex> out, Map<Container, List<DiagramElement>> sortedContainerContents) {
		if (em.hasOuterCornerVertices(c)) {
			CornerVertices cvs = em.getOuterCornerVertices(c);
			RoutingInfo bounds = rh.getPlacedPosition(c);
			log.send("Placed position of container: "+c+" is "+bounds);
			vp.setPerimeterVertexPositions(before, c, after, cvs, out);

			if (c instanceof Container) {
				Container container = (Container) c;
				if (container.getContents().size() > 0) {
					buildVertexListForContainerContents(out, container, sortedContainerContents);
				}
			}

		} else {
			vp.setCentralVertexPosition(c, out);
		}
		return;
	}

	public static final boolean CHANGE_CONTAINER_ORDER = true;

	private void buildVertexListForContainerContents(List<Vertex> out, Container container, Map<Container, List<DiagramElement>> sortedContainerContents) {
		boolean layingOut = container.getLayout() != null;
		List<DiagramElement> contents = container.getContents();
		
		if (layingOut) {
			// sort the contents so that we can connect the right elements together
			contents = getContainerContentsHolder(container.getLayout(), contents);
			
			Collections.sort(contents, new Comparator<DiagramElement>() {
				@Override
				public int compare(DiagramElement arg0, DiagramElement arg1) {
					return compareDiagramElements(arg0, arg1);
				}
			});
			
			sortedContainerContents.put(container, contents);
		} 
		
		Connected conBefore = null, current = null, conAfter = null;
		boolean start =true;
		Iterator<DiagramElement> iterator = contents.iterator();
		while (start || (current != null)) {
			conBefore = current;
			current = conAfter;
			conAfter = getNextConnected(iterator);
			if (current != null) {
				buildVertexList(conBefore, current, conAfter, out, sortedContainerContents);	
				start = false;
			}
		}
	}

	private Connected getNextConnected(Iterator<DiagramElement> iterator) {
		while (iterator.hasNext()) {
			DiagramElement de= iterator.next();
			if (de instanceof Connected) {
				return (Connected) de;
			}
		}
		
		return null;
	}

	private List<DiagramElement> getContainerContentsHolder(Layout ld, List<DiagramElement> contents) {
//		if (CHANGE_CONTAINER_ORDER) {
//			if ((ld == Layout.HORIZONTAL) || (ld == Layout.VERTICAL)) {
//				// we are going to modify the original diagram
//				return contents;
//			}
//		} 
		
		// leave original intact
		return new ArrayList<DiagramElement>(contents);
	} 
	
	/**
	 * This implements a kind of quad sort, where we look at each vertex and ascribe it to TL, TR, BL, BR
	 * quadrants.  We do that over and over until there's only a single vertex in each quadrant.
	 */
	private void sortContents(List<Vertex> in, Bounds x, Bounds y) {
		if (in.size() > 1) {
			log.send("Sorting: "+in);
			Bounds left = rh.narrow(Layout.LEFT, x, true, false); 
			Bounds right =rh.narrow(Layout.RIGHT, x, true, false);
			Bounds top =rh.narrow(Layout.UP, y, false, false);
			Bounds bottom = rh.narrow(Layout.DOWN, y, false, false);
			
			List<Vertex> topRight = new ArrayList<Vertex>(in.size()/2);
			List<Vertex> topLeft = new ArrayList<Vertex>(in.size()/2);
			List<Vertex> bottomRight = new ArrayList<Vertex>(in.size()/2);
			List<Vertex> bottomLeft = new ArrayList<Vertex>(in.size()/2);
			
			boolean mergeTopAndBottom = false;
			boolean mergeLeftAndRight = false;
			
			for (Vertex vertex : in) {
				RoutingInfo vri = vertex.getRoutingInfo();
				Bounds vx = rh.getBoundsOf(vri, true);
				Bounds vy = rh.getBoundsOf(vri, false);
				boolean inLeft = rh.compareBounds(vx, left) == DPos.OVERLAP;
				boolean inRight = rh.compareBounds(vx, right) == DPos.OVERLAP;
				boolean inTop = rh.compareBounds(vy, top) == DPos.OVERLAP;
				boolean inBottom = rh.compareBounds(vy, bottom) == DPos.OVERLAP;
				
				if (inTop && inBottom) {
					mergeTopAndBottom = true;
				}
				
				if (inLeft && inRight) {
					mergeLeftAndRight = true;
				}
				
				if ((!inTop && !inBottom) || (!inLeft && !inRight)) {
					throw new LogicException("Vertex not within either bounds"+vri+" "+vertex);
				}
				
				if (inLeft) {
					if (inTop) {
						topLeft.add(vertex);
					} else {
						bottomLeft.add(vertex);
					}  
				} else {
					if (inTop) {
						topRight.add(vertex);
					} else {
						bottomRight.add(vertex);
					}  
				}
			}
			
			if (mergeLeftAndRight && mergeTopAndBottom) {
				// we should have a single vertex, so you need to sort the old fashioned way
				sortContentsOldStyle(in);
			} else if (mergeLeftAndRight) {
				in.clear();
				topRight.addAll(topLeft);
				bottomRight.addAll(bottomLeft);
				sortContents(topRight, x, top);
				sortContents(bottomRight, x, bottom);
				in.addAll(topRight);
				in.addAll(bottomRight);
			} else if (mergeTopAndBottom) {
				in.clear();
				topRight.addAll(bottomRight);
				topLeft.addAll(bottomLeft);
				sortContents(topRight, right, y);
				sortContents(topLeft, left, y);
				in.addAll(topLeft);
				in.addAll(topRight);
			} else {
				in.clear();
				sortContents(topLeft, left, top);
				sortContents(topRight, right, top);
				sortContents(bottomLeft, left, bottom);
				sortContents(bottomRight, right, bottom);
				in.addAll(topLeft);
				in.addAll(topRight);
				in.addAll(bottomLeft);
				in.addAll(bottomRight);
			}
		
			
		}
		
		
	}

	
	/**
	 * Simple Y,X sort, where Y has priority.
	 * 
	 * This is insufficient on it's own, because we end up with situations where in order to "get around" one large vertex we
	 * move out-of-position with respect to another vertex.  Because of the difference in vertex sizes, you basically
	 * can't just sort on middle position and hope things are in the right order.
	 */
	private void sortContentsOldStyle(List<Vertex> c) {
		Collections.sort(c, new Comparator<Vertex>() {

			@Override
			public int compare(Vertex arg0, Vertex arg1) {
				RoutingInfo ri0 = arg0.getRoutingInfo();
				RoutingInfo ri1 = arg1.getRoutingInfo();
				int out = 0;

				DPos yc = rh.compare(ri0, ri1, false);
				if (yc == DPos.BEFORE) {
					out = -1;
				} else if (yc == DPos.AFTER) {
					out = 1;
				}

				if (out == 0) {
					DPos xc = rh.compare(ri0, ri1, true);
					if (xc == DPos.BEFORE) {
						out = -1;
					} else if (xc == DPos.AFTER) {
						out = 1;
					}
				}

				if (out == 0) {
					//return 0;
					throw new LogicException("Contents overlap: "+arg0+" "+arg1);
				}

				//System.out.println("Comparing: " + arg0 + " " + arg1 + " " + out);
				return out;
			}
		});
	}

	@Override
	public String getPrefix() {
		return "GRPW";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	

	/**
	 * Returns the sort order for the two elements according to their groupwise position
	 * within a container.
	 */
	protected int compareDiagramElements(DiagramElement a, DiagramElement b) {
		DiagramElement parent =a.getParent();
		if (b.getParent() != parent) {
			parent = getCommonContainer(a, b);
		}
		
		Layout l = ((Container) parent).getLayout();
		
		if (l == null) {
			return 0;
		} else {
			DPos dp = null;
			switch (l) {
				case UP:
				case DOWN:
				case VERTICAL:
					dp  = rh.compare(a, b, false);
					break;
				case LEFT:
				case RIGHT:
				case HORIZONTAL:
					dp = rh.compare(a, b, true);
					break;
				case GRID:
					dp = rh.compare(a, b, false);
					dp = (dp == DPos.OVERLAP) ? rh.compare(a, b, true) : dp;
			}
			
			if (dp == DPos.BEFORE) {
				return -1;
			} else if (dp == DPos.AFTER) {
				return 1; 
			} else {
				return 0;
			//	throw new LogicException("Elements within a container shouldn't overlap");
			}
		}
		
	}
	
	protected Container getCommonContainer(DiagramElement from, DiagramElement to) {
		while (from != to) {
			int depthFrom = em.getContainerDepth(from);
			int depthTo = em.getContainerDepth(to);
			if (depthFrom < depthTo) {
				to = to.getParent();
			} else if (depthFrom > depthTo) {
				from = from.getParent();
			} else {
				to = to.getParent();
				from = from.getParent();
			}
		}
		
		return (Container) from;
	}

	@Override
	public int compare(DiagramElement o1, DiagramElement o2) {
		return compareDiagramElements(o1, o2);
	}
	
	
}
