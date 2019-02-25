package org.kite9.diagram.functional.display;

import java.io.InputStreamReader;
import java.io.StringWriter;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.framework.common.RepositoryHelp;

public class Test54SVGPrimitives extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_54_1_EmptyDiagram() throws Exception {
		String someXML = svgOpen() + diagramOpen() + diagramClose()+  svgClose();
		transcodeSVG(someXML);
	}

	@Test
	public void test_54_2_FixedGraphicsPrimitive() throws Exception {
		String someXML = svgOpen() + diagramOpen() + fixedSizeOpen()+svgSquiggle()+fixedSizeClose()+diagramClose() + svgClose();
		transcodeSVG(someXML);
	}

	@Test
	public void test_54_3_TestTranscoderOnRandomSVGFile() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("test_54_simple.svg"));
		RepositoryHelp.streamCopy(in, out, true);
		String xml = out.toString();
		transcodePNG(xml);
		transcodeSVG(xml);
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
								svgSquiggle()+
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
		transcodeSVG(someXML);
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
							svgSquiggle()+
						fixedSizeClose() +
					containerClose() +
				diagramClose()+ 
			svgClose();
		transcodeSVG(someXML);
	}


	@Test
	public void test_54_7_FontSVGTranscoding() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("test_54_fontexample.svg"));
		RepositoryHelp.streamCopy(in, out, true);
		String xml = out.toString();
		transcodePNG(xml);
		transcodeSVG(xml);
	}
	
	@Test
	public void test_54_8_ImageSVGTranscoding() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("test_54_image.svg"));
		RepositoryHelp.streamCopy(in, out, true);
		String xml = out.toString();
		transcodePNG(xml);
		transcodeSVG(xml);
	}
	
	@Test
	public void test_54_9_GradientSVGTranscoding() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("test_54_gradient.svg"));
		RepositoryHelp.streamCopy(in, out, true);
		String xml = out.toString();
		transcodePNG(xml);
		transcodeSVG(xml);
	}
	
	@Test
	public void test_54_10_ShadowSVGTranscoding() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("test_54_dropshadow.svg"));
		RepositoryHelp.streamCopy(in, out, true);
		String xml = out.toString();
		transcodePNG(xml);
		transcodeSVG(xml);
	}
	
	@Test
	public void test_54_11_FlowingText() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("test_54_simpleflow.svg"));
		RepositoryHelp.streamCopy(in, out, true);
		String xml = out.toString();
		transcodePNG(xml);
		// no svg, as it won't render in any browser!
	}
	
	private String scalablePath() {
		return "<svg:path d='M0 0 H #{$width} V #{$height}z' />";
	}

	private String svgOpen() {
		return "<svg:svg xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:svg='http://www.w3.org/2000/svg'>";
	}
	
	private String svgClose() {
		return "</svg:svg>";
	}
	
	private String diagramClose() {
		return "</svg:g></diagram>";
	}

	private String diagramOpen() {
		return "<diagram xmlns='"+XMLHelper.KITE9_NAMESPACE+"' id='one' style='kite9-type: diagram; kite9-padding: 50px;'><svg:g style='fill: white; stroke: grey; stroke-width: 3px; '>";
	}
	

	private String fixedSizeClose() {
		return "</someelement>";
	}

	private String fixedSizeOpen() {
		return "<someelement id='someelement' style='kite9-type: svg; '>";
	}
	
	private String scaledOpen() {
		return "<somescaled style='kite9-usage: decal; kite9-transform: rescale; kite9-type: svg; '>"; 
	}
	
	private String scaledClose() {
		return "</somescaled>"; 
	}
	
	private String adaptiveOpen() {
		return "<someadaptive id=\"adap\" style='kite9-type: svg; kite9-usage: decal; '>"; 
	}
	
	private String adaptiveClose() {
		return "</someadaptive>"; 
	}


	private String containerOpen(String id, String fill) {
		return "<container id='"+id+"' style='kite9-type: container; kite9-sizing: minimize; kite9-padding: 10px; kite9-margin: 10px;  '><svg:g style='fill: "+fill+"; '>";
	}

	private String containerClose() {
		return "</svg:g></container>";
	}
	
	private String svgSquiggle() {
		return "<svg:path d=\"M 100 100 L 300 100 L 200 300 z\" fill=\"red\" stroke=\"blue\" stroke-width=\"3\" />";
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
