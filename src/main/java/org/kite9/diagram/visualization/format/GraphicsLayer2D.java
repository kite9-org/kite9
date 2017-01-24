package org.kite9.diagram.visualization.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * This contains an interface version of the Graphics2D class, containing just the subset of 
 * methods we use, plus methods for indicating what is being drawn.
 * 
 * @author robmoffat
 *
 */
public interface GraphicsLayer2D extends GraphicsLayer {
	
	
	
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
	 * Call when done
	 */


}
