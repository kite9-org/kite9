package org.kite9.diagram.visualization.display.style;

import org.kite9.diagram.visualization.display.style.io.SVGHelper;


/**
 * Handles basic formatting of a box, which is compatible with CSS box model.
 * 
 * @author robmoffat
 */
public class BoxStyle extends ShapeStyle {

	DirectionalValues internalPadding;
	DirectionalValues outerMargin;
	
	public DirectionalValues getMargin() {
		return outerMargin;
	}
	
	public void setMargin(DirectionalValues dv) {
		this.outerMargin = dv;
	}

	public BoxStyle(SVGHelper h, DirectionalValues padding, DirectionalValues margin, ShapeStyle borderStyle, boolean castsShadow) {
		super(h, borderStyle);
		this.internalPadding = padding;
		this.outerMargin = margin;
		this.castsShadow = castsShadow;
	}
	

	public DirectionalValues getInternalPadding() {
		return internalPadding;
	}
}
