package org.kite9.diagram.visualization.planarization.rhd.grouping.directed;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState.GroupContainerState;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager;
import org.kite9.diagram.logging.LogicException;

public class DirectedLinkManager implements LinkManager {
	
	private BasicMergeState ms;
	
	public DirectedLinkManager(BasicMergeState ms) {
		super();
		this.ms = ms;
	}

	public static final int MASK_X_FIRST = 1;
	public static final int MASK_Y_FIRST = 2;

	public static final int MASK_NEAREST_NEIGHBOUR = 4;
	public static final int MASK_IN_CONTAINER = 8;
	
	@Override
	public void setGroup(Group g) {
		this.g = g;
		links = new LinkedHashMap<Group, AbstractLinkDetail>();
	}

	public static final int MASK_UP = 16;
	public static final int MASK_DOWN = 32;
	public static final int MASK_LEFT = 64;
	public static final int MASK_RIGHT = 128;
	public static final int MASK_NO_DIRECTION = 256;
	
	protected LinkedHashMap<Group, AbstractLinkDetail> links;
	private Group g;
	
	private Set<Direction> yFirstNearestNeighbours = EnumSet.noneOf(Direction.class);
	private Set<Direction> xFirstNearestNeighbours = EnumSet.noneOf(Direction.class);
	
	public static int createMask(MergePlane mp, boolean nearestNeighbour, boolean inContainer, Direction... d) {
		int outMask = 0;
		if ((mp == MergePlane.X_FIRST_MERGE) || (mp==MergePlane.UNKNOWN) || (mp == null)) {
			outMask += MASK_X_FIRST;			
		}
		
		if ((mp == MergePlane.Y_FIRST_MERGE) || (mp==MergePlane.UNKNOWN) || (mp == null)) {
			outMask += MASK_Y_FIRST;			
		}

		if (nearestNeighbour) {
			outMask += MASK_NEAREST_NEIGHBOUR;
		}
		
		if (inContainer) {
			outMask += MASK_IN_CONTAINER;
		}
		
		for (Direction direction : d) {
			if (direction==null) {
				outMask += MASK_NO_DIRECTION;
			} else {
				switch (direction) {
				case UP:
					outMask += MASK_UP;
					break;
				case DOWN:
					outMask += MASK_DOWN;
					break;
				case LEFT:
					outMask += MASK_LEFT;
					break;
				case RIGHT:
					outMask += MASK_RIGHT;
					break;
				}
			}
		}
		
		return outMask; 
	}
	
	protected static abstract class AbstractLinkDetail implements LinkDetail {
	
		private Direction d;
		protected boolean inContainer;
		protected boolean nearestNeighbour;
		
		@Override
		public boolean from(Group b) {
			return object == b;
		}

		private Group subject;
		private Group object;
		
		@Override
		public void processToLevel(LinkProcessor lp, int l) {
			lp.process(getOriginatingGroup(), getGroup(), this);
		}
		
		@Override
		public void processLowestLevel(LinkProcessor lp) {
			processToLevel(lp, Integer.MAX_VALUE);
		}

		public AbstractLinkDetail(Direction d, Group subject, Group object) {
			super();
			this.d = d;
			this.subject = subject;
			this.object = object;
		}

		@Override
		public Direction getDirection() {
			return d;
		}

		@Override
		public Group getGroup() {
			return subject;
		}
		
		public Group getOriginatingGroup() {
			return object;
		}
		
		public String toString() {
			return getNumberOfLinks()+"/d="+getDirection()+"/"+(isOrderingLink() ? "O" : "~O" )+"/"+(inContainer ? "C" : "~C")+"/"+(nearestNeighbour ? "N" : "~N")+"/" +getGroup().getGroupNumber();
		}
		
	}
	
	protected static class ActualLinkDetail extends AbstractLinkDetail {
		
		private int linkRank = 0;
		private boolean ordering = false;
		private float numberOfLinks;
		
		private List<BiDirectional<Connected>> connections = new LinkedList<BiDirectional<Connected>>();
		
		public ActualLinkDetail(ActualLinkDetail toCopy, Group lmsGroup) {
			this(toCopy.getGroup(), lmsGroup, toCopy.numberOfLinks, toCopy.getDirection(), toCopy.ordering, toCopy.linkRank, toCopy.connections);
			this.inContainer = toCopy.inContainer;
			this.nearestNeighbour= toCopy.nearestNeighbour;
		}
		
		public ActualLinkDetail(Group g, Group o, float numberOfLinks, Direction d, boolean isOrdering, int linkRank, Iterable<BiDirectional<Connected>> c) {
			super(d, g, o);
			this.numberOfLinks = numberOfLinks;
			this.ordering = isOrdering;
			this.linkRank = linkRank;
			addConnections(c);
		}
		
		private void addConnections(Iterable<BiDirectional<Connected>> c) {
			for (BiDirectional<Connected> item : c) {
				if (!getConnections().contains(c)) {
					getConnections().add(item);
				}	
			}
		}
	
		@Override
		public boolean isOrderingLink() {
			return ordering;
		}
	
		@Override
		public float getNumberOfLinks() {
			return numberOfLinks;
		}
	
		public List<BiDirectional<Connected>> getConnections() {
			return connections;
		}

		@Override
		public int getLinkRank() {
			return linkRank;
		}
	}

	private boolean keepLinkTo(Group otherGroup) {
		MergePlane mp = DirectedGroupAxis.getMergePlane(this.g, otherGroup);
		return mp != null;
	}

	public static final AbstractLinkDetail NULL = new AbstractLinkDetail(null, null, null) {
		
		@Override
		public boolean isOrderingLink() {
			return false;
		}
		
		@Override
		public float getNumberOfLinks() {
			return 0;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Iterable<BiDirectional<Connected>> getConnections() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public void processToLevel(LinkProcessor lp, int i) {
		}

		@Override
		public boolean from(Group b) {
			return false;
		}

		@Override
		public int getLinkRank() {
			return 0;
		}
	};
	
	/**
	 * This is used where we want to inherit a link detail in a compound group from one of the 
	 * contained groups.
	 */
	public class WrapLinkDetail extends AbstractLinkDetail {
		
		LinkDetail inside;

		public WrapLinkDetail(LinkDetail other, Group lmsGroup) {
			super(other.getDirection(), other.getGroup(), lmsGroup);
			this.inside = other;
		}

		@Override
		public boolean from(Group b) {
			return super.from(b) || inside.from(b);
		}

		@Override
		public boolean isOrderingLink() {
			return inside.isOrderingLink();
		}

		@Override
		public float getNumberOfLinks() {
			return inside.getNumberOfLinks();
		}

		@Override
		public Iterable<BiDirectional<Connected>> getConnections() {
			return inside.getConnections();
		}

		@Override
		public void processToLevel(LinkProcessor lp, int i) {
			inside.processToLevel(lp, i);
		}

		@Override
		public int getLinkRank() {
			return inside.getLinkRank();
		}
		
	}
	
	public class CompoundLinkDetail extends AbstractLinkDetail {
	
		LinkDetail a, b;
		
		public CompoundLinkDetail(Group cg, Group lmsGroup, AbstractLinkDetail a, AbstractLinkDetail b) {
			super(ms.getContradictionHandler().checkContradiction(a, b, null), cg, lmsGroup);
			a = a==null ? NULL : a;
			b = b==null ? NULL : b;
			this.a = a;
			this.b = b;
			
			this.inContainer = a.inContainer || b.inContainer;
			this.nearestNeighbour = a.nearestNeighbour || b.nearestNeighbour;
		}
	
	
		@Override
		public boolean isOrderingLink() {
			return a.isOrderingLink() || b.isOrderingLink();
		}
	
		@Override
		public float getNumberOfLinks() {
			return a.getNumberOfLinks() + b.getNumberOfLinks();
		}
	
		@Override
		public Iterable<BiDirectional<Connected>> getConnections() {
			return new Iterable<BiDirectional<Connected>> () {
	
				@Override
				public Iterator<BiDirectional<Connected>> iterator() {
					return new Iterator<BiDirectional<Connected>> () {
						Iterator<BiDirectional<Connected>> ai = a.getConnections().iterator();
						Iterator<BiDirectional<Connected>> bi = b.getConnections().iterator();
						
						@Override
						public boolean hasNext() {
							return ai.hasNext() || bi.hasNext();
						}
	
						@Override
						public BiDirectional<Connected> next() {
							if (ai.hasNext()) {
								return ai.next();
							} else {
								return bi.next();
							}
						}
	
						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
						
					};
				}
			};
		}


		@Override
		public void processToLevel(LinkProcessor lp, int i) {
			if (i > 0) {
				((AbstractLinkDetail)a).processToLevel(lp, i-1);
				((AbstractLinkDetail)b).processToLevel(lp, i-1);
			} else {
				super.processToLevel(lp, i-1);
			}
		}


		@Override
		public boolean from(Group g) {
			if (super.from(g)) {
				return true; 
			} else {
				return ((AbstractLinkDetail)a).from(g) || ((AbstractLinkDetail)b).from(g);
			}
		}

		@Override
		public int getLinkRank() {
			return Math.max(a.getLinkRank(), b.getLinkRank());
		}
		
		
	}
	
	public int getAxisDirection(MergePlane mp, Direction d, boolean incontainer) {
		int mask = incontainer ? 2048 : 0;
		
		if (mp == MergePlane.X_FIRST_MERGE) {
			mask = mask + 1024;
		} else if (mp == MergePlane.Y_FIRST_MERGE) {
			mask = mask + 512;
		} else {
			mask = mask + 128;
		}
		
		if (d==null) {
			return mask;
		}
		
		switch (d) {
			case UP:
				return mask + 1;
			case DOWN:
				return mask + 2;
			case LEFT:
				return mask + 3;
			case RIGHT:
			default:
				return mask + 4;
		}

	}

	
	/**
	 * Set if the nearest neighbour is single.
	 */
	private Map<Integer, Object> singleDirectedMergeOption = new HashMap<Integer, Object>(10);

	private final static Object NONE = new Object();

	public Group getSingleDirectedMergeOption(final Direction l, final MergePlane axis, BasicMergeState ms, boolean inContainer) {
		Integer ad = getAxisDirection(axis, l, inContainer);
		Object out = singleDirectedMergeOption.get(ad);
		if (out == null) { 
			//System.out.println("Recalc of SDM "+axis+" "+l+" "+inContainer+" group="+g.getGroupNumber());
			out = NONE;
			int mask = createMask(axis, true, inContainer, l);
			Collection<Group> options = subsetGroup(mask);
			if (options.size() == 1) {
				Group first = options.iterator().next();
				int rmask = createMask(axis, true, inContainer, Direction.reverse(l));
				DirectedLinkManager lm = (DirectedLinkManager) first.getLinkManager();
				Collection<Group> reverse = lm.subsetGroup(rmask);
				if ((reverse.size() == 1) && (reverse.contains(this.g))) {
					out = first;
					lm.singleDirectedMergeOption.put(getAxisDirection(axis, Direction.reverse(l), inContainer), this.g);
				}
			}
			singleDirectedMergeOption.put(ad, out);
		}

		if (out != NONE) {
			return (Group) out;
		} else {
			return null;
		}

	}
	
	private static Set<Container> containersFor(Group a, BasicMergeState ms) {
		Map<Container, GroupContainerState> cf = ms.getContainersFor(a);
		Set<Container> ac = cf == null ? null : cf.keySet();
		return ac;
	}
	
	public boolean sameContainer(Group in, BasicMergeState ms) {
		Set<Container> ac = containersFor(g, ms);
		Set<Container> bc = Collections.emptySet();
		if (ac==null) {
			// deal with containers not being set to start with
			ac = containersFor(((CompoundGroup)g).getA(), ms);
			bc = containersFor(((CompoundGroup)g).getB(), ms);
		}
		
		Set<Container> itc = containersFor(in, ms);
		for (Container container : itc) {
			if (ac.contains(container)) {
				return true;
			}
			if (bc.contains(container)) {
				return true;
			}
		}
		
		return false;
	}

	private void ensureNearestNeighboursInitialised(int mask) {
		if (!masked(mask, MASK_NEAREST_NEIGHBOUR)) {
			return;
		}
			
		if (masked(mask, MASK_X_FIRST)) {
			initialiseNearestNeighbourPlane(mask, MergePlane.X_FIRST_MERGE);	
		}
		
		if (masked(mask, MASK_Y_FIRST)) {
			initialiseNearestNeighbourPlane(mask, MergePlane.Y_FIRST_MERGE);	
		}
	}

	private void initialiseNearestNeighbourPlane(int mask, MergePlane mp) {
		if (masked(mask, MASK_UP))
			initialiseNearestNeighbours(mp, Direction.UP);
		if (masked(mask, MASK_DOWN))
			initialiseNearestNeighbours(mp, Direction.DOWN);
		if (masked(mask, MASK_LEFT))
			initialiseNearestNeighbours(mp, Direction.LEFT);
		if (masked(mask, MASK_RIGHT))
			initialiseNearestNeighbours(mp, Direction.RIGHT);
	}
	
	private void initialiseNearestNeighbours(MergePlane mp, Direction d) {
		if (mp == MergePlane.X_FIRST_MERGE) {
			if (xFirstNearestNeighbours.contains(d)) {
				return;
			}
		} else if (mp == MergePlane.Y_FIRST_MERGE) {
			if (yFirstNearestNeighbours.contains(d)) {
				return;
			}
		} else {
			throw new LogicException("Must be checking NN with a given axis");
		}
		
		int mask = DirectedLinkManager.createMask(mp, false, false, d);
		Collection<LinkDetail> myNeighbours = subset(mask);
		
		// set them all to true to start with
		for (LinkDetail linkDetail : myNeighbours) {
			((AbstractLinkDetail)linkDetail).nearestNeighbour = true;
		}
		
		// narrow them down
		for (LinkDetail linkDetail : myNeighbours) {
			checkForExclusions(linkDetail.getGroup(), d, mp, 0, new GroupChain(linkDetail.getGroup()));
		} 
		
		if (mp == MergePlane.X_FIRST_MERGE) {
			xFirstNearestNeighbours.add(d);
		} 
		
		if (mp == MergePlane.Y_FIRST_MERGE) {
			yFirstNearestNeighbours.add(d);
		}
	}
	
	private static class GroupChain {
		
		GroupChain prev;
		Group g;
		
		public GroupChain(Group g) {
			this.g = g;
		}
		
		public GroupChain(Group g,GroupChain prev) {
			this.g = g;
			this.prev = prev;
		}
		
		public boolean stop(Group g) {
			return (this.g == g) || (prev != null ? prev.stop(g) : false);
		}
	}
	

	private void checkForExclusions(Group group, Direction d, MergePlane mp, int level, GroupChain gc) {
		//System.out.println("Exc check: "+group+" level "+level);
		int mask = DirectedLinkManager.createMask(mp, false, false, d);
		Collection<Group> groupNeighbours = group.getLinkManager().subsetGroup(mask);
		
		for (Group g1 : groupNeighbours) {
			AbstractLinkDetail ld = links.get(g1);
			if (ld != null) {
				if (!ld.isOrderingLink()) {
					// for ordering links, they are always neighbours (even when
					// there is a contradiction)
					ld.nearestNeighbour = false;
				}
			} else {
				if (!gc.stop(g1)) {
					checkForExclusions(g1, d, mp, level + 1, new GroupChain(g1, gc));
				} else {
					setContradiction(new GroupChain(g1, gc), g1);
				}
			}
		}
	}
	
	private void setContradiction(GroupChain groupChain, Group stop) {
		LinkDetail best = groupChain.g.getLink(groupChain.prev.g);
		float bestCount = Float.MAX_VALUE;
		boolean ordering = best.isOrderingLink();
		groupChain = groupChain.prev;
		while (groupChain.g != stop) {
			LinkDetail current = groupChain.g.getLink(groupChain.prev.g);
			
			if (((current.isOrderingLink() == false) && (ordering == true)) || 
				(current.getNumberOfLinks() < bestCount)) {
				best = current;
				bestCount = current.getNumberOfLinks();
				ordering = false;
			}
		
			groupChain = groupChain.prev;
		}
		
		for (BiDirectional<Connected> c : best.getConnections()) {
			ms.getContradictionHandler().setContradiction(c, false);
		}
	}

	public void sortLink(Direction d, Group otherGroup, float linkValue, boolean ordering, int linkRank, Iterable<BiDirectional<Connected>> c) {
		if (keepLinkTo(otherGroup)) {
			checkAddLinkDetail(d, otherGroup, linkValue, ordering, linkRank, c);
		}
	}
	
	public void sortLink(LinkDetail ld) {
		if (keepLinkTo(ld.getGroup())) {
			checkAddLinkDetail(ld);
		}
	}
	
	private void checkAddLinkDetail(LinkDetail other) {
		AbstractLinkDetail existing = (AbstractLinkDetail) links.remove(other.getGroup());
		AbstractLinkDetail cld = null;
		if (existing == null) {
			cld = new WrapLinkDetail(other, this.g);
		} else {
			cld = new CompoundLinkDetail(other.getGroup(), this.g, existing, (AbstractLinkDetail) other);
		}
		
		cld.inContainer = sameContainer(other.getGroup(), ms);
		links.put(other.getGroup(), cld);
		
		lc += other.getNumberOfLinks();
	}

	private void checkAddLinkDetail(Direction d, Group otherGroup,
			float linkValue, boolean ordering, int linkRank,
			Iterable<BiDirectional<Connected>> c) {
		
		ActualLinkDetail ld = new ActualLinkDetail(otherGroup, this.g, linkValue, d, ordering, linkRank, c);
		checkAddLinkDetail(ld);
	}

	public static boolean matches(AbstractLinkDetail ld, int mask) {
		if (mask == -1) {
			return true;
		}
		
		// plane mask
		boolean planeOK = false;
		if (masked(mask, MASK_X_FIRST)) {
			if (MergePlane.X_FIRST_MERGE.matches(DirectedGroupAxis.getState(ld.getGroup()))) {
				planeOK = true;
			}
		}
		if (masked(mask, MASK_Y_FIRST)) {
			if (MergePlane.Y_FIRST_MERGE.matches(DirectedGroupAxis.getState(ld.getGroup()))) {
				planeOK = true;
			}
		} 
		
		if (!planeOK) {
			return false;
		}
		
		// nearest neighbour
		if (masked(mask, MASK_NEAREST_NEIGHBOUR)) {
			if (!ld.nearestNeighbour) {
				return false;
			}
		}
		
		// in container
		if (masked(mask, MASK_IN_CONTAINER)) {
			if (!ld.inContainer) {
				return false;
			}
		}
		
		// direction mask
		boolean directionOK = false;
		
		if (masked(mask, MASK_NO_DIRECTION)) {
			if (ld.d == null) {
				directionOK = true;
			}
		}
		
		if (masked(mask, MASK_LEFT)) {
			if (ld.d == Direction.LEFT) {
				directionOK = true;
			}
		}
		
		if (masked(mask, MASK_RIGHT)) {
			if (ld.d == Direction.RIGHT) {
				directionOK = true;
			}
		}
		
		if (masked(mask, MASK_UP)) {
			if (ld.d == Direction.UP) {
				directionOK = true;
			}
		}
		
		if (masked(mask, MASK_DOWN)) {
			if (ld.d == Direction.DOWN) {
				directionOK = true;
			}
		}
		
		if (!directionOK) {
			return false;
		}
		
		return true;
	}
	
	private static boolean masked(int mask, int m) {
		if (mask == -1) {
			return false;
		}
		
		return (mask & m)  >0;
		
	}

	public abstract static class FilterIterator<X> implements Iterator<X> {

		Iterator<Entry<Group,AbstractLinkDetail>> i;
		int mask;
		
		public FilterIterator(Collection<Entry<Group,AbstractLinkDetail>> links, int mask) {
			i =  links.iterator();
			this.mask = mask;
		}
		
		Entry<Group,AbstractLinkDetail> next = null;
		
		@Override
		public boolean hasNext() {
			ensureNext();
			return (next != null);
		}

		protected void ensureNext() {
			while ((next == null) && (i.hasNext())) {
				next = i.next();
				if (!matches(next.getValue(), mask)) {
					next = null;
				}
 			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class GroupFilterIterator extends FilterIterator<Group> {

		public GroupFilterIterator(Collection<Entry<Group,AbstractLinkDetail>> links, int mask) {
			super(links, mask);
		}
		
		@Override
		public Group next() {
			ensureNext();
			if (next ==null) {
				throw new NoSuchElementException();
			}
			Group out = next.getKey();
			this.next = null;
			return out;
		}
		
	}
	
	public static class LinkDetailFilterIterator extends FilterIterator<LinkDetail> {

		public LinkDetailFilterIterator(Collection<Entry<Group,AbstractLinkDetail>> links, int mask) {
			super(links, mask);
		}
		
		@Override
		public LinkDetail next() {
			ensureNext();
			if (next ==null) {
				throw new NoSuchElementException();
			}
			LinkDetail out = next.getValue();
			this.next = null;
			return out;
		}
	}
	
	public static abstract class AbstractDLMCollection<X> extends AbstractCollection<X> {

		int s = -1;

		@Override
		public int size() {
			if (s == -1) {
				s = 0;
				for (Iterator<?> iterator = this.iterator(); iterator.hasNext();) {
					iterator.next();
					s++;
				}
			}
			return s;
		}
	}
	
	@Override
	public Collection<LinkDetail> subset(final int mask) {
		ensureNearestNeighboursInitialised(mask);
		return new AbstractDLMCollection<LinkDetail>() {

			@Override
			public Iterator<LinkDetail> iterator() {
				return new LinkDetailFilterIterator(links.entrySet(), mask);
			}
		};
	}

	@Override
	public Collection<Group> subsetGroup(final int mask) {
		ensureNearestNeighboursInitialised(mask);
		return new AbstractDLMCollection<Group>() {

			@Override
			public Iterator<Group> iterator() {
				return new GroupFilterIterator(links.entrySet(), mask);
			}
		};
	}
	
	public static int all() {
		return -1;
	}

	@Override
	public int allMask() {
		return -1;
	}

	@Override
	public LinkDetail get(Group g) {
		return links.get(g);
	}

	int lc = 0;
	
	@Override
	public int getLinkCount() {
		return lc;
	}
	
	public void setLinkCount(int lc) {
		this.lc = lc;
	}

	@Override
	public void notifyContainerChange(Group g) {
		AbstractLinkDetail ld = links.get(g);
		ld.inContainer = sameContainer(g, ms);
	}
	

	@Override
	public void notifyContainerChange() {
		for (AbstractLinkDetail ld : links.values()) {
			ld.inContainer = sameContainer(ld.getGroup(), ms);		
		}
	}
	

	@Override
	public void notifyMerge(CompoundGroup g, boolean aRemains, boolean bRemains) {
		MergePlane aggregateDimension = DirectedGroupAxis.getState(g);
		AbstractLinkDetail aLD = links.remove(g.getA());
		AbstractLinkDetail bLD = links.remove(g.getB());
		AbstractLinkDetail cLD = new CompoundLinkDetail(g, this.g, aLD, bLD);
		
		
		switch (aggregateDimension) {
		case X_FIRST_MERGE:
			maybeKeepLink(g.getA(), aLD, aRemains, MergePlane.Y_FIRST_MERGE);
			maybeKeepLink(g.getB(), bLD, bRemains, MergePlane.Y_FIRST_MERGE);
			upgradeLink(g, cLD, MergePlane.X_FIRST_MERGE);
			break;
		case Y_FIRST_MERGE:
			maybeKeepLink(g.getA(), aLD, aRemains, MergePlane.X_FIRST_MERGE);
			maybeKeepLink(g.getB(), bLD, bRemains, MergePlane.X_FIRST_MERGE);
			upgradeLink(g, cLD, MergePlane.Y_FIRST_MERGE);
			break;
		case UNKNOWN:
			upgradeLink(g, cLD, MergePlane.Y_FIRST_MERGE);
			upgradeLink(g, cLD, MergePlane.X_FIRST_MERGE);
		}
	}

	private void upgradeLink(CompoundGroup g, AbstractLinkDetail cLD, MergePlane plane) {
		if (keepLinkTo(g)) {
			links.put(g, cLD);
			
			// keep nearest neighbour up-to-date
			Direction d = cLD.d;
			if ((d != null) && (cLD.nearestNeighbour)) {
				checkNearestNeighbourInPlane(g, d, plane);
				//System.out.println("Clear SDM "+plane+" "+d+" group="+this.g.getGroupNumber());

				singleDirectedMergeOption.remove(getAxisDirection(plane, d, true));
				singleDirectedMergeOption.remove(getAxisDirection(plane, d, false));
			}	
		}
	}

	private void maybeKeepLink(Group oldGroup, AbstractLinkDetail aLD, boolean remains, MergePlane plane) {
		if (remains && (aLD!=null) && keepLinkTo(oldGroup)) {
			links.put(oldGroup, aLD);
		}	
	}

	private void checkNearestNeighbourInPlane(Group g, Direction d, MergePlane merge) {
		MergePlane mp = DirectedGroupAxis.getState(this.g);
		if (mp.matches(merge)) {
			if (merge == MergePlane.X_FIRST_MERGE) {
				if (!xFirstNearestNeighbours.contains(d))
					return;
			} else if (merge == MergePlane.Y_FIRST_MERGE) {
				if (!yFirstNearestNeighbours.contains(d)) {
					return;
				}
			}
			
			checkForExclusions(g, d, merge, 0, new GroupChain(g));
		}
	}

	@Override
	public void processAllLeavingLinks(boolean compound, int mask, LinkProcessor lp) {
		for (LinkDetail ld : subset(mask)) {
			if (compound) {
				lp.process(this.g, ld.getGroup(), ld);
			} else {
				((AbstractLinkDetail)ld).processLowestLevel(lp);
			}	
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Group, LinkDetail> forLogging() {
		return (Map<Group, LinkDetail>) (Map<?, ?>) links;
	}

	@Override
	public void notifyAxisChange() {
		for (Iterator<Group> iterator = links.keySet().iterator(); iterator.hasNext();) {
			Group g = iterator.next();
			if (!keepLinkTo(g)) {
				iterator.remove();
			}
		}
	}

	
	
}
