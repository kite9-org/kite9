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

/**
 * This is the stylesheet used in the Kite9 Designer GUI
 * 
 * @author robmoffat
 * 
 */
public class Designer2012Stylesheet extends AbstractADLStylesheet implements Stylesheet {

	private static final LinearGradientPaint KEY_BACKGROUND_GRADIENT = new LinearGradientPaint(new Point2D.Double(.5d,0),  new Point2D.Double(.5d,1), new float[] {0f, 1f}, new Color[] { new Color(0.98f,.98f,.98f), new Color(.88f,.88f,.88f) });
	private static final LinearGradientPaint GLYPH_GRADIENT = new LinearGradientPaint(new Point2D.Double(.5d,0), new Point2D.Double(.5d,1), new float[] { 0f, 1f}, new Color[] {new Color(0.95f,.95f,.95f), new Color(.8f,.8f,.8f) });
	private static final int SMALL_FONT_SIZE = 12;

	private Color getBlack() {
		return Color.BLACK;
	}

	private Color getGrey() {
		return new Color(7 * 16, 7 * 16, 7 * 16);
	}

	private Font getSmallFont() {
		return getFont(getLightFontName(), SMALL_FONT_SIZE);
	}

	private Color getContextColour() {
		return getBlack();
	}

	@Override
	public TextStyle getCopyrightStyle() {
		return new TextStyle(h,getSmallFont(), getBlack(), Justification.CENTER, getLightFontName());
	}

	@Override
	public Font getDebugTextFont() {
		return getFont(getLightFontName(), 8);
	}

	public BoxStyle getGlyphCompositionalShapeStyle() {
		return new BoxStyle(h,
				new DirectionalValues(6, 0, 6, 0), 
				DirectionalValues.ZERO,
				new ShapeStyle(h,new BasicStroke(1f), getGrey(), null, "", false, false),
				false);
	}

	private Font getLargeFont() {
		return getFont(getLightFontName(), 15);
	}

	private String getLightFontName() {
		return "opensans-light-webfont";
	}

	private Font getSmallBoldFont() {
		return getFont(getBoldFontName(), SMALL_FONT_SIZE);
	}

	private String getBoldFontName() {
		return "opensans-bold-webfont";
	}

	@Override
	public int getGridSize() {
		return 6;
	}

	@Override
	public Color getShadowColour() {
		return new Color(.7f, .7f, .7f);
	}

	@Override
	public Color getSymbolBackgroundColor(SymbolShape ss) {
		switch (ss) {
		case CIRCLE:
			return new Color(0, 150, 73);
		case HEXAGON:
			return new Color(207, 36, 42);
		case DIAMOND:
			return new Color(237, 106, 86);

		}

		return new Color(0, 117, 178);
	}

	public TextStyle getSymbolTextStyle() {
		return new TextStyle(h,getFont(getBoldFontName(), 10), Color.WHITE, Justification.CENTER, getBoldFontName());
	}

	@Override
	public Color getWatermarkColour() {
		return getContextColour();
	}

	@Override
	public float getInterSymbolPadding() {
		return 2;
	}

	@Override
	public int getKeyInternalSpacing() {
		return 12;
	}

	@Override
	public float getLinkEndSize() {
		return 10;
	}

	@Override
	public Map<String, ShapeStyle> getLinkStyles() {
		Map<String, ShapeStyle> out= new HashMap<String, ShapeStyle>();
		out.put(LinkLineStyle.DOTTED, new ShapeStyle(h,new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 8.0f, new float[] { 8f, 8f },
					0.0f), Color.BLACK, null, "-", false, true));
		out.put(LinkLineStyle.NORMAL, new ShapeStyle(h,new BasicStroke(2), Color.BLACK, null));
		out.put(LinkLineStyle.INVISIBLE, new ShapeStyle(h,new BasicStroke(.5f), new Color(160, 160, 255), null, "-..", true, false));
		return out;
	}

	@Override
	public float getSymbolSize() {
		return 14;
	}

	@Override
	public Stroke getDebugLinkStroke() {
		return new BasicStroke(1);
	}

	@Override
	public float getLinkHopSize() {
		return 6;
	}
	
	@Override
	public Paint getBackground() {
		return new Color(1f, 1f, 1f); // white overwash
	}

	@Override
	public int getShadowXOffset() {
		return 2;
	}

	@Override
	public int getShadowYOffset() {
		return 2;
	}

	@Override
	public TextBoxStyle getGlyphBoxStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(3, 6, 3, 6), 
				DirectionalValues.ZERO, 
				new ShapeStyle(h,new BasicStroke(1), getBlack(), GLYPH_GRADIENT),
				new TextStyle(h,getLargeFont(), getBlack(), Justification.CENTER, getLightFontName()),
				new TextStyle(h,getSmallBoldFont(), getGrey(), Justification.CENTER, getBoldFontName()),
				true);
	}
	
	@Override
	public FlexibleShape getGlyphDefaultShape() {
		return new RoundedRectFlexibleShape(12, 0, 0);
	}

	@Override
	public TextBoxStyle getGlyphTextLineStyle() {
		return new TextBoxStyle(h,
				DirectionalValues.ZERO, 
				DirectionalValues.ZERO,
				null, 
				new TextStyle(h,getSmallFont(), getBlack(), Justification.LEFT, getLightFontName()),
				new TextStyle(h,getSmallBoldFont(), getBlack(), Justification.LEFT, getBoldFontName()),
				false);

	}

	@Override
	public TextBoxStyle getConnectionBodyStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 3, 2, 3), 
				DirectionalValues.ZERO,
				new ShapeStyle(h, null, null, Color.BLACK),
				new TextStyle(h,getSmallFont(), getWhite(), Justification.CENTER, getLightFontName()),
				new TextStyle(h,getSmallFont(), getWhite(), Justification.CENTER, getLightFontName()),
				true);
	}
	
	

	@Override
	public FlexibleShape getConnectionBodyDefaultShape() {
		return 				new RoundedRectFlexibleShape(8);
	}

	@Override
	public BoxStyle getContextBoxStyle() {
		return new BoxStyle(h,
				DirectionalValues.ZERO, 
				DirectionalValues.ZERO,
				new ShapeStyle(h,new BasicStroke(1f), getBlack(), null, "", false, false),
				false);
	}
	
	@Override
	public FlexibleShape getContextBoxDefaultShape() {
		return new RoundedRectFlexibleShape(12);
	}

	@Override
	public TextBoxStyle getContextLabelStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 2, 2, 2), 
				new DirectionalValues(2, 5, 2, 5),
				new ShapeStyle(h,null, null,  new Color(0f, 0f, 0f, 0.1f), "", false, false),
				new TextStyle(h,getSmallFont(), getBlack(), Justification.LEFT, getLightFontName()),
				new TextStyle(h,getSmallFont(), getBlack(), Justification.LEFT, getLightFontName()),
				false);
	}

	@Override
	public FlexibleShape getContextLabelDefaultShape() {
		return new RoundedRectFlexibleShape(8);
	}

	@Override
	public TextBoxStyle getKeyBoxStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(0, 24, 0, 24), 
				new DirectionalValues(12, 12, 12, 12),
				new ShapeStyle(h,new BasicStroke(.5f), 
						getBlack(),
						KEY_BACKGROUND_GRADIENT),
				new TextStyle(h,getSmallFont(), getBlack(), Justification.CENTER, getLightFontName()),
				new TextStyle(h,getSmallBoldFont(), getBlack(), Justification.CENTER, getBoldFontName()),
				true);
	}
	
	

	@Override
	public FlexibleShape getKeyBoxDefaultShape() {
		return new RoundedRectFlexibleShape(0, 12, 12);
	}

	@Override
	public TextBoxStyle getKeySymbolStyle() {
		return new TextBoxStyle(h,
				DirectionalValues.ZERO, 
				DirectionalValues.ZERO,
				null, 
				new TextStyle(h,getSmallFont(), getGrey(), Justification.LEFT, getLightFontName()),
				new TextStyle(h,getSmallFont(), getGrey(), Justification.LEFT, getLightFontName()),
				false);
	}
	
	@Override
	public BoxStyle getKeyDividerStyle() {
		return new BoxStyle(h,
				new DirectionalValues(6, 0 , 6, 0), 
				DirectionalValues.ZERO,
				new ShapeStyle(h,new BasicStroke(.5f), new Color(.7f, .7f, .7f), null, "", false, false),
				false);
	}

	@Override
	public TextBoxStyle getConnectionLabelStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 2, 2, 2), 
				new DirectionalValues(2, 10, 8, 2),
				new ShapeStyle(h,null, null,  new Color(0f, 0f, 0f, 0.1f), "", false, false),
				new TextStyle(h,getSmallFont(), getBlack(), Justification.LEFT, getLightFontName()),
				new TextStyle(h,getSmallFont(), getBlack(), Justification.LEFT, getLightFontName()),
				false);
	}
	
	

	@Override
	public FlexibleShape getConnectionLabelDefaultShape() {
		return new RoundedRectFlexibleShape(8);
	}

	private Color getWhite() {
		return Color.WHITE;
	}

	@Override
	protected double getSymbolPadding() {
		return 2;
	}


	@Override
	protected Map<String, LocalFont> getFontsInternal() {
		Map<String, LocalFont> out = new HashMap<String, LocalFont>();
		out.put(getBoldFontName(), getFont(getBoldFontName(), 1));
		out.put(getLightFontName(), getFont(getLightFontName(), 1));
		return out;
	}

	@Override
	public String getId() {
		return "designer2012";
	}

	
}
