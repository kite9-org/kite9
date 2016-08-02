package org.kite9.diagram.visualization.display.style;

import java.awt.Color;
import java.awt.Paint;

import org.kite9.diagram.style.StyledDiagramElement;


/**
 * Handles basic formatting of a box, which is compatible with CSS box model.
 * 
 * @author robmoffat
 */
public class BoxStyle extends ShapeStyle {

	DirectionalValues overrideMargin = null;

	public BoxStyle(StyledDiagramElement h) {
		super(h);
	}
	
	public BoxStyle(BoxStyle original, DirectionalValues overrideMargin) {
		super((StyledDiagramElement) original.styleElement);
		this.overrideMargin = overrideMargin;
	}
	

	public DirectionalValues getInternalPadding() {
		return getDirectionalValues("padding");
	}

	public Paint getShadowPaint() {
		return new Color(.7f, .7f, .7f);
	}

	@Override
	public DirectionalValues getMargin() {
		if (overrideMargin != null) {
			return overrideMargin;
		} 
		return super.getMargin();
	}
	
	
}
