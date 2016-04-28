package org.kite9.diagram.visualization.display.java2d.style;

import org.kite9.diagram.visualization.display.java2d.style.io.SVGHelper;


public class TextBoxStyle extends BoxStyle {
	
	public TextBoxStyle(SVGHelper h, DirectionalValues padding, DirectionalValues margin,
			ShapeStyle borderStyle, TextStyle labelFormat, TextStyle typeFormat, boolean castsShadow) {
		super(h, padding, margin, borderStyle, castsShadow);
		this.labelTextFormat = labelFormat;
		this.typeTextFormat = typeFormat;
	}

	private TextStyle labelTextFormat;
	
	private TextStyle typeTextFormat;
	
	public TextStyle getLabelTextFormat() {
		return labelTextFormat;
	}
	
	public TextStyle getTypeTextFormat() {
		return typeTextFormat;
	}
	
}
