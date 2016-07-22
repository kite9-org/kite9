package org.kite9.diagram.visualization.display.style;

import org.kite9.diagram.adl.LinkTerminator;

public class TerminatorShape extends FixedShape {
	
	/*private double minLength;
	private boolean filled;
	Stroke s;
	Shape p;
	DirectionalValues margin;
	Paint paint;*/

	public double getMinInputLinkLength() {
		return 20;  // minLength;
	}

	/*public TerminatorShape(BasicStroke s, Paint paint, Shape p, DirectionalValues margin, float f, boolean b) {
		super(null);
		this.p = p;
		this.margin = margin;
		this.minLength = f;
		this.filled = b;
		this.paint = paint;
		this.s = s;
	}*/

	public TerminatorShape(LinkTerminator s) {
		super(s);
	}

	/*@Override
	public boolean isFilled() {
		return this.filled;
	}*/

	public double getReservedLength(double strokeWidth) {
		 return getMargin().getTop() + getMargin().getBottom() + strokeWidth;
	}

//	@Override
//	public DirectionalValues getMargin() {
//		return margin;
//	}
//
//	@Override
//	public float getWidth() {
//		return 2;
//	}
//
//	@Override
//	public Shape getPath() {
//		return p;
//	}
//
//	@Override
//	public Stroke getStroke() {
//		return s;
//	}
//	
	
	
}
