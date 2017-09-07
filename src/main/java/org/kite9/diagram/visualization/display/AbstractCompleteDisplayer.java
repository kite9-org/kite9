package org.kite9.diagram.visualization.display;

import org.kite9.diagram.common.elements.mapping.GeneratedLayoutConnection;
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
			length = getMinimumDistanceRectangularToConnection((Rectangular) a, (Connection) b, d, along);
		} else if ((a instanceof Connection) && (b instanceof Rectangular)) {
			length = getMinimumDistanceRectangularToConnection((Rectangular) b, (Connection) a, Direction.reverse(d), along);
		} else {
			throw new Kite9ProcessingException("Don't know how to calc min distance");
		}
		
		return length;
	}

	private double getMinimumDistanceRectangularToConnection(Rectangular a, Connection b, Direction d, DiagramElement along) {
		// we are inside a, so use the padding distance
		double inset = getLinkInset(a, d);
		double margin = getMargin(b, Direction.reverse(d));
		double length = Math.max(inset, margin);
		return incorporateLinkMinimumLength(along, d, length);
	}

	private double incorporateLinkMinimumLength(DiagramElement along, Direction d, double in) {
		if (along instanceof Connection) {
			return Math.max(in, getLinkMinimumLength((Connection) along));
		}
		
		return in;
	}

	private double getMinimumDistanceRectangularToRectangular(Rectangular a, Direction aSide, Rectangular b, Direction bSide, Direction d, DiagramElement along, boolean concave) {
		// distances when one element is contained within another
		double length;
		if (a == b) {
			length =  getInternalDistance(a, aSide, bSide); 
		} else if ((a instanceof Container) && (((Container) a).getContents().contains(b))) {
			length = getPadding(a, d);
		} else if ((b instanceof Container) && (((Container) b).getContents().contains(a))) {
			length = getPadding(b, Direction.reverse(d));
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
		
		return incorporateLinkMinimumLength(along, d, length);
	}

	private double getMinimumDistanceConnectionToConnection(Connection a, Direction aSide, Connection b, Direction bSide, Direction d, DiagramElement along, boolean concave) {
		if ((a == b)) {
			return 0;
			
		}
		double margin = concave ? calculateMargin(a, aSide, b, bSide) : 0;
		margin = incorporateLinkMinimumLength(along, d, margin);
		return margin;
	}

	private double calculateMargin(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide) {
		double marginA = getMargin(a, aSide);
		double marginB = getMargin(b, bSide);
		double margin = Math.max(marginA, marginB);
		return margin;
	}

	protected abstract double getPadding(DiagramElement a, Direction d);

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
	
	protected abstract CostedDimension size(DiagramElement a, Dimension2D s);

	public abstract double getLinkGutter(Rectangular element, Direction d);
	
	public abstract double getLinkInset(Rectangular element, Direction d);
	
	public abstract double getLinkMinimumLength(Connection element);

	public String getPrefix() {
		return "DD  ";
	}

	public boolean isLoggingEnabled() {
		return true;
	}
	
	public abstract double getMargin(DiagramElement element, Direction d);

	public abstract double getTerminatorLength(Terminator terminator);
	
	public abstract double getTerminatorReserved(Terminator terminator, Connection on);
	
	

}