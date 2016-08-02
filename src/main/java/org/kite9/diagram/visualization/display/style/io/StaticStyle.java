package org.kite9.diagram.visualization.display.style.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;

/**
 * Temporary class
 * @author robmoffat
 *
 */
public class StaticStyle {
	
	public static double getSymbolWidth() {
		return 20; 
		//return ss.getSymbolSize() + ss.getInterSymbolPadding();
	}

	public static Color getWatermarkColour() {
		return new Color(0f, 0f, 0f, .2f);
	}

	public static float getLinkHopSize() {
		return 12;
	}

	public static Paint getBackground() {
		return Color.WHITE;
	}

	public static Stroke getDebugLinkStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Font getDebugTextFont() {
		// TODO Auto-generated method stub
		return null;
	}

	public static double getInterSymbolPadding() {
		return 2;
	}
	
	

	public static int getKeyInternalSpacing() {
		return 15;
	}
}
