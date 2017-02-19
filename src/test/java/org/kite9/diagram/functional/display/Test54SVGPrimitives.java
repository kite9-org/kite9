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
import org.kite9.diagram.visualization.batik.format.Kite9SVGTranscoder;
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
	public void test_54_2_FixedGraphicsPrimitive() throws Exception {
		String someXML = svgOpen() + diagramOpen() + fixedSizeOpen()+svgText()+fixedSizeClose()+diagramClose() + svgClose();
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
		String someXML = svgOpen()+ diagramOpen()+ diagramClose() + svgClose();
		transcode(someXML);
	}
	

	@Test
	public void test_54_6_ResizeablePrimitive() throws Exception {
		String someXML = 
			svgOpen() + 
				diagramOpen() + 
					containerOpen("container1", "red")+
						svgRect2() +
						containerOpen("container2", "yellow")+
							svgRect1() +
							fixedSizeOpen()+
								svgText()+
							fixedSizeClose() +
						containerClose() +
					containerClose() +
					containerOpen("container3", "green")+
						svgRect3() +
						containerOpen("container4", "blue") + 
							svgRect1() +
						containerClose() + 
					containerClose() +
				diagramClose() + 
			svgClose();
		transcode(someXML);
	}
	
	@Test
	public void test_54_7_ScalablePath() throws Exception {
		String someXML = 
			svgOpen() + 
				diagramOpen() + 
					containerOpen("container1", "red")+
						scalablePath() + 
						fixedSizeOpen()+
							svgText()+
						fixedSizeClose() +
					containerClose() +
				diagramClose()+ 
			svgClose();
		transcode(someXML);
	}
	
	private String scalablePath() {
		return "<svg:path d='M10 10 H 50 V 20z' />";
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
	

	private String fixedSizeClose() {
		return "</someelement>";
	}

	private String fixedSizeOpen() {
		return "<someelement id='someelement' style='type: connected; sizing: fixed-size; '>";
	}

	private String containerOpen(String id, String fill) {
		return "<container id='"+id+"' style='type: connected; sizing: minimize; fill: "+fill+"; '>";
	}

	private String containerClose() {
		return "</container>";
	}
	
	private String svgText() {
		return "<svg:text style='font-size: 25px; stroke: black; '>Some text</svg:text>";
	}
	
	private String svgRect2() {
		return "<svg:rect x='0' y='50' width='2' height='1' stroke-width='0' stroke='black' />";
	}
	
	private String svgRect1() {
		return "<svg:rect x='0' y='30' width='30' height='10' rx='3' ry='3' stroke-width='1' stroke='black' />";
	}
	
	private String svgRect3() {
		return "<svg:rect x='1' y='1' width='3' height='3' stroke-width='0' stroke='black' />";
	}
}
