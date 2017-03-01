package org.kite9.diagram.functional.display;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.svggen.SVGGraphics2D;
import org.junit.Test;
import org.kite9.diagram.visualization.batik.format.GroupManagingSVGGraphics2D;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.Document;


public class Test54SVGPrimitives extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_54_1_EmptyDiagram() throws Exception {
		String someXML = svgOpen() + diagramOpen() + diagramClose()+  svgClose();
		transcodeSVG(someXML);
	}

	@Test
	public void test_54_2_FixedGraphicsPrimitive() throws Exception {
		String someXML = svgOpen() + diagramOpen() + fixedSizeOpen()+svgText()+fixedSizeClose()+diagramClose() + svgClose();
		transcodeSVG(someXML);
	}

	@Test
	public void test_54_3_TestTranscoderOnRandomSVGFile() throws Exception {
		StringWriter out = new StringWriter();
		InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("simple.svg"));
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
							svgText()+
						fixedSizeClose() +
					containerClose() +
				diagramClose()+ 
			svgClose();
		transcodeSVG(someXML);
	}
	

	@Test
	public void test_54_6_GradientFill() throws Exception {
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		SVGGraphics2D g2d = new GroupManagingSVGGraphics2D(d);
		Color[] c = new Color[] { Color.BLACK, Color.WHITE};
		LinearGradientPaint lgp = new LinearGradientPaint((Point2D) new Point2D.Double(0, 5),
				(Point2D) new Point2D.Double(0, 100), 
				new float[] {0f, 1f}, c);
		g2d.setPaint(lgp);
		g2d.fill(new Rectangle(0, 0, 100, 100));
		g2d.stream(new FileWriter(getOutputFile("-graph.svg")));
		checkIdenticalXML();
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
		return "<someelement id='someelement' style='type: connected; sizing: fixed; '>";
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
