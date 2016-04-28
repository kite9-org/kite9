package org.kite9.diagram.visualization.display.java2d.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Map;

import org.kite9.diagram.adl.Link;

/**
 * The stylesheet determines the colours and fonts that should be used for rendering
 * all of the different elements of a diagram.
 * 
 * @author robmoffat
 *
 */
public interface Stylesheet {
	
	/**
	 * Returns the name of the stylesheet
	 */
	public String getId();
	
	/* Background */
	
	public Color getShadowColour();
	
	public int getShadowXOffset();
	
	public int getShadowYOffset(); 
	
	public TextStyle getCopyrightStyle();
	
	public Color getWatermarkColour();
	
	/* Glyphs */

	public TextBoxStyle getGlyphBoxStyle();
	
	public FlexibleShape getGlyphDefaultShape();
	
	public TextBoxStyle getGlyphTextLineStyle();
	
	public BoxStyle getGlyphCompositionalShapeStyle();
	
	/* Symbols, general */
		
	public Map<String, FixedShape> getSymbolShapes();
	
	public Map<String, FlexibleShape> getFlexibleShapes();
	
	public TextStyle getSymbolTextStyle();

	public float getInterSymbolPadding();
	
	/* connection body */
	
	public TextBoxStyle getConnectionBodyStyle();
	
	public FlexibleShape getConnectionBodyDefaultShape();
	
	/* Contexts */
	
	public BoxStyle getContextBoxStyle();
	
	public BoxStyle getContextBoxInvisibleStyle();
	
	public FlexibleShape getContextBoxDefaultShape();
	
	/* Context Label */
	
	public TextBoxStyle getContextLabelStyle();
	
	public FlexibleShape getContextLabelDefaultShape();
		
	/* Key */
	
	public TextBoxStyle getKeyBoxStyle();
	
	public FlexibleShape getKeyBoxDefaultShape();
	
	public TextBoxStyle getKeySymbolStyle();

	public int getKeyInternalSpacing();
	
	public BoxStyle getKeyDividerStyle();
	
	/* Connection Labels */

	public TextBoxStyle getConnectionLabelStyle();	
	
	public FlexibleShape getConnectionLabelDefaultShape();
	
	/* Links */

	public Map<String, ShapeStyle> getLinkStyles();
	
	public Map<String, ConnectionTemplate> getConnectionTemplates();
	
	public Map<String, TerminatorShape> getLinkTerminatorStyles();
		
	public float getLinkHopSize();
	
	public String getLinkTerminator(Link l, boolean fromEnd);

	/* Grid */
	
	public int getGridSize();
	
	/* Debug */
	public Font getDebugTextFont();

	public Stroke getDebugLinkStroke();

	/* background */
	public Paint getBackground();

	float getSymbolSize();

	/* Style Chooser */
	public Map<String, ? extends Font> getFontFamilies();
	
	public Map<String, Paint> getBoxFills();
}
