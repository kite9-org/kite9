package org.kite9.diagram.visualization.display.java2d.style;

import java.awt.Font;

/**
 * Provides font URL information.
 * 
 * @author robmoffat
 *
 */
public class LocalFont extends Font {
	
	private static final long serialVersionUID = 5349028374123608169L;
	private String file;
	
	public LocalFont(Font out, String file) {
		super(out);		
		this.file = file;
	}

	public String getFontFileName() {
		return file;
	}
}
