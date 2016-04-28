package org.kite9.diagram.visualization.planarization.rhd.layout;

import java.util.Arrays;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos;
import org.kite9.framework.logging.LogicException;

/**
 * The exit matrix keeps track of links leaving a group in order that we can perform 
 * 
 * @author robmoffat
 *
 */
public class ExitMatrix {
	
	public enum RelativeSide {FACING, MIDDLE, OPPOSITE};

	private float counts[][] = new float[3][];
	private Bounds[] spans = new Bounds[] { 
			BasicBounds.EMPTY_BOUNDS,
			BasicBounds.EMPTY_BOUNDS,
			BasicBounds.EMPTY_BOUNDS,
			BasicBounds.EMPTY_BOUNDS
	};
	
	private Bounds sizex = BasicBounds.EMPTY_BOUNDS, sizey = BasicBounds.EMPTY_BOUNDS;

	private boolean empty = true;
	
	private float getCount(int x, int y) {
		return counts[y][x];
	}
	
	private void incrCount(DPos x, DPos y, float v) {
		counts[getIndex(y)][getIndex(x)] += v;
	}
	
	private int getIndex(DPos d) {
		return d.ordinal();
	}
		
	
	public String toString() {
		return "ExitMatrix[spans="+Arrays.toString(spans)+","+Arrays.toString(counts[0])+"/"+Arrays.toString(counts[1])+"/"+Arrays.toString(counts[2])+"]";
	}
	
	public ExitMatrix() {
		for (int i = 0; i < counts.length; i++) {
			counts[i] = new float[3];
		}
	}
	
	public ExitMatrix(float[][] counts, boolean empty) {
		this.counts = counts;
		this.empty = empty;
	}
	
	public void setSpans(Bounds[] spans) {
		this.spans = spans;
	}
	
	public void setSize(Bounds x, Bounds y) {
		this.sizex = x;
		this.sizey = y;
	}
	
	public void addLink(Group originatingGroup, Group destinationGroup, LinkDetail ld, RoutableHandler2D rh) {
		//System.out.println("Adding link from "+originatingGroup+ " to "+destinationGroup);
		RoutingInfo oPos = originatingGroup.getAxis().getPosition(rh, true);
		RoutingInfo dPos = destinationGroup.getAxis().getPosition(rh, true);
		DPos xCompare = rh.compareBounds(rh.getBoundsOf(dPos, true), rh.getBoundsOf(oPos, true));
		DPos yCompare = rh.compareBounds(rh.getBoundsOf(dPos, false), rh.getBoundsOf(oPos, false));
		incrCount(xCompare, yCompare, ld.getNumberOfLinks());
		
		if (yCompare == DPos.OVERLAP) {
			if (xCompare == DPos.BEFORE) {
				expandBounds(Direction.LEFT, oPos, rh);
			} else if (xCompare == DPos.AFTER) {
				expandBounds(Direction.RIGHT, oPos, rh);
			}
		} 
		
		if (xCompare == DPos.OVERLAP) {
			if (yCompare == DPos.BEFORE) {
				expandBounds(Direction.UP, oPos, rh);
			} else if (yCompare == DPos.AFTER) {
				expandBounds(Direction.DOWN, oPos, rh);
			}
		}
		empty = false;
		//System.out.println(this);
	}

	private void expandBounds(Direction d, RoutingInfo oPos, RoutableHandler2D rh) {
		Bounds b;
		
		switch (d) {
		case LEFT:
		case RIGHT:
			b = rh.getBoundsOf(oPos, false);
			break;
		case UP:
		case DOWN:
			b = rh.getBoundsOf(oPos, true);
			break;
		default:
			throw new LogicException("Unexpected direction");
		}
		spans[d.ordinal()] = spans[d.ordinal()].expand(b);
	}
	
	
	public float getLinkCount(Layout d, RelativeSide rs, int rank) {
		int rsRank = rs.ordinal();
		switch (d) {
		case UP:
			return getCount(rank+1, rsRank);
		case DOWN:
			return getCount(rank+1, 2-rsRank);
		case LEFT:
			return getCount(rsRank, rank+1);
		case RIGHT:
			return getCount(2-rsRank, rank+1);
		default:
			throw new LogicException("Not sure what to return");
		}
	}
	
	public Bounds getSpanInDirectionOfLayout(Layout d) {
		switch (d) {
		case UP:
			return spans[Direction.UP.ordinal()];
		case DOWN:
			return spans[Direction.DOWN.ordinal()];
		case LEFT:
			return spans[Direction.LEFT.ordinal()];
		case RIGHT:
			return spans[Direction.RIGHT.ordinal()];
		default:
			throw new LogicException("Span not defined for "+d);
		}
	}
	
	public boolean isEmpty() {
		return empty;
	}

	public Bounds getSizeInDirectionOfLayout(Layout d) {
		switch (d) {
		case DOWN:
		case UP:
		case VERTICAL:
			return sizey;
		default:
			return sizex;
		}
	}
	
}
