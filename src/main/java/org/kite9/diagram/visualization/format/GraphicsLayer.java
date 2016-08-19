package org.kite9.diagram.visualization.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.style.DiagramElement;

/**
 * This contains an interface version of the Graphics2D class, containing just the subset of 
 * methods we use, plus methods for indicating what is being drawn.
 * 
 * @author robmoffat
 *
 */
public interface GraphicsLayer {
	
	/**
	 * Use this to indicate we have started processing a diagram element.
	 */
	public void startElement(DiagramElement de);
	
	/**
	 * Use this to indicate end of processing.
	 */
	public void endElement(DiagramElement de);
	
	/* Font Things */
	
	public void setFont(Font font);

	public FontMetrics getFontMetrics(Font font);

	public FontMetrics getFontMetrics();

	public FontRenderContext getFontRenderContext();
	
	public Rectangle2D getStringBounds(FontMetrics fm, String s);
	
	public void outputText(Font font, double y, double x, String line);

	/* Affine Transform */
	
	public AffineTransform getTransform();

	void setTransform(AffineTransform at);

	public void translate(double x, double y);

	public void scale(double x, double y);
	
	/*
	 * Colour / Paint
	 */

	public void setStroke(Stroke s);
	
	public void setColor(Color c);
	
	public void setPaint(Paint p);

	public Paint getPaint();

	/*
	 * Putting Stuff On the Screen
	 */
	void fill(Shape f);

	void draw(Shape s);
	
	/* 
	 * Call when done
	 */

	public void dispose();


}
