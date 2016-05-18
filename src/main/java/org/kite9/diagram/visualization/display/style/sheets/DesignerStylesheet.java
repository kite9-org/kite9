package org.kite9.diagram.visualization.display.style.sheets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.kite9.diagram.adl.LinkLineStyle;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.visualization.display.components.AbstractADLDisplayer.Justification;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.DirectionalValues;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.LocalFont;
import org.kite9.diagram.visualization.display.style.ShapeStyle;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.display.style.TextBoxStyle;
import org.kite9.diagram.visualization.display.style.TextStyle;
import org.kite9.diagram.visualization.display.style.shapes.RoundedRectFlexibleShape;
import org.kite9.framework.logging.LogicException;

/**
 * This is the stylesheet used in the Kite9 Designer GUI
 * 
 * @author robmoffat
 * 
 */
public class DesignerStylesheet extends AbstractADLStylesheet implements Stylesheet {

	@Override
	public String getId() {
		return "designer";
	}
	
	private static final int SMALL_FONT_SIZE = 12;

	private Color getBlack() {
		return Color.BLACK;
	}

	private Color getGrey() {
		return new Color(7 * 16, 7 * 16, 7 * 16);
	}

	private Font getSmallFont() {
		return getFont(getRegularFontName(), SMALL_FONT_SIZE);
	}

	private String getRegularFontName() {
		return "opensans-light-webfont";
	}

	private Color getContextColour() {
		return Color.BLACK;
	}

	@Override
	public TextStyle getCopyrightStyle() {
		return new TextStyle(h,getSmallFont(), getBlack(), Justification.CENTER, getRegularFontName());
	}

	@Override
	public Font getDebugTextFont() {
		return getFont(getRegularFontName(), 8);
	}
	
	public BoxStyle getGlyphCompositionalShapeStyle() {
		return new BoxStyle(h,
				new DirectionalValues(8, 0, 8, 0), 
				DirectionalValues.ZERO,
				new ShapeStyle(h,new BasicStroke(1f), getGrey(), null, "", false, true),
				false);
	}

	private Font getLargeFont() {
		return getFont(getRegularFontName(), 15);
	}

	private Font getSmallBoldFont() {
		return getFont(getBoldFontName(), SMALL_FONT_SIZE);
	}
	
	
	@Override
	protected Map<String, LocalFont> getFontsInternal() {
		Map<String, LocalFont> out = new HashMap<String, LocalFont>();
		out.put(getRegularFontName(), getFont(getRegularFontName(), 1));
		out.put(getBoldFontName(), getFont(getBoldFontName(), 1));
		return out;
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
		return new Color(0f, 0f, 0f, 0.2f);
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
		return new TextStyle(h,getFont(getRegularFontName(), 10), Color.WHITE,
				Justification.CENTER, getRegularFontName());
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
		return 8;
	}

	public Map<String, ShapeStyle> getLinkStyles() {
		Map<String, ShapeStyle> out = new HashMap<String, ShapeStyle>();
		out.put(LinkLineStyle.DOTTED, new ShapeStyle(h,new BasicStroke(1,
				BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
				new float[] { 10f, 10f }, 0.0f), getBlack(), null));

		out.put(LinkLineStyle.NORMAL, new ShapeStyle(h,new BasicStroke(3),
				getBlack(), null));
		out.put(LinkLineStyle.INVISIBLE, new ShapeStyle(h,new BasicStroke(2), Color.GREEN, null, "..-", true, false));
		return out;

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
	public Paint getBackground() {
		try {
			BufferedImage image = ImageIO.read(getClass().getResourceAsStream(
					"/stylesheets/designer/background-texture-lt-01.png"));
			Rectangle2D r = new Rectangle2D.Double(0, 0, image.getWidth(),
					image.getHeight());
			TexturePaint tp = new TexturePaint(image, r);
			return tp;
		} catch (IOException e) {
			throw new LogicException("Couldn't get image file");
		}

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
				new DirectionalValues(6, 12, 6, 12),
				new DirectionalValues(0, 0, 0, 0), 
				new ShapeStyle(h,new BasicStroke(1), 
						Color.BLACK, Color.WHITE),
				new TextStyle(h,getLargeFont(), Color.BLACK, Justification.CENTER, getRegularFontName()),
				new TextStyle(h,getFont(getRegularFontName(), 13), Color.BLACK, Justification.CENTER, getRegularFontName()), true);
	}

	@Override
	public FlexibleShape getGlyphDefaultShape() {
		return getFlexibleShape(null);
	}

	@Override
	public TextBoxStyle getGlyphTextLineStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(0, 0, 0, 0),
				new DirectionalValues(0, 0, 0, 0), 
				null, 
				new TextStyle(h,getSmallFont(), getGrey(), Justification.LEFT, getRegularFontName()),
				null, false);
	}

	@Override
	public TextBoxStyle getConnectionBodyStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 3, 2, 3),
				new DirectionalValues(0, 0, 0, 0), 
				new ShapeStyle(h,null, null, Color.BLACK),
				new TextStyle(h,getSmallFont(), Color.WHITE, Justification.CENTER, getRegularFontName()),
				null, true);
	}
	
	

	@Override
	public FlexibleShape getConnectionBodyDefaultShape() {
		return new RoundedRectFlexibleShape(8);
	}

	@Override
	public BoxStyle getContextBoxStyle() {
		return new BoxStyle(h,new DirectionalValues(0, 0, 0, 0),
				new DirectionalValues(0, 0, 0, 0), 
				new ShapeStyle(h,new BasicStroke(1f), Color.BLACK, null, "", false, false), false);
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
				new ShapeStyle(h,null, null, new Color(0f, 0f, 0f, 0.2f), "", false, false), 
				new TextStyle(h,getSmallFont(), Color.WHITE, Justification.CENTER, getRegularFontName()),
				null, false);
	}
	
	@Override
	public FlexibleShape getContextLabelDefaultShape() {
		return new RoundedRectFlexibleShape(8); 
	}

	@Override
	public TextBoxStyle getKeyBoxStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(12, 12, 12, 12),
				new DirectionalValues(12, 12, 12, 12), 
				new ShapeStyle(h,new BasicStroke(1), Color.BLACK, Color.WHITE),
				new TextStyle(h,getSmallBoldFont(), Color.BLACK, Justification.CENTER,getBoldFontName()),
				new TextStyle(h,getSmallFont(), Color.BLACK, Justification.CENTER,getRegularFontName()), true);
	}
	
	

	@Override
	public FlexibleShape getKeyBoxDefaultShape() {
		return new RoundedRectFlexibleShape(0,12,12);
	}

	@Override
	public TextBoxStyle getKeySymbolStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(0, 0, 0, 0),
				new DirectionalValues(0, 0, 0, 0), 
				null, 
				new TextStyle(h, getSmallFont(), getGrey(), Justification.LEFT, getRegularFontName()),
				null, false);
	}

	public BoxStyle getKeyDividerStyle() {
		return null;
	}


	@Override
	public TextBoxStyle getConnectionLabelStyle() {
		return new TextBoxStyle(h,
				new DirectionalValues(2, 2, 2, 2),
				new DirectionalValues(2, 6, 4, 2), 
				new ShapeStyle(h,null, null, new Color(0f, 0f, 0f, 0.2f)), 
				new TextStyle(h,getSmallFont(), Color.WHITE, Justification.LEFT, getRegularFontName()),
				null, false);
	}
	
	

	@Override
	public FlexibleShape getConnectionLabelDefaultShape() {
		return new RoundedRectFlexibleShape(8);
	}

	@Override
	protected double getSymbolPadding() {
		return 0;
	}

}
