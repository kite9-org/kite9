package org.kite9.diagram.visualization.display.style;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.kite9.diagram.visualization.display.style.io.SVGHelper;

public class TerminatorShape extends FixedShape {
	
	private double minLength;
	private boolean filled;

	public double getMinInputLinkLength() {
		return minLength;
	}

	public TerminatorShape(SVGHelper h, Stroke stroke, Paint background, Shape path, DirectionalValues margin, double minLength, boolean filled) {
		super(h, stroke, null, background, path, margin);
		this.minLength = minLength;
		this.filled = filled;
	}

	@Override
	public boolean isFilled() {
		return this.filled;
	}

	public double getReservedLength(double strokeWidth) {
		 return getMargin().getTop() + getMargin().getBottom() + strokeWidth;
	}
}
