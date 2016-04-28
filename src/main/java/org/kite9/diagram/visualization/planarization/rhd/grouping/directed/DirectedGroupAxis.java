/**
 * 
 */
package org.kite9.diagram.visualization.planarization.rhd.grouping.directed;

import java.util.Set;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.LogicException;

public class DirectedGroupAxis implements GroupAxis {

	Group g;
	public void setGroup(Group g) {
		this.g = g;
	}

	Kite9Log log;

	public DirectedGroupAxis(Kite9Log log) {
		this.log = log;
	}

	private boolean layoutRequired = true;
	
	public boolean isLayoutRequired() {
		return layoutRequired;
	}

	public void setLayoutRequired(boolean layoutRequred) {
		this.layoutRequired = layoutRequred;
	}

	MergePlane state = MergePlane.UNKNOWN;
	boolean active = true;

	public boolean isActive() {
		return active;
	}
	
	@Override
	public String toString() {
		return state.toString();
	}
	
	/**
	 * Given two groups and the layout between them, this works out what type the merge should be.
	 * Effectively, this controls whether two groups are allowed to merge with one another.  If it returns null,
	 * the merge isn't allowed.
	 */
	public static MergePlane getMergePlane(Group a, Group b) {
		switch (DirectedGroupAxis.getState(a)) {
		case X_FIRST_MERGE:
			switch (DirectedGroupAxis.getState(b)) {
			case X_FIRST_MERGE:
			case UNKNOWN:
				return MergePlane.X_FIRST_MERGE;
			case Y_FIRST_MERGE:
				return null;
			}
			
		case Y_FIRST_MERGE:
			switch (DirectedGroupAxis.getState(b)) {
			case Y_FIRST_MERGE:
			case UNKNOWN:
				return MergePlane.Y_FIRST_MERGE;
			case X_FIRST_MERGE:
				return null;
			}
		case UNKNOWN:
			switch (DirectedGroupAxis.getState(b)) {
			case X_FIRST_MERGE:
				return MergePlane.X_FIRST_MERGE;
			case Y_FIRST_MERGE:
				return MergePlane.Y_FIRST_MERGE;
			case UNKNOWN:
				LinkDetail ld = a.getLink(b);
				if ((ld!=null) && (ld.getDirection()!=null)) {
					switch (ld.getDirection()) {
					case UP:
					case DOWN:
						return MergePlane.Y_FIRST_MERGE;
					case LEFT:
					case RIGHT:
						return MergePlane.X_FIRST_MERGE;
					}
				}
				
				return MergePlane.UNKNOWN;
			}
		}
		
		throw new LogicException("Eventuality not considered: "+getType(a)+ " "+ getType(b));
	}
	
	/**
	 * Only allows the merge if the neighbour is in the right state
	 * @param l 
	 */
	public static boolean compatibleNeighbour(Group originatingGroup, Group destinationGroup) {
		return getMergePlane(originatingGroup, destinationGroup) != null;
	}

	public static boolean inState(Group group, Object... okStates) {
		for (int i = 0; i < okStates.length; i++) {
			if (getState(group) == okStates[i]) {
				return true;
			}
		}
		return false;
	}

	public static MergePlane getState(Group group) {
		return ((DirectedGroupAxis)group.getType()).state;
	}

	@Override
	public boolean isHorizontal() {
		return horizontal;
	}
	
	@Override
	public boolean isVertical() {
		return vertical;
	}
	
	private boolean horizontal = false, vertical = false;
	
	public CompoundGroup horizParentGroup;
	public CompoundGroup vertParentGroup;
	
	
	@Override
	public CompoundGroup getParentGroup(boolean horizontal) {
		return horizontal ? horizParentGroup : vertParentGroup;
	}

	public boolean isSet(RoutableHandler2D rh, Bounds ri, boolean horiz) {
		if (ri==null) {
			return false;
		} else {
			return true;
		}
	}
	
	public Bounds getPosition1D(RoutableHandler2D rh, boolean temp, boolean horiz) {
		Bounds ri = rh.getPosition(g, horiz);
		
		if ((!isSet(rh, ri, horiz)) && (temp)) {
			ri = rh.getTempPosition(g, horiz);
		}
		
		if (!isSet(rh, ri, horiz)){
			// ok, calculate via parent groups.  
			Bounds out = null;
			
			Group parent = horiz ? horizParentGroup : vertParentGroup;
			
			if (parent != null) {
				if (parent.getAxis().isLayoutRequired()) {
					Layout l = getLayoutFor(parent, g);
					out = rh.narrow(l, ((DirectedGroupAxis)parent.getAxis()).getPosition1D(rh, temp, horiz), horiz, true);	
				} else { 
					out = ((DirectedGroupAxis)parent.getAxis()).getPosition1D(rh, temp, horiz);
				}
			} else {
				// no parent group = top
				out = rh.getTopLevelBounds(horiz);
			} 
			
			//log.send("Setting "+(temp? "temp" : "real") + (horiz ? "horiz" : "vert")+" position for "+g+"\n\t"+out);
			if (temp) {
				rh.setTempPosition(g, out, horiz);
			} else {
				log.send("Placed: "+g.getGroupNumber()+ " "+horiz+" "+out);
				rh.setPlacedPosition(g, out, horiz);
			}
			
			if ((!temp) && (g instanceof LeafGroup)) {
				// this means the routable handler also has final positions for each contained element
				rh.setPlacedPosition(((LeafGroup)g).getContained(), out, horiz);
			}
			
			return out;
		} else {
			return ri;
		}
	}

	public RoutingInfo getPosition(RoutableHandler2D rh, boolean temp) {
		Bounds xBounds = getPosition1D(rh, temp, true);
		Bounds yBounds = getPosition1D(rh, temp, false);
		return rh.createRouting(xBounds, yBounds);
	}
	
	public static Layout getLayoutFor(Group in, Group g) {
		DirectedGroupAxis ax = DirectedGroupAxis.getType(in);
		if (in.getLayout()==null) {
			return null;
		}
		switch (in.getLayout()) {
		case LEFT:
		case RIGHT:
			return ax.isHorizontal() ? layoutSide(in, g) : null;
		case UP:
		case DOWN:
			return ax.isVertical() ? layoutSide(in, g) : null;
		default:
			//HORIZONTAL, VERTICAL:
			return in.getLayout();
		}
	}

	private static Layout layoutSide(Group in, Group g) {
		CompoundGroup cg = (CompoundGroup) in;
		if (cg.getB() == g) {
			return cg.getLayout();
		} else if (cg.getA() == g) {
			return Layout.reverse(cg.getLayout());
		} else {
			throw new LogicException();
		}
	}

	public static DirectedGroupAxis getType(Group g) {
		DirectedGroupAxis out = (DirectedGroupAxis) g.getAxis();
		return out;
	}

	@Override
	public boolean isReadyToPosition(Set<Group> completedGroups) {
		boolean hready= (this.horizParentGroup == null) || (completedGroups.contains(this.horizParentGroup));
		boolean vready= (this.vertParentGroup == null) || (completedGroups.contains(this.vertParentGroup));
		return hready && vready;
	}

	public void setHorizontal(boolean horizontal) {
		this.horizontal = horizontal;
	}

	public void setVertical(boolean vertical) {
		this.vertical = vertical;
	}


	
}