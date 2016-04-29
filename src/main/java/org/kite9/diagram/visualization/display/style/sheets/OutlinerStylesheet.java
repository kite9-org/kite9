package org.kite9.diagram.visualization.display.style.sheets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.adl.LinkLineStyle;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.visualization.display.components.AbstractADLDisplayer.Justification;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.DirectionalValues;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.HatchStroke;
import org.kite9.diagram.visualization.display.style.LocalFont;
import org.kite9.diagram.visualization.display.style.ShapeStyle;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.display.style.TextBoxStyle;
import org.kite9.diagram.visualization.display.style.TextStyle;
import org.kite9.diagram.visualization.display.style.shapes.RoundedRectFlexibleShape;

public class OutlinerStylesheet extends AbstractADLStylesheet implements Stylesheet {

	private static final int SMALL_FONT_SIZE = 12;

	private Color getBlue() {
		return new Color(87, 116, 131);
	}

	private Font getSmallFont() {
		return getFont("Gotham Book", SMALL_FONT_SIZE);
	}

	private Color getContextColour() {
		return new Color(205, 209, 211);
	}

	@Override
	public Font getDebugTextFont() {
		return getFont("Gotham Book", 8);
	}	
	
	public String getMediumFontName() {
		return "Gotham Medium";
	}
	
	public String getBookFontName() {
		return "Gotham Book";
	}

	private Font getLargeFont() {
		return getFont("Gotham Book", 19);
	}

	private Font getSmallBoldFont() {
		return getFont("Gotham Medium", SMALL_FONT_SIZE);
	}

	@Override
	public int getGridSize() {
		return 12;
	}

	@Override
	public Color getShadowColour() {
		return Color.WHITE;
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

	

	@Override
	public Color getWatermarkColour() {
		return getContextColour();
	}

	@Override
	public float getInterSymbolPadding() {
		return 2;
	}

	@Override
	public float getLinkEndSize() {
		return 8;
	}

	

	@Override
	public float getSymbolSize() {
		return 18;
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
	public float getLinkGapSize() {
		return 5;
	}

	@Override
	public Paint getBackground() {
		return Color.WHITE;
	}

	@Override
	public int getShadowXOffset() {
		return 0;
	}

	@Override
	public int getShadowYOffset() {
		return 0;
	}

	@Override
	public TextBoxStyle getGlyphBoxStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(6, 6, 6, 6),
				new DirectionalValues(0, 0, 0, 0),
				new ShapeStyle(h,
					new HatchStroke(
							new BasicStroke(5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER),
							new BasicStroke(2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER),
							5, 1 ),
					new Color(159, 188, 46), 
					null),
				new TextStyle(h,getLargeFont(), Color.BLACK, Justification.CENTER, getBookFontName()),
				new TextStyle(h,getFont("Gotham Medium", 15), getBlue(), Justification.CENTER, getMediumFontName()), true);
	}
	
	

	@Override
	public FlexibleShape getGlyphDefaultShape() {
		return getFlexibleShape(null);
	}

	@Override
	public TextBoxStyle getGlyphTextLineStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 0, 0, 0), 
				new DirectionalValues(0,0,0,0),
				null,
				new TextStyle(h,getSmallFont(), getBlue(), Justification.LEFT, getBookFontName()),
				new TextStyle(h,getSmallFont(), getBlue(), Justification.LEFT, getBookFontName()), false);
	}

	@Override
	public TextBoxStyle getConnectionBodyStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(0, 0, 0, 0), 
				new DirectionalValues(0, 0, 0, 0), 
				new ShapeStyle(h,null, null, getBlue()), 
				new TextStyle(h,getSmallFont(), Color.WHITE, Justification.CENTER, getBookFontName()),
				new TextStyle(h,getSmallBoldFont(), Color.WHITE, Justification.CENTER, getMediumFontName()), true);
	}
	
	@Override
	public FlexibleShape getConnectionBodyDefaultShape() {
		return new RoundedRectFlexibleShape(8);
	}

	@Override
	public BoxStyle getContextBoxStyle() {
		return new BoxStyle(h,
				new DirectionalValues(0, 0, 0, 0),
				new DirectionalValues(0, 0, 0, 0),
				new ShapeStyle(h,new BasicStroke(2f),getContextColour(), null), false);
	}
	
	@Override
	public FlexibleShape getContextBoxDefaultShape() {
		return new RoundedRectFlexibleShape(20);
	}

	@Override
	public TextBoxStyle getContextLabelStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 4, 2, 4), 
				new DirectionalValues(4, 4, 4, 4), 
				null,
				new TextStyle(h,getSmallBoldFont(),getContextColour(), Justification.CENTER, getMediumFontName()),
				new TextStyle(h,getSmallBoldFont(),getContextColour(), Justification.CENTER, getMediumFontName()), false);
	}
	
	@Override
	public FlexibleShape getContextLabelDefaultShape() {
		return null;
	}

	@Override
	public TextBoxStyle getKeySymbolStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 0, 0, 0), 
				new DirectionalValues(0, 0, 0, 0), 
				null,
				new TextStyle(h,getSmallFont(), getBlue(), Justification.LEFT, getBookFontName()),
				new TextStyle(h,getSmallFont(), getBlue(), Justification.LEFT, getBookFontName()), false);
	}

	@Override
	public TextBoxStyle getKeyBoxStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(16, 16, 16, 16), 
				new DirectionalValues(16, 16, 16, 16), 
				new ShapeStyle(h,
					new BasicStroke(5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER),
					getContextColour(), Color.WHITE),
				new TextStyle(h,getSmallFont(), getBlue(), Justification.CENTER, getBookFontName()),
				new TextStyle(h,getSmallBoldFont(), getBlue(), Justification.CENTER, getMediumFontName()), true);
	}
	
	@Override
	public FlexibleShape getKeyBoxDefaultShape() {
		return new RoundedRectFlexibleShape(0, 16, 16);
	}

	@Override
	public TextBoxStyle getConnectionLabelStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 2, 2, 2),
				new DirectionalValues(0, 3, 5, 2),
				null, 
				new TextStyle(h,getSmallFont(), getBlue(), Justification.LEFT, getBookFontName()),
				null, false);
	}

	@Override
	public int getKeyInternalSpacing() {
		return 16;
	}

	@Override
	public TextStyle getCopyrightStyle() {
		return new TextStyle(h,getSmallFont(), getBlue(), Justification.CENTER, getBookFontName());
	}
	
	public BoxStyle getGlyphCompositionalShapeStyle() {
		return new BoxStyle(h,
				new DirectionalValues(8, 0, 8, 0), 
				DirectionalValues.ZERO,
				null, false);
	}
	
	@Override
	public TextStyle getSymbolTextStyle() {
		return new TextStyle(h, getFont("Gotham Medium", 10), Color.WHITE, Justification.CENTER, getMediumFontName());
	}

	public BoxStyle getKeyDividerStyle() {
		return  null;
	}
	
	@Override
	public Map<String, ShapeStyle> getLinkStyles() {
		Map<String, ShapeStyle> out= new HashMap<String, ShapeStyle>();
		out.put(LinkLineStyle.DOTTED, new ShapeStyle(h,new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 8.0f, new float[] { 8f, 8f },
					0.0f), getBlue(), null,  ".", false, true));
		out.put(LinkLineStyle.NORMAL, new ShapeStyle(h,new BasicStroke(2), getBlue(), null));
		out.put(LinkLineStyle.INVISIBLE, new ShapeStyle(h,new BasicStroke(2), Color.GREEN, null,"..-", true, false));
		return out;
	}

	@Override
	protected double getSymbolPadding() {
		return 2;
	}

	@Override
	public FlexibleShape getConnectionLabelDefaultShape() {
		return null;
	}

	@Override
	protected Map<String, LocalFont> getFontsInternal() {
		return null;
	}

	@Override
	public String getId() {
		return "outline";
	}

	
}
