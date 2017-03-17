package org.kite9.diagram.functional.display;

import java.net.URL;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.framework.dom.XMLHelper;

public class Test56Templates extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_56_1_SimplestGlyphUsingTemplate() throws Exception {
		String someXML = 
			svgOpen() + 	
				diagramOpen() +
					glyphOpen() + 
						label() +
					glyphClose() +
				diagramClose()+ 
			svgClose();
		transcodeSVG(someXML);
	}

	private String glyphOpen() {
		URL u = getClass().getResource("template.svg");
		String toUse = u.toString() + "#glyph";
		return "<glyph id='g1' style='type: connected; sizing: minimize; padding: 8px 10px 8px 10px; template: url("+toUse+");  '>"; 
	}
	
	
	private String glyphClose() {
		return "</glyph>";
	}
	
	
	private String label() {
		URL u = getClass().getResource("template.svg");
		String toUse = u.toString() + "#label";
		return "<label id='label' text='This is my Glyph' style='type: connected; sizing: fixed; template: url("+toUse+"); ' />";
	}
	
	private String svgOpen() {
		return "<svg:svg xmlns:svg='http://www.w3.org/2000/svg'>";
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
