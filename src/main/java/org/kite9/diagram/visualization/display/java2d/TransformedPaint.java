package org.kite9.diagram.visualization.display.java2d;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

public final class TransformedPaint implements Paint {
	
	private final Paint p;
	private final AffineTransform paintTransform;
	private final String key;

	public TransformedPaint(Paint p, AffineTransform paintTransform, String key) {
		this.p = p;
		this.paintTransform = paintTransform;
		this.key = key;
	}

	@Override
	public int getTransparency() {
		return 1;
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
			Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
							
		return p.createContext(cm, deviceBounds, userBounds, paintTransform, hints);
	}
	
	public Paint getUnderlyingPaint() {
		return p;
	}
	
	public String getKey() {
		return key;
	}
}