package org.kite9.diagram.adl;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RouteRenderingInformation;

/**
 * A connection is a link between two Connected items within the diagram.  Connections have a notional
 * 'from' and 'to', as well as decorations to show how the links should look.
 */
public interface Connection extends DiagramElement, BiDirectional<Connected> {

	/**
	 * The shape of the end of the edge at the from end
	 */
	public org.kite9.diagram.adl.Terminator getFromDecoration();
	
	/**
	 * The shape of the end of the edge at the to end
	 */
	public org.kite9.diagram.adl.Terminator getToDecoration();
	
	/**
	 * The text written on the from end
	 */
	public Label getFromLabel();
	
	/**
	 * Text on the to end
	 */
	public Label getToLabel();

	
	public RouteRenderingInformation getRenderingInformation();
	
	@Deprecated
	public String getStyle();
	
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
}
