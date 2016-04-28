package org.kite9.diagram.visualization.display.java2d.style.sheets;

import java.awt.Color;


public class BasicBlueStylesheet extends BasicStylesheet {

	@Override
	public String getId() {
		return "blue";
	}
	
	public Color getShadowColour() {
		return new Color(0, 0, 0, .2f);
	}

	private static final Color DARKEST_BACKGROUND = new Color(223,230,240);
    private static final Color LIGHTEST_BACKGROUND =  new Color(234,248,252);

	
	protected Color getBackgroundGradientCenterColour() {
		return LIGHTEST_BACKGROUND;
	}


	protected Color getBackgroundGradientEdgeColour() {
		return DARKEST_BACKGROUND;
	}


}
