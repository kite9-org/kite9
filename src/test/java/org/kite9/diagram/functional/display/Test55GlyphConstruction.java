package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.framework.serialization.XMLHelper;

public class Test55GlyphConstruction extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_55_1_SimplestGlyph() throws Exception {
		String someXML = 
			svgOpen() + 	
				svgDefs() +
				diagramOpen() +
					glyphOpen() + 
						backOpen() +
							shadedRect() +
						backClose() +
						labelOpen() +
							svgText()+ 
						labelClose() +
					glyphClose() +
				diagramClose()+ 
			svgClose();
		transcodeSVG(someXML);
	}

	private String backClose() {
		return "</back>";
	}

	private String shadedRect() {
		return "<svg:rect x='0' y='0' width='{x1}' height='{y1}' rx='5' ry='5' "
				+ "style='fill: url(#gg); stroke: black; stroke-width: 2px; '/>";
	}

	private String backOpen() {
		return "<back style='type: decal; sizing: adaptive; '>";
	}

	private String glyphOpen() {
		return "<glyph id='g1' style='type: connected; sizing: minimize; padding: 3px 6px 3px 6px;  '>"; 
	}
	
	private String glyphClose() {
		return "</glyph>";
	}
	
	private String labelOpen() {
		return "<label style='type: connected; sizing: fixed'>";
	}
	
	private String labelClose() {
		return "</label>";
	}

	private String svgText() {
		return "<svg:text style='font-size: 15px; stroke: black; font-face: sans-serif; '>Some Glyph</svg:text>";
	}
	
	private String svgOpen() {
		return "<svg:svg xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:svg='http://www.w3.org/2000/svg'>";
	}
	
	private String svgDefs() {
		return "<svg:defs>"
				+ "<svg:linearGradient id='gg'>"
				+ "<svg:stop offset='5%' stop-color='#F60' />"
				+ "<svg:stop offset='95%' stop-color='#FF6' />"
				+ "</svg:linearGradient>"
				+ "</svg:defs>";
	}
	
	private String svgClose() {
		return "</svg:svg>";
	}
	
	private String diagramClose() {
		return "</diagram>";
	}

	private String diagramOpen() {
		return "<diagram xmlns='"+XMLHelper.KITE9_NAMESPACE+"' id='one' style='type: diagram;'>";
	}
	

	


}
