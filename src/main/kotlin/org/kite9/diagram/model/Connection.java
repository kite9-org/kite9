package org.kite9.diagram.model;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RouteRenderingInformation;

/**
 * A connection is a link between two Connected items within the diagram.  Connections have a notional
 * 'from' and 'to', as well as decorations to show how the links should look.
 */
public interface Connection extends DiagramElement, BiDirectional<Connected> {

	/**
	 * The shape of the end of the edge at the from end
	 */
	public Terminator getFromDecoration();
	
	/**
	 * The shape of the end of the edge at the to end
	 */
	public Terminator getToDecoration();
	
	public Terminator getDecorationForEnd(DiagramElement end);
	
	/**
	 * The text written on the from end
	 */
	public Label getFromLabel();
	
	/**
	 * Text on the to end
	 */
	public Label getToLabel();

	
	public RouteRenderingInformation getRenderingInformation();

	/**
	 * Returns the rank of the connection from the ordering of all the connections on the diagram.
	 */
	public int getRank();
	
	/**
	 * Margin is the minimum distance from this connection to an element it is not connected with.
	 */
	public double getMargin(Direction d);
	
	/**
	 * Padding is the space above or below the end of the connection such that it doesn't join
	 * the corner of the element it connects to.
	 */
	public double getPadding(Direction d);

	/**
	 * Smallest length this connection can have (when terminators are zero-size)
	 */
	public double getMinimumLength();
	
	/**
	 * Arc radius used for corners and hops
	 */
	public double getCornerRadius();
	
	public Direction getFromArrivalSide();
	
	public Direction getToArrivalSide();
 
}