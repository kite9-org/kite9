package org.kite9.diagram.unit;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.kite9.diagram.AbstractLayoutFunctionalTest;

public class AbstractRenderingTest extends AbstractLayoutFunctionalTest {

	public AbstractRenderingTest() {
		super();
	}

	protected void setHints(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	}

}