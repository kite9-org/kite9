package org.kite9.diagram.visualization.planarization.rhd.links;

import java.util.Collection;
import java.util.Map;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;

/**
 * Handles the links from one group to others.  
 */
public interface LinkManager {
	
	public static interface LinkProcessor {

		void process(Group originatingGroup, Group destinationGroup, LinkDetail ld);
		
	}
	
	/**
	 * Uses a link processor to manage the iteration.
	 */
	public void processAllLeavingLinks(boolean compound, int mask, LinkProcessor lp);

	
	public static interface LinkDetail {

		/**
		 * Rank allows us to prioritise importance of adhering to a link.
		 * Higher = later specified link = more important.
		 */
		public int getLinkRank();
		
		/**
		 * True if this link is involved in ordering a container
		 */
		public abstract boolean isOrderingLink();

		public abstract Direction getDirection();

		public abstract float getNumberOfLinks();
		
		public Iterable<BiDirectional<Connected>> getConnections();
		
		public Group getGroup();
		
		public void processToLevel(LinkProcessor lp, int l);
		
		public void processLowestLevel(LinkProcessor lp);

		/**
		 * Returns true if the linkdetail leaves a given group.
		 */
		public boolean from(Group b);
		
	}
	
	/**
	 * Called when two original groups (linked to this LM) are merged into a compound group.
	 */
	public void notifyMerge(CompoundGroup g, boolean aRemains, boolean bRemains);
	
	/**
	 * Called when a group (linked to this LM) changes container, because a container gets completed.
	 */
	public void notifyContainerChange(Group g);
	
	/**
	 * Called when this group changes container.
	 */
	public void notifyContainerChange();
	
	/**
	 * Called when the group with it's links being managed has an axis change
	 */
	public void notifyAxisChange();
	
	public Collection<LinkDetail> subset(int mask);
	
	public Collection<Group> subsetGroup(int mask);
	
	public int allMask();
	
	public LinkDetail get(Group g);
	
	/**
	 * Tells the lm what group it is for.
	 */
	public void setGroup(Group g);
	
	/**
	 * Adds links to the link manager.
	 */
	public void sortLink(Direction d, Group otherGroup, float linkValue, boolean ordering, int linkRank,
			Iterable<BiDirectional<Connected>> c);
	
	/**
	 * Adds an existing link detail to the link manager (promoted from a sub-group)
	 */
	public void sortLink(LinkDetail ld);

	public int getLinkCount();
	
	public void setLinkCount(int lc);
	
	public Map<Group, LinkDetail> forLogging();
}
