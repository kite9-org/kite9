package org.kite9.diagram.visualization.display.style;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import org.apache.batik.css.engine.CSSStylableElement;
import org.kite9.diagram.visualization.display.style.io.SVGHelper;

public class TerminatorShape extends FixedShape {
	
	private double minLength;
	private boolean filled;

	public double getMinInputLinkLength() {
		return minLength;
	}

	public TerminatorShape(CSSStylableElement h) {
		super(h);
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
