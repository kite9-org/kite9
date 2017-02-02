package org.kite9.diagram.functional.display;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.junit.Test;
import org.kite9.diagram.visualization.batik.Kite9SVGTranscoder;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.serialization.ADLExtensibleDOMImplementation;
import org.kite9.framework.serialization.XMLHelper;

public class Test54SVGPrimitives extends AbstractDisplayFunctionalTest {

	
	private void transcode(String s) throws Exception {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		File f = TestingHelp.prepareFileName(theTest, m.getName(), m.getName()+"-graph.svg");
		TranscoderInput in = new TranscoderInput(new StringReader(s));
		TranscoderOutput out = new TranscoderOutput(new FileWriter(f));
		Transcoder transcoder = new Kite9SVGTranscoder();
		
		TranscodingHints hints = new TranscodingHints();
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, ADLExtensibleDOMImplementation.SVG_NAMESPACE_URI);
		hints.put(XMLAbstractTranscoder.KEY_DOM_IMPLEMENTATION, new ADLExtensibleDOMImplementation());
		
		
		transcoder.setTranscodingHints(hints);
		transcoder.transcode(in, out);
	}
	
	@Test
	public void test_54_1_EmptyDiagram() throws Exception {
		String someXML = svgOpen() + diagramOpen() + diagramClose()+  svgClose();
		transcode(someXML);
	}

	@Test
	public void test_54_2_TextPrimitive() throws Exception {
		String someXML = svgOpen() + diagramOpen() + textOpen()+svgText()+textClose()+diagramClose() + svgClose();
		transcode(someXML);
	}
	
	@Test
	public void test_54_3_TestTranscoderOnRandomSVGFile() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("simple.svg"));
		RepositoryHelp.streamCopy(in, out, true);
		transcode(out.toString());
	}
	
	@Test
	public void test_54_4_TestTranscoderOnKite9() throws Exception {
		String someXML = diagramOpen()+ diagramClose();
		transcode(someXML);
	}
	

	@Test
	public void test_54_6_ResizeablePrimitive() throws IOException {
		String someXML = diagramOpen() + 
					containerOpen()+
				textOpen()+
				"Internal Text" +
				textClose() +
				containerClose() +
				diagramClose();
		renderDiagram(someXML);
	}
	
	private String svgOpen() {
		return "<svg:svg xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:svg='http://www.w3.org/2000/svg'>";
	}
	
	private String svgClose() {
		return "</svg:svg>";
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
	
	private String svgText() {
		return "<svg:text style='font-size: 25px'>Some text</svg:text>";
	}
}
