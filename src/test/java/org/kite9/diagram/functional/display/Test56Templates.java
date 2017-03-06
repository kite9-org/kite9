package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.framework.serialization.XMLHelper;

public class Test56Templates extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_56_1_SimplestGlyphUsingTemplate() throws Exception {
		String someXML = 
			svgOpen() + 	
				diagramOpen() +
					glyphOpen() + 
						labelOpen() +
							svgLabelText()+ 
						labelClose() +
					glyphClose() +
				diagramClose()+ 
			svgClose();
		transcodeSVG(someXML);
	}
	
	@Test
	public void test_55_2_ComplexGlyph() throws Exception {
		String someXML = svgOpen() + 	
				svgDefs() +
				diagramOpen() +
					complexGlyphOpen() + 
						backOpen() +
							shadedRect() +
						backClose() +
						stereoOpen() + 
							svgStereoText() +
						stereoClose() +
						symbolsContainer() + 
						labelOpen() +
							svgLabelText()+ 
						labelClose() +
						textContainer() +
					glyphClose() +
				diagramClose()+ 
			svgClose();
		transcodePNG(someXML);
	}

	private String textContainer() {
		return "<text id='textContainer' style='type: connected; sizing: minimize; layout: vertical; occupies: 0 1 2 2 '>"  
			+textLine("tl1") + textLine("tl2") + textLine("tl3")
		+ "</text>";
	}

	private String textLine(String id) {
		return "<text-line id='"+id+"' style='type: connected; sizing: fixed; margin: 4px; '>"+
				"<svg:text style='font-size: 15px; stroke: black; font-face: sans-serif; '>This is text line "+id+"</svg:text>" +
				"</text-line>";
	}

	private String symbolsContainer() {
		return "<symbols id='symbolsContainer'  style='type: connected; sizing: minimize; layout: horizontal; occupies: 1 0 '>" 
			+ symbol("s1") + symbol("s2") + symbol("s3")
		+ "</symbols>";
	}
	
	private String symbol(String id) {
		return "<symbol id='"+id+"' style='type: connected; sizing: fixed;'>"+
			"<svg:circle cx='0' cy='0' r='10' />" +
			"</symbol>";
			
	}

	private String backClose() {
		return "</back>";
	}

	private String shadedRect() {
		return "<svg:rect x='0' y='0' width='{x1}' height='{y1}' rx='8' ry='8' "
				+ "style='fill: url(#gg); stroke: black; stroke-width: 2px; '/>";
	}

	private String backOpen() {
		return "<back id='back' style='type: decal; sizing: adaptive; '>";
	}

	private String glyphOpen() {
		return "<glyph id='g1' style='type: connected; sizing: minimize; padding: 8px 10px 8px 10px;  '>"; 
	}
	
	private String complexGlyphOpen() {
		return "<glyph id='g1' style='type: connected; sizing: minimize; layout: grid; grid-size: 2 3; padding: 8px 10px 8px 10px;  '>"; 
	}
	
	private String glyphClose() {
		return "</glyph>";
	}
	
	private String stereoOpen() {
		return "<stereo id='stereo' style='type: connected; sizing: fixed; occupies: 0 0'>";
	}
	
	private String stereoClose() {
		return "</stereo>";
	}
	
	private String labelOpen() {
		return "<label id='label' style='type: connected; sizing: fixed; occupies: 1 1 0 1'>";
	}
	
	private String labelClose() {
		return "</label>";
	}

	private String svgLabelText() {
		return "<svg:text style='font-size: 15px; stroke: black; font-face: sans-serif; '>Some Glyph</svg:text>";
	}
	
	private String svgStereoText() {
		return "<svg:text style='font-size: 12px; font-weight: bold; stroke: black; font-face: sans-serif; '>Stereo</svg:text>";
	}
	
	private String svgOpen() {
		return "<svg:svg xmlns:svg='http://www.w3.org/2000/svg'>";
	}
	
	private String svgDefs() {
		return "<svg:defs>"
				+ "<svg:linearGradient id='gg' x1='0%' x2='0%' y1='0%' y2='100%'>"
				+ "<svg:stop offset='0%' stop-color='#FFF' />"
				+ "<svg:stop offset='100%' stop-color='#DDD' />"
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
		return "<diagram xmlns='"+XMLHelper.KITE9_NAMESPACE+"' id='one' style='type: diagram; padding: 10px'>";
	}
	

	


}
