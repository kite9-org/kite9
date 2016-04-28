package org.kite9.diagram.visualization.display.java2d.style.sheets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.adl.LinkLineStyle;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.visualization.display.java2d.adl_basic.AbstractADLDisplayer.Justification;
import org.kite9.diagram.visualization.display.java2d.style.BoxStyle;
import org.kite9.diagram.visualization.display.java2d.style.DirectionalValues;
import org.kite9.diagram.visualization.display.java2d.style.FlexibleShape;
import org.kite9.diagram.visualization.display.java2d.style.LocalFont;
import org.kite9.diagram.visualization.display.java2d.style.ShapeStyle;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.diagram.visualization.display.java2d.style.TextBoxStyle;
import org.kite9.diagram.visualization.display.java2d.style.TextStyle;
import org.kite9.diagram.visualization.display.java2d.style.shapes.RoundedRectFlexibleShape;

public class BasicStylesheet extends AbstractADLStylesheet implements Stylesheet {

	
	public TextStyle getCopyrightStyle() {
		return new TextStyle(h, getFont(getPlainFont(), 13), COPYRIGHT_TEXT, Justification.CENTER, getPlainFontName());
	}

	protected String getPlainFontName() {
		return "Arial";
	}

	
	@Override
	protected float getLinkGapSize() {
		return 3;
	}


	public static final Color COPYRIGHT_TEXT = new Color(130, 130, 130);

	public Color getWatermarkColour() {
		return getDarkColour();
	}

	protected Color getDarkColour() {
		return Color.BLACK;
	}

	protected String getPlainFont() {
		return getPlainFontName();
	}

	protected String getBoldFont() {
		return "Arial Bold";
	}
	
	@Override
	protected Map<String, LocalFont> getFontsInternal() {
		Map<String, LocalFont> out = new HashMap<String, LocalFont>();
		out.put(getBoldFont(), getFont(getBoldFont(), 1));
		out.put(getPlainFont(), getFont(getPlainFont(), 1));
		return out;
	}

	protected String getBoldFontName() {
		return "Arial Bold";
	}

	protected Color getLightColour() {
		return Color.WHITE;
	}

	public Color getShadowColour() {
		return new Color(.5f, .5f, .5f, .3f);
	}

	public Color getSymbolBackgroundColor(SymbolShape ss) {
		return getDarkColour();
	}

	private static final Color DARKEST_BACKGROUND = new Color(210, 210, 210);
	private static final Color LIGHTEST_BACKGROUND = new Color(250, 250, 250);

	protected Color getBackgroundGradientCenterColour() {
		return LIGHTEST_BACKGROUND;
	}

	protected Color getBackgroundGradientEdgeColour() {
		return DARKEST_BACKGROUND;
	}

	public final Color MID_COLOUR = new Color(50, 50, 50);

	
	
	protected Color getMidColour() {
		return MID_COLOUR;
	}

	public final Color GLYPH_DIVIDER_COLOUR = new Color(50, 50, 50);

	public TextStyle getSymbolTextStyle() {
		return new TextStyle(h, getFont(getPlainFont(), 12), Color.WHITE, Justification.CENTER, getPlainFontName());
	}
	
	public final static BasicStroke BORDER_STROKE = new BasicStroke(2f);
	public final static BasicStroke CONTEXT_BORDER_STROKE = new BasicStroke(2f);

	@Override
	public BoxStyle getGlyphCompositionalShapeStyle() {
		return new BoxStyle(h,
				new DirectionalValues(5, 0, 5, 0),
				DirectionalValues.ZERO,
				new ShapeStyle(h, getGlyphDividerStroke(), getGlyphDividerColour(),  null, "", true, false), false);
	}
	
	public BoxStyle getKeyDividerStyle() {
		return getGlyphCompositionalShapeStyle();
	}

	protected Stroke getGlyphDividerStroke() {
		return BORDER_STROKE;
	}

	protected Color getGlyphDividerColour() {
		return getDarkColour();
	}


	@Override
	public Font getDebugTextFont() {
		return getFont(getPlainFont(), 8);
	}

	@Override
	public int getGridSize() {
		return 10;
	}

	@Override
	public float getInterSymbolPadding() {
		return 2;
	}

	@Override
	public float getSymbolSize() {
		return 15;
	}
	
	protected double getSymbolPadding() {
		return 1;
	}

	public Map<String, ShapeStyle> getLinkStyles() {
		 Map<String, ShapeStyle> out = new HashMap<String, ShapeStyle>();
		 out.put(LinkLineStyle.DOTTED, 
				 new ShapeStyle(h,
						 new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 10f, 10f },
							0.0f),
				getDarkColour(), null));
		 
		 out.put(LinkLineStyle.NORMAL, new ShapeStyle(h,new BasicStroke(3), getDarkColour(), null));
		out.put(LinkLineStyle.INVISIBLE, new ShapeStyle(h,new BasicStroke(2), Color.GREEN, null, "..-", true, false));
		 return out;
		
	}

	@Override
	public float getLinkEndSize() {
		return 8;
	}

	@Override
	public Stroke getDebugLinkStroke() {
		return new BasicStroke(1);
	}

	@Override
	public float getLinkHopSize() {
		return 5;
	}


	@Override
	public int getShadowXOffset() {
		return 3;
	}

	@Override
	public int getShadowYOffset() {
		return 3;
	}

	@Override
	public TextBoxStyle getGlyphBoxStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(5, 10, 5, 10),
				DirectionalValues.ZERO,
				new ShapeStyle(h,new BasicStroke(2), getDarkColour(), getGlyphBackgroundColour()),
				new TextStyle(h,getFont(getPlainFont(), 16), getDarkColour(), Justification.CENTER, getPlainFontName()),
				new TextStyle(h,getFont(getBoldFont(), 15), getDarkColour(), Justification.CENTER, getBoldFontName()),
				true);
	}

	@Override
	public FlexibleShape getGlyphDefaultShape() {
		return new RoundedRectFlexibleShape(20, 0, 0);
	}

	protected Paint getGlyphBackgroundColour() {
		return new Color(1f, 1f,1f,1f);
	}

	@Override
	public TextBoxStyle getGlyphTextLineStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 0, 0, 0),
				new DirectionalValues(0, 0, 0, 0), 
				new ShapeStyle(h,null, null, null),  
				new TextStyle(h,getFont(getPlainFont(), 13), getDarkColour(), Justification.LEFT, getPlainFontName()),
				new TextStyle(h,getFont(getPlainFont(), 13), getDarkColour(), Justification.LEFT, getPlainFontName()), false);
	}

	@Override
	public TextBoxStyle getConnectionBodyStyle() {
		return new TextBoxStyle(h,
				DirectionalValues.ZERO,
				//new DirectionalValues(3, 3, 3, 3),
				new DirectionalValues(0, 0, 0, 0), 
				new ShapeStyle(h,null, null, getDarkColour()),
				new TextStyle(h,getFont(getPlainFont(), 12), Color.WHITE, Justification.CENTER, getPlainFontName()),
				null,
				true);
	}
	
	@Override
	public FlexibleShape getConnectionBodyDefaultShape() {
		return new RoundedRectFlexibleShape(8);
	}

	@Override
	public TextBoxStyle getConnectionLabelStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(0, 10, 0, 5),
				new DirectionalValues(0, 0, 0, 0), 
				new ShapeStyle(h,null,null, null, "", false, false),
				new TextStyle(h,getFont(getPlainFont(), 12), getDarkColour(), Justification.LEFT, getPlainFontName()),
				null,
				false);
	}
	
	@Override
	public FlexibleShape getConnectionLabelDefaultShape() {
		return new RoundedRectFlexibleShape(3);
	}

	@Override
	public TextBoxStyle getContextLabelStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(13, 3, 3, 3),
				new DirectionalValues(0, 0, 0, 0),
				new ShapeStyle(h,null, null, getLightColour(), "", false, false),
				new TextStyle(h,getFont(getPlainFont(), 12), getDarkColour() , Justification.CENTER, getPlainFontName()), 
				null, false);
	}
	
	@Override
	public FlexibleShape getContextLabelDefaultShape() {
		return null;
	}

	@Override
	public TextBoxStyle getKeyBoxStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(10, 10, 10, 10),
				new DirectionalValues(15, 15, 15, 15), 
				new ShapeStyle(h,new BasicStroke(2), getDarkColour(), getLightColour()), 
				new TextStyle(h,getFont(getPlainFont(), 13), getDarkColour(), Justification.LEFT, getPlainFontName()),
				new TextStyle(h,getFont(getBoldFont(), 13), getDarkColour(), Justification.CENTER, getBoldFontName()), true);
	}
	
	
	
//	@Override
//	public ShapeStyle getKeyDividerStyle() {
//		return new ShapeStyle(BORDER_STROKE, getMidColour(), false, null, 2); //, new DirectionalValues(8, 0, 8, 0));
//	}


	@Override
	public FlexibleShape getKeyBoxDefaultShape() {
		return new RoundedRectFlexibleShape(0, 15, 15); 
	}

	@Override
	public TextBoxStyle getKeySymbolStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 0, 0, 0),
				new DirectionalValues(0, 0, 0, 0), 
				new ShapeStyle(h,null, null, null, "", false, false),
				new TextStyle(h,getFont(getPlainFont(), 13), getDarkColour(), Justification.LEFT, getPlainFontName()),
				new TextStyle(h,getFont(getPlainFont(), 13), getDarkColour(), Justification.LEFT, getPlainFontName()), false);
	}

	@Override
	public BoxStyle getContextBoxStyle() {
		return new BoxStyle(h,new DirectionalValues(0, 0, 0, 0),
				new DirectionalValues(0, 0, 0, 0),
				new ShapeStyle(h, CONTEXT_BORDER_STROKE, getContextBorderColour(), null,"", false, false), false);
	}

	@Override
	public FlexibleShape getContextBoxDefaultShape() {
		return 	new RoundedRectFlexibleShape(20);
	}

	protected Color getContextBorderColour() {
		return getMidColour();
	}

	@Override
	public int getKeyInternalSpacing() {
		return 10;
	}

	@Override
	public Paint getBackground() {
		return new LinearGradientPaint(new Point2D.Double(.5, 0), new Point2D.Double(.5, 1), new float[] {0, 0.5f, 1},
				new Color[] {
					getBackgroundGradientEdgeColour(),
					getBackgroundGradientCenterColour(), 
					getBackgroundGradientEdgeColour()});
	}

	@Override
	public String getId() {
		return "basic";
	}
	

}
