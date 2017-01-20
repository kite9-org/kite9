package org.kite9.diagram.functional.display;

import java.io.IOException;

import org.junit.Test;
import org.kite9.framework.serialization.XMLHelper;

public class Test54SVGPrimitives extends AbstractDisplayFunctionalTest {

	@Test
	public void test_54_1_EmptyDiagram() throws IOException {
		String someXML = diagramOpen()+ diagramClose();
		renderDiagram(someXML);
	}

	@Test
	public void test_54_2_TextPrimitive() throws IOException {
		String someXML = diagramOpen() + textOpen()+"The Text"+textClose()+diagramClose();
		renderDiagram(someXML);
	}

	@Test
	public void test_54_3_ResizeablePrimitive() throws IOException {
		String someXML = diagramOpen() + 
					containerOpen()+
				textOpen()+
				"Internal Text" +
				textClose() +
				containerClose() +
				diagramClose();
		renderDiagram(someXML);
	}
	
	private String diagramClose() {
		return "</diagram>";
	}

	private String diagramOpen() {
		return "<diagram xmlns='"+XMLHelper.KITE9_NAMESPACE+"' id='one' style='type: diagram; padding: 30px; fill: white; stroke: grey; stroke-width: 3px; '>";
	}
	

	private String textClose() {
		return "</someelement>";
	}

	private String textOpen() {
		return "<someelement style='type: connected; sizing: text; '>";
	}

	private String containerOpen() {
		return "<container style='type: container'>";
	}

	private String containerClose() {
		return "</container>";
	}
}
