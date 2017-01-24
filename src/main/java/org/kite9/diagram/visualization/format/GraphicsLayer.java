package org.kite9.diagram.visualization.format;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Dimension2D;

public interface GraphicsLayer {

	/**
	 * Use this to indicate we have started processing a diagram element.
	 */
	void startGroup(DiagramElement de);

	/**
	 * Use this to indicate end of processing.
	 */
	void endGroup(DiagramElement de);

	/**
	 * Works out the size of the string, s, given the styling of de.
	 */
	public Dimension2D getStringBounds(DiagramElement de, String s);
	
	/**
	 * Putting Stuff On the Screen
	 */
	void fill(Shape f);

	void draw(Shape s);

	void dispose();

}