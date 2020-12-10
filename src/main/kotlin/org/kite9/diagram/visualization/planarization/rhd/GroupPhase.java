package org.kite9.diagram.visualization.planarization.rhd;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.det.Deterministic;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.hints.PositioningHints;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;
import org.kite9.diagram.visualization.planarization.rhd.links.OrderingTemporaryBiDirectional;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.kite9.diagram.logging.LogicException;

/**
 * A GroupPhase is responsible for creating the Group data structures out of the
 * vertices, and holding various related lookup maps. 
 * 
 * @author robmoffat
 * 
 */
public class GroupPhase {

	public static final float LINK_WEIGHT = 1;

	public Set<LeafGroup> allGroups;
	
	private Random hashCodeGenerator;
	
	public static Kite9Log log = new Kite9Log(new Logable() {
		
		@Override
		public boolean isLoggingEnabled() {
			return true;
		}
		
		@Override
		public String getPrefix() {
			return "GP  ";
		}
	});
	
	private Map<Connected, LeafGroup> pMap;
	private GroupBuilder ab;
	private Set<Connection> allLinks = new UnorderedSet<Connection>(1000);
	private ContradictionHandler ch;
	private GridPositioner gp;
	private ElementMapper em;
	
	public GroupPhase(Kite9Log log, DiagramElement top, int elements, GroupBuilder ab, ContradictionHandler ch, GridPositioner gp, ElementMapper em) {
		this.pMap = new LinkedHashMap<Connected, LeafGroup>(elements * 2);
		allGroups = new LinkedHashSet<LeafGroup>(elements * 2);
		this.ab = ab;
		this.ch = ch;
		this.gp = gp;
		this.em = em;
		this.hashCodeGenerator = new Random(elements);
		
		createLeafGroup((Connected) top, null, pMap);
		setupLinks(top);
		
		for (LeafGroup group : allGroups) {
			group.log(log);
		}
	}
	
	public LeafGroup getLeafGroupFor(Connected ord) {
		return pMap.get(ord);
	}

	/**
	 * Creates leaf groups and any ordering between them, recursively.
	 * @param ord  The object to create the group for
	 * @param prev1 Previous element in the container
	 * @param pMap Map of Connected to LeafGroups (created)
	 */
	private LeafGroup createLeafGroup(Connected ord, Connected prev1, Map<Connected, LeafGroup> pMap) {
		if (pMap.get(ord) != null) {
			throw new LogicException("Diagram Element " + ord + " appears multiple times in the diagram definition");
		}

		Container cnr = ord.getContainer();

		boolean leaf = needsLeafGroup(ord);
		LeafGroup g = null;

		if (leaf) {
			g = new LeafGroup(ord, cnr, ab.createAxis(), ab.createLinkManager());
			pMap.put(ord, g);
			allGroups.add(g);
		}

		if ((prev1 != null) && (prev1 != ord)) {
			addContainerOrderingInfo(ord, prev1, cnr, null);
		}

		if (!leaf) {
			Layout l = ((Container) ord).getLayout();

			if (l == Layout.GRID) {
				// need to iterate in 2d
				DiagramElement[][] grid = gp.placeOnGrid((Container) ord, false);

				// create unconnected groups
				Map<DiagramElement, LeafGroup> gridGroups = new LinkedHashMap<>();
				for (int y = 0; y < grid.length; y++) {
					for (int x = 0; x < grid[0].length; x++) {
						DiagramElement de = grid[y][x];
						if (!gridGroups.containsKey(de)) {
							LeafGroup gg = createLeafGroup((Connected) de, null, pMap);
							if (gg == null) {
								gg = getConnectionEnd((Connected) de);
							}
							gridGroups.put(de, gg);
						}
					}
				}

				// link them up
				for (int y = 0; y < grid.length; y++) {
					for (int x = 0; x < grid[0].length; x++) {
						Connected prevy = (Connected) (y > 0 ? grid[y - 1][x] : null);
						Connected prevx = (Connected) (x > 0 ? grid[y][x - 1] : null);
						Connected c = (Connected) grid[y][x];

						if ((c != prevx) && (prevx != null)) {
							OrderingTemporaryBiDirectional tc = new OrderingTemporaryBiDirectional(prevx, c, Direction.RIGHT, cnr);
							LeafGroup from = gridGroups.get(prevx);
							LeafGroup to = gridGroups.get(c);
							
							from.sortLink(Direction.RIGHT, to, LINK_WEIGHT, true, Integer.MAX_VALUE, single(tc));
							to.sortLink(Direction.LEFT, from, LINK_WEIGHT, true, Integer.MAX_VALUE, single(tc));
						}

						if ((c != prevy) && (prevy != null)) {
							OrderingTemporaryBiDirectional tc = new OrderingTemporaryBiDirectional(prevy, c, Direction.DOWN, cnr);
							LeafGroup from = gridGroups.get(prevy);
							LeafGroup to = gridGroups.get(c);
							
							from.sortLink(Direction.DOWN, to, LINK_WEIGHT, true, Integer.MAX_VALUE, single(tc));
							to.sortLink(Direction.UP, from, LINK_WEIGHT, true, Integer.MAX_VALUE, single(tc));
						}
					}
				}
			} else {
				Connected prev = null;
				for (DiagramElement c : ((Container) ord).getContents()) {
					if (c instanceof Connected) {
						createLeafGroup((Connected) c, prev, pMap);
						prev = (Connected) c;
					}
				}
			}
		}

		return g;

	}
	

	private void setupLinks(DiagramElement o) {
		if (o instanceof Connected) {
			for (Connection c : ((Connected) o).getLinks()) {
				if (!allLinks.contains(c)) {
					allLinks.add(c);
					ch.checkForContainerContradiction(c);
					
					if (Tools.isConnectionRendered(c)) {
						LeafGroup to = getConnectionEnd(c.otherEnd((Connected) o));
						LeafGroup from = getConnectionEnd((Connected) o);
						
						if ((to != null) && (to != o)) {
							Direction d = c.getDrawDirectionFrom((Connected) o);
							if (Tools.isConnectionContradicting(c)) {
								d = null;
							}
							
							boolean ordering = false; ///c instanceof OrderingTemporaryBiDirectional;
							from.sortLink(d, to, LINK_WEIGHT, ordering, getLinkRank(c), single(c));
							to.sortLink(Direction.reverse(d), from, LINK_WEIGHT, ordering, getLinkRank(c), single(c));
						}
					}
				}
			}		
		}
		
		if (o instanceof Container) {
			for (DiagramElement o2 : ((Container)o).getContents()) {
				setupLinks(o2);
			}
		}
	}
	
	private int getLinkRank(Connection c) {
//		if (c instanceof OrderingTemporaryBiDirectional) {
//			return Integer.MAX_VALUE;
//		} else

		if (c.getDrawDirection() != null) {
			return c.getRank();
		} else {
			return 0;
		}
	}
	
	public static boolean isHorizontalDirection(Direction drawDirection) {
		return (drawDirection==Direction.LEFT) || (drawDirection==Direction.RIGHT);
	}

	public static boolean isVerticalDirection(Direction drawDirection) {
		return (drawDirection==Direction.UP) || (drawDirection==Direction.DOWN);
	}

	private boolean needsLeafGroup(Connected ord) {
		if ((ord instanceof Diagram) && (!hasConnectedContents((Diagram)ord))) {
			// we need at least one group in the GroupPhase, so if the diagram is empty, return a
			// single leaf group.
			return true;
		}
		
		return !em.requiresPlanarizationCornerVertices(ord);
	}
	
	private boolean hasConnectedContents(Diagram d) {
		for (DiagramElement de : d.getContents()) {
			if (de instanceof Connected) {
				return true;
			}
		}
		
		return false;
	}

	private static final Set<Layout> TEMPORARY_NEEDED = EnumSet.of(Layout.LEFT, Layout.RIGHT, Layout.UP, Layout.DOWN);
	
	private void addContainerOrderingInfo(Connected current, Connected prev, Container cnr, Dimension gridDimension) {
		if (prev == null)
			return;


		Layout l = cnr.getLayout();
		Direction d = null;
		
		if (TEMPORARY_NEEDED.contains(l)) {
			d = getDirectionForLayout(l);
		} else if (gridDimension != null) {
			d = gridDimension == Dimension.H ? Direction.RIGHT : Direction.DOWN;
		}
		
		if (d != null) {
			OrderingTemporaryBiDirectional tc = new OrderingTemporaryBiDirectional(prev, current, d, cnr);
			LeafGroup from = getConnectionEnd(prev);
			LeafGroup to = getConnectionEnd(current);
			
			from.sortLink(d, to, LINK_WEIGHT, true, Integer.MAX_VALUE, single(tc));
			to.sortLink(Direction.reverse(d), from, LINK_WEIGHT, true, Integer.MAX_VALUE, single(tc));
		
		}
	}

	public int groupCount = 0;
	public int containerCount = 0;	
	
	public abstract class Group implements Deterministic {
		
		protected Group(GroupAxis axis, LinkManager lm) {
			this.axis = axis;
			this.axis.setGroup(this);
			this.lm = lm;
			this.lm.setGroup(this);
		}
		
		private String leafList;
		
		/**
		 * Returns the leaf group number (or numbers for compound group) 
		 * composing this group.
		 */
		public String getLeafList() {
			if (leafList == null) {
				TreeSet<Integer> gs = new TreeSet<Integer>();
				addLeafGroupNumbersToSet(gs);
				this.leafList = gs.toString();
			}
			
			return leafList;
		}
		
		public abstract void addLeafGroupNumbersToSet(Set<Integer> s);

		protected Layout layout;
		protected int ordinal;
		
		private GroupAxis axis = null;
		private LinkManager lm = null;
		
		protected int hashCode;
		
		/**
		 * Live means that the group is ready to merge at the moment.
		 */
		private boolean live;
		
		public boolean isLive() {
			return live;
		}

		public void setLive(boolean live) {
			this.live = live;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		public GroupAxis getAxis() {
			return axis;
		}
		
		public boolean isActive() {
			return (this.axis==null) || (this.axis.isActive());
		}
		
		public GroupAxis getType() {
			return axis;
		}

		protected int size = 0;
		
		public void setSize(int size) {
			this.size = size;
		}

		public int getSize() {
			return size;
		}
		
		public void processAllLeavingLinks(boolean compound, int mask, LinkProcessor lp) {
			lm.processAllLeavingLinks(compound, mask, lp);
		}

		public LinkDetail getLink(Group g) {
			return lm.get(g);
		}
		
		public LinkManager getLinkManager() {
			return lm;
		}
				
		public abstract void processLowestLevelLinks(LinkProcessor lp);
				
		/**
		 * Returns true if this group is lg, or it contains it somehow in the hierarchy
		 */
		public abstract boolean contains(Group lg);
		
		public int getGroupOrdinal() {
			return ordinal;
		}
		
		/**
		 * Returns the number of nested levels below this group
		 */
		public abstract int getGroupHeight();
		
		int groupNumber = (groupCount ++)+1;
		
		public int getGroupNumber() {
			return groupNumber;
		}
		
		public void log(Kite9Log log) {
			log.send("Group: "+this);
			log.send("  Links:", getLinkManager().forLogging());
		}
		
		public Layout getLayout() {
			return layout;
		}
		
		public void setLayout(Layout l) {
			this.layout = l;
		}
				
		public abstract Map<String, Float> getHints();
 	}
	
	/**
	 * Represents the relative positions of two other groups within the diagram, allowing the immediate contents 
	 * of any container to be expressed as a binary tree.
	 */
	public class CompoundGroup extends Group {

		private Group a, b;
		private int height;
		private LinkDetail internalLinkA;
		private LinkDetail internalLinkB;
		private Map<String, Float> hints;
		private boolean treatAsLeaf;

		public LinkDetail getInternalLinkA() {
			return internalLinkA;
		}
		
		public LinkDetail getInternalLinkB() {
			return internalLinkB;
		}


		public Group getA() {
			return a;
		}

		public Group getB() {
			return b;
		}

		public CompoundGroup(Group a, Group b, GroupAxis axis, LinkManager lm, boolean treatAsLeaf) {
			super(axis, lm);
			
			this.a = a;
			this.b = b;
			this.size = a.size + b.size;
			this.height = Math.max(a.getGroupHeight(), b.getGroupHeight()) +1;
			this.ordinal = Math.min(a.getGroupOrdinal(), b.getGroupOrdinal());
			this.treatAsLeaf = treatAsLeaf;
			if (!treatAsLeaf) {
				// this is done so that a different compound group containing the same leaves can
				// occupy the same position in a hashmap
				this.hashCode = a.hashCode() + b.hashCode();
			} else {
				this.hashCode = hashCodeGenerator.nextInt();
			}
						
			fileLinks(a, b);
			fileLinks(b, a); // internals kept from one side to avoid duplication
			hints = PositioningHints.merge(a.getHints(), b.getHints());
		}
		
		@Override
		public void addLeafGroupNumbersToSet(Set<Integer> s) {
			if (treatAsLeaf) {
				s.add(this.getGroupNumber());
			} else {
				a.addLeafGroupNumbersToSet(s);
				b.addLeafGroupNumbersToSet(s);
			}
		}

		/**
		 * This will process {@link OrderingTemporaryBiDirectional}s first, so that if there is
		 * a contradiction in the links, it will occur on one of the Link - Connections.
		 */
		private void fileLinks(final Group linksGroup, final Group toGroup) {
			linksGroup.processAllLeavingLinks(true, getLinkManager().allMask(), new LinkProcessor() {
				
				@Override
				public void process(Group notUsed, Group g, LinkDetail ld) {
					fileLink(linksGroup, g, toGroup, ld);
				}
			});
		}

		private void fileLink(Group from, Group to, Group merging, LinkDetail ld) {
			boolean internal = merging.contains(to);

			if (!internal) {
				getLinkManager().sortLink(ld);
			} else {
				if (merging == to) {
					if (from == a) {
						log.send("Setting internal A:"+ld+" "+to);
						internalLinkA = ld;
					} else {
						log.send("Setting internal B:"+ld+" "+to);
						internalLinkB = ld;
					}
				}
			}
		}

		@Override
		public String toString() {
			return "[" +getGroupNumber() + a + "," + b + ":"+getAxis()+"]";
		}

		

		@Override
		public boolean contains(Group lg) {
			if (this == lg) {
				return true;
			}
			
			/**
			 * Obviously, we can't contain bigger groups than ourselves.
			 */
			if (lg.getSize() >= this.getSize()) {
				return false;
			}
			
			/**
			 * Also, can't create later-created groups than this.
			 */
			if (lg.getGroupNumber() > this.getGroupNumber()) {
				return false;
			}
			
			return a.contains(lg) || b.contains(lg);
		}


		@Override
		public void processLowestLevelLinks(LinkProcessor lp) {
			a.processLowestLevelLinks(lp);
			b.processLowestLevelLinks(lp);
		}

		@Override
		public int getGroupHeight() {
			return height;
		}

		@Override
		public Map<String, Float> getHints() {
			return hints;
		}
	}

	/**
	 * Represents a single vertex (glyph, context) within the diagram
	 */
	public class LeafGroup extends Group {		
		Connected o;
		Container c;
		
		public Connected getContained() {
			return o;
		}
		
		public Container getContainer() {
			return c;
		}

		public LeafGroup(Connected o, Container parent, GroupAxis axis, LinkManager lm) {
			super(axis, lm);
			this.o = o;
			this.c = parent;
			
			// layout is the container layout at this level
			this.layout = c != null ? c.getLayout() : null;
			if (o instanceof Container) {
				containerCount++;
			}
			this.ordinal = getGroupNumber();
			this.size = 1;
			this.hashCode = hashCodeGenerator.nextInt();
		}
			
		@Override
		public String toString() {
			return "["+getGroupNumber()+ o + "("+(c instanceof Diagram ? "" :" c: "+c)+","+getAxis()+")]";
		}
	
		@Override
		public boolean contains(Group lg) {
			return (this == lg);
		}

		@Override
		public void processLowestLevelLinks(LinkProcessor lp) {
			processAllLeavingLinks(false, getLinkManager().allMask(), lp);
		}

		@Override
		public int getGroupHeight() {
			return 0;
		}
		
		public void sortLink(Direction d, Group otherGroup, float linkValue, boolean ordering, int linkRank, Iterable<BiDirectional<Connected>> c) {
			getLinkManager().sortLink(d, otherGroup, linkValue, ordering, linkRank, c);
		}
		
		public Map<String, Float> getHints() {
//			Map<String, Float> positioningHints = o != null ? o.getPositioningHints() : null;
//			if (positioningHints!= null){
//				return positioningHints;
//			}
			
			return Collections.emptyMap();
		}

		@Override
		public void addLeafGroupNumbersToSet(Set<Integer> s) {
			s.add(this.getGroupNumber());
		}
	}

	private Set<BiDirectional<Connected>> single(BiDirectional<Connected> c) {
		return Collections.singleton(c);
	}

	public static Layout getLayoutForDirection(Direction currentDirection) {
		if (currentDirection == null)
			return null;
		
		switch (currentDirection) {
		case RIGHT:
			return Layout.RIGHT;
		case LEFT:
			return Layout.LEFT;
		case DOWN:
			return Layout.DOWN;
		case UP:
			return Layout.UP;
		default:
			return null;
		}
	}
	
	public static Direction getDirectionForLayout(Layout currentDirection) {
		switch (currentDirection) {
		case RIGHT:
			return Direction.RIGHT;
		case LEFT:
			return Direction.LEFT;
		case DOWN:
			return Direction.DOWN;
		case UP:
			return Direction.UP;
		case VERTICAL:
		case HORIZONTAL:
		default:
			throw new LogicException("Wasn't expecting direction: " + currentDirection);

		}
	}
	
	
	
	
	
	/**
	 * This has to handle decomposition 
	 */
	private LeafGroup getConnectionEnd(Connected oe) {
		LeafGroup otherGroup = pMap.get(oe);
		if (otherGroup == null) {
			LeafGroup decomp = new LeafGroup(null, (Container) oe, ab.createAxis(), ab.createLinkManager());
			allGroups.add(decomp);
			return decomp;
		} else {
			return otherGroup;
		}
	}

}