package org.kite9.diagram.functional.display;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.junit.Test;
import org.kite9.diagram.visualization.batik.format.Kite9SVGTranscoder;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.common.StackHelp;
import org.kite9.framework.common.TestingHelp;
import org.kite9.framework.serialization.XMLHelper;

public class Test54SVGPrimitives extends AbstractDisplayFunctionalTest {

	
	private void transcode(String s) throws Exception {
		Method m = StackHelp.getAnnotatedMethod(Test.class);
		Class<?> theTest = m.getDeclaringClass();
		File f = TestingHelp.prepareFileName(theTest, m.getName(), m.getName()+"-graph.svg");
		TranscoderInput in = new TranscoderInput(new StringReader(s));
		TranscoderOutput out = new TranscoderOutput(new FileWriter(f));
		Transcoder transcoder = new Kite9SVGTranscoder();
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
	public void test_54_4_ResizeablePrimitive() throws Exception {
		String someXML = 
			svgOpen() + 
				diagramOpen() + 
					containerOpen("container1", "red")+
						scaledOpen() +
							svgRect2() +
						scaledClose() +
						containerOpen("container2", "yellow")+
							scaledOpen() +
								svgRect2() +
							scaledClose()+ 
							fixedSizeOpen()+
								svgText()+
							fixedSizeClose() +
						containerClose() +
					containerClose() +
					containerOpen("container3", "green")+
						scaledOpen() +
							svgRect3() +
						scaledClose() +
						containerOpen("container4", "blue") + 
							scaledOpen() +
								svgRect1() +
							scaledClose() +
						containerClose() + 
					containerClose() +
				diagramClose() + 
			svgClose();
		transcode(someXML);
	}
	
	@Test
	public void test_54_5_ScalablePath() throws Exception {
		String someXML = 
			svgOpen() + 
				diagramOpen() + 
					containerOpen("container1", "red")+
						adaptiveOpen() +
							scalablePath() +
						adaptiveClose() +
						fixedSizeOpen()+
							svgText()+
						fixedSizeClose() +
					containerClose() +
				diagramClose()+ 
			svgClose();
		transcode(someXML);
	}
	
	private String scalablePath() {
		return "<svg:path d='M{x0} {y0} H {x1} V {y1}z' />";
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
		return "<diagram xmlns='"+XMLHelper.KITE9_NAMESPACE+"' id='one' style='type: diagram; padding: 50px; fill: white; stroke: grey; stroke-width: 3px; '>";
	}
	

	private String fixedSizeClose() {
		return "</someelement>";
	}

	private String fixedSizeOpen() {
		return "<someelement id='someelement' style='type: connected; sizing: fixed-size; '>";
	}
	
	private String scaledOpen() {
		return "<somescaled style='type: decal; sizing: scaled; '>"; 
	}
	
	private String scaledClose() {
		return "</somescaled>"; 
	}
	
	private String adaptiveOpen() {
		return "<someadaptive id=\"adap\" style='type: decal; sizing: adaptive; '>"; 
	}
	
	private String adaptiveClose() {
		return "</someadaptive>"; 
	}


	private String containerOpen(String id, String fill) {
		return "<container id='"+id+"' style='type: connected; sizing: minimize; fill: "+fill+"; '>";
	}

	private String containerClose() {
		return "</container>";
	}
	
	private String svgText() {
		return "<svg:text style='font-size: 25px; stroke: black; font-face: sans-serif; '>Some Text</svg:text>";
	}
	
	private String svgRect2() {
		return "<svg:rect x='0' y='0' width='1' height='1' stroke-width='0' stroke='black' />";
	}
	
	private String svgRect1() {
		return "<svg:rect x='0' y='30' width='30' height='10' rx='3' ry='3' stroke-width='1' stroke='black' />";
	}
	
	private String svgRect3() {
		return "<svg:rect x='1' y='1' width='3' height='3' stroke-width='0' stroke='black' />";
	}
}
