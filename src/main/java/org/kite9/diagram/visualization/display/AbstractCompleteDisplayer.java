package org.kite9.diagram.visualization.display;

import org.kite9.diagram.common.elements.mapping.GeneratedLayoutConnection;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

public abstract class AbstractCompleteDisplayer implements CompleteDisplayer, DiagramSizer, Logable {
	
	private int gridSize = 12;

	double buffer;
	protected Kite9Log log = new Kite9Log(this);


	/**
	 * Set buffer > 0 to ensure gaps even around invisible attr.  0 should be used for correct rendering
	 */
	public AbstractCompleteDisplayer(boolean buffer, int gridSize) {
		this.buffer = buffer ? gridSize : 0;
		this.gridSize = gridSize;
	}

	public boolean isVisibleElement(DiagramElement de) {
		return (de.getRenderingInformation().isRendered());
	}
	
	public static final double MINIMUM_GLYPH_SIZE = 2;
	public static final double MINIMUM_ARROW_SIZE = 2;
	
	public static final double EDGE_DISTANCE = 2;
	

	public double getMinimumDistanceBetween(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide, Direction d, DiagramElement along, boolean concave) {
		double distance = getMinimumDistanceInner(a,aSide,  b, bSide, d, along, concave);
		log.send(log.go() ? null : "Minimum distances between  " + a + "  " + aSide+ " "+ b + " "+ bSide +" in " + d + " is " + distance);
		return distance;

	}

	/**
	 * Given two diagram attr, separated with a gutter, figure out how much
	 * space must be between them.
	 * @param concave 
	 */
	private double getMinimumDistanceInner(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide, Direction d, DiagramElement along, boolean concave) {
		double length;
		
		if ((a instanceof GeneratedLayoutConnection) || (b instanceof GeneratedLayoutConnection)) {
			return 0;
		} else if ((a instanceof Connection) && (b instanceof Connection)) {
			length = getMinimumDistanceConnectionToConnection((Connection) a, aSide, (Connection) b, bSide, d, along, concave);
		} else if ((a instanceof Rectangular) && (b instanceof Rectangular)) {
			length = getMinimumDistanceRectangularToRectangular((Rectangular) a, aSide, (Rectangular) b, bSide, d, along, concave);
		} else if ((a instanceof Rectangular) && (b instanceof Connection)) {
			length = getMinimumDistanceRectangularToConnection((Rectangular) a, aSide, (Connection) b, bSide, d, along);
		} else if ((a instanceof Connection) && (b instanceof Rectangular)) {
			length = getMinimumDistanceRectangularToConnection((Rectangular) b, bSide, (Connection) a, aSide, Direction.reverse(d), along);
		} else {
			throw new Kite9ProcessingException("Don't know how to calc min distance");
		}
		
		return length;
	}

	private double getMinimumDistanceRectangularToConnection(Rectangular a, Direction aSide, Connection b, Direction bSide, Direction d, DiagramElement along) {
		if (aSide == d) {
			// we are outside a
			if (isEventualParent(a, b)) {
				return 0;
			} else {
				double inset = getMargin(a, aSide);
				double margin = getMargin(b, bSide);
				double length = Math.max(inset, margin);
				return incorporateAlongMinimumLength(along, d, length, a, aSide, b, bSide);
			}
		} else {
			if ((a instanceof Connected) && (b.meets((Connected) a))) {
				// a connection arriving at a rectangular
				return incorporateAlongMinimumLength(along, d, 0, a, aSide, b, bSide);
			} else {
				// we are inside a, so use the padding distance
				double inset = getPadding(a, d);
				double margin = getMargin(b, Direction.reverse(d));
				double length = Math.max(inset, margin);
				return incorporateAlongMinimumLength(along, d, length, a, aSide, b, bSide);
			}
		}
	}

	private double incorporateAlongMinimumLength(DiagramElement along, Direction d, double in, DiagramElement a, Direction aSide, DiagramElement b, Direction bSide) {
		if (along instanceof Connection) {
			if ((in == 0) && passingThrough((Connection) along, a, b)) {
				// in this special case, the link can pass 
				return 0;
			}
			
			double alongDist = getAlongMinimumLength((Connection) along, d, a, aSide, b, bSide);
			return Math.max(in, alongDist);
			
		} else if (along instanceof Connected) {
			
			double alongDist = getAlongMinimumLength((Connected) along, d, a, aSide, b, bSide);
			return Math.max(in, alongDist);
			
		} else {
			return in;
		}
		
	}
	
	private boolean passingThrough(Connection along, DiagramElement a, DiagramElement b) {
		boolean touchesA = (a instanceof Connected) && along.meets((Connected)a);
		boolean touchesB = (b instanceof Connected) && along.meets((Connected)b);
			
		return !touchesA && !touchesB;
	}

	private double getAlongMinimumLength(Connection along, Direction d, DiagramElement a, Direction aSide, DiagramElement b, Direction bSide) {
		Connection c = along;
		boolean starting = c.getFrom() == a || c.getFrom() == b;
		boolean ending = c.getTo() == b ||c.getTo() == a;
		return getLinkMinimumLength(along, starting, ending);
	}
			
	private double getAlongMinimumLength(Connected along, Direction d, DiagramElement a, Direction aSide, DiagramElement b, Direction bSide) {
		if ((along == a) && (b instanceof Connection)) {
			// link meeting connected, and we're working out distance to corner.
			return getLinkInset(along, d);
		} else if ((along == b) && (a instanceof Connection)) {
			// link meeting connected, and we're working out distance to corner.
			return getLinkInset(along, d);
		} else if ((a instanceof Connection) && (b instanceof Connection)) {
			// the gutter space between two connections arriving on a side
			Terminator startA = ((Connection)a).meets(along) ? ((Connection)a).getDecorationForEnd(along) : null;
			Terminator startB = ((Connection)b).meets(along) ? ((Connection)b).getDecorationForEnd(along) : null;
			return getLinkGutter(along, startA, aSide, startB, bSide);
		} else {
			// sides of a rectangle or something
			return 0;
		} 
	}

	private double getMinimumDistanceRectangularToRectangular(Rectangular a, Direction aSide, Rectangular b, Direction bSide, Direction d, DiagramElement along, boolean concave) {
		// distances when one element is contained within another
		double length;
		if (a == b) {
			length =  getInternalDistance(a, aSide, bSide); 
		} else if (isImmediateParent(b, a)) {
			length = Math.max(getPadding(a, aSide), getMargin(b, bSide));
		} else if (isImmediateParent(a, b)) {
			length = Math.max(getPadding(b, bSide), getMargin(a, aSide));
		} else if (concave) {
			if (aSide == bSide) {
				// not facing each other
				length = 0;
			} else {
				// no containment, just near each other
				length = calculateMargin(a, aSide, b, bSide);
			}
		} else {
			length = 0;
		}
		
		return incorporateAlongMinimumLength(along, d, length,a, aSide, b, bSide);
	}

	protected boolean isImmediateParent(DiagramElement a, DiagramElement parent) {
		return (((Rectangular)a).getParent() == parent);
				 // || (parent instanceof Container) && ((Container)parent).getContents().contains(a) || 
	}
	
	protected boolean isEventualParent(DiagramElement d, DiagramElement parent) {
		if (d == null) {
			return false;
		} else if (d.getParent() == parent) {
			return true;
		} else {
			return isEventualParent(d.getParent(), parent);
		}
		
	}

	private double getMinimumDistanceConnectionToConnection(Connection a, Direction aSide, Connection b, Direction bSide, Direction d, DiagramElement along, boolean concave) {
		if ((a == b)) {
			return 0;
			
		}
		double margin = concave ? calculateMargin(a, aSide, b, bSide) : 0;
		margin = incorporateAlongMinimumLength(along, d, margin, a, aSide, b, bSide);
		return margin;
	}

	private double calculateMargin(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide) {
		double marginA = getMargin(a, aSide);
		double marginB = getMargin(b, bSide);
		double margin = Math.max(marginA, marginB);
		return margin;
	}

	private double getInternalDistance(DiagramElement a, Direction aSide, Direction bSide) {
		if (a == null) {
			throw new Kite9ProcessingException("Can't get internal distance for null");
		} else if ((aSide == null) || (bSide == null)) {
			throw new Kite9ProcessingException("Don't know sides");
		} else if ((aSide == Direction.LEFT) || (aSide == Direction.RIGHT)) {
			return size(a, CostedDimension.UNBOUNDED).getWidth();
		} else {
			return size(a, CostedDimension.UNBOUNDED).getHeight();
		}

	}
	
	public abstract double getPadding(DiagramElement a, Direction d);
	
	public abstract double getMargin(DiagramElement element, Direction d);
	
	protected abstract CostedDimension size(DiagramElement a, Dimension2D s);
	
	/**
	 * The smallest possible length of element, when the element is starting or ending in the length being considered.
	 * This should include terminators.
	 */
	protected abstract double getLinkMinimumLength(Connection element, boolean starting, boolean ending);
	
	/**
	 * Distance from the edge of a connected element to the connection, minimum.  (Could be increased by terminators)
	 */
	protected abstract double getLinkInset(Connected element, Direction d);
	
	/**
	 * This is the amount of space along the side of "along" that should be reserved between two
	 * connections.   Should also consider the amount of room required for the terminators.
	 */
	protected abstract double getLinkGutter(Connected along, Terminator a, Direction aSide, Terminator b, Direction bSide);

	public String getPrefix() {
		return "DD  ";
	}

	public boolean isLoggingEnabled() {
		return false;
	}
	


}