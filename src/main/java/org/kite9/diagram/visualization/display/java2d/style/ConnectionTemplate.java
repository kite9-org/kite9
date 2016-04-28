package org.kite9.diagram.visualization.display.java2d.style;

public class ConnectionTemplate {

	String fromTerminator;
	
	public String getFromTerminator() {
		return fromTerminator;
	}

	public String getToTerminator() {
		return toTerminator;
	}

	public String getLinkStyle() {
		return linkStyle;
	}

	public String getLinkShape() {
		return linkShape;
	}

	String toTerminator;
	
	String linkStyle;
	
	String linkShape;
	
	String description;

	public ConnectionTemplate(String fromTerminator, String toTerminator, String linkShape, String linkStyle, String description) {
		super();
		this.fromTerminator = fromTerminator;
		this.toTerminator = toTerminator;
		this.linkShape = linkShape;
		this.linkStyle = linkStyle;
		this.description = description;
	}

	
}
