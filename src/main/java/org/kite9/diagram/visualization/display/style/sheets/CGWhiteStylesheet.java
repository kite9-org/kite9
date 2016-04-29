package org.kite9.diagram.visualization.display.style.sheets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;


public class CGWhiteStylesheet extends BasicStylesheet {


	@Override
	public String getId() {
		return "cg_white";
	}
	
	public Color getShadowColour() {
		return SHADOW_COLOUR;
	}

	private static final Color SHADOW_COLOUR = new Color(192, 214, 226);
	
	public Paint getBackground() {
		return Color.WHITE;
	}

	@Override
	protected Color getDarkColour() {
		return TEXT_COLOR;
	}

	@Override
	public Paint getGlyphBackgroundColour() {
		return new Color(247, 219, 198);
	}

	@Override
	public Color getGlyphDividerColour() {
		return TEXT_COLOR;
	}
	
	@Override
	public Color getContextBorderColour() {
		return getDarkColour();
	}

	@Override
	protected Color getLightColour() {
		return Color.WHITE;
	}

	@Override
	protected Color getMidColour() {
		return SHADOW_COLOUR;
	}

	@Override
	public String getBoldFont() {
		return "GOTHICB";
	}

	@Override
	protected String getPlainFontName() {
		return "GOTHIC";
	}

	@Override
	protected String getBoldFontName() {
		return "GOTHICB";
	}
	
	public static final Color TEXT_COLOR = new Color(20, 60, 113);


//	@Override
//	public Color getGlyphBorderColour() {
//		return KITE9_SKY_BLUE;
//	}

	public final static BasicStroke BORDER_STROKE = new BasicStroke(2.5f);
	
	public final static BasicStroke DIVIDER_STROKE = new BasicStroke(1f);



	@Override
	public Stroke getGlyphDividerStroke() {
		return DIVIDER_STROKE;
	}

	@Override
	public int getGridSize() {
		return 12;
	}

}
