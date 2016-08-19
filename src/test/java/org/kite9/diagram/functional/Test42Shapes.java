package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.FlowchartShapes;
import org.kite9.diagram.visualization.display.style.shapes.UMLShapes;
import org.kite9.diagram.xml.Context;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.diagram.xml.Link;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.diagram.xml.TextLine;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.logging.Kite9Log;

public class Test42Shapes extends AbstractFunctionalTest {

	@Test
	public void test_42_1_DiamondShapedGlyph() throws IOException {
		Diagram d = createDiagram("DIAMOND");
		renderDiagramLocal(d);
	}

	private Diagram createDiagram(String shape) {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Glyph two = new Glyph("two", null, "A slightly longer one", null, null);
		Glyph three = new Glyph("three", null, "Thinny", null, null);
		Glyph four = new Glyph("four", "some type", "Running out of things To do", null, null);
		
		one.setShapeName(shape);
		two.setShapeName(shape);
		three.setShapeName(shape);
		createLinks(one, two);
		createDownLinks(one, four);
		
		Diagram d = new Diagram("The Diagram", createList((XMLElement) one, two, three, four), null);
		return d;
	}
	
	@Test
	public void test_42_2_HexagonShapedGlyph() throws IOException {
		Diagram d = createDiagram("HEXAGON");
		renderDiagramLocal(d);
	}

	private void createLinks(Glyph one, Glyph two) {
//		for (int i = 0; i < 3; i++) {
//			new Link(one, two);
			new Link(one, two, LinkEndStyle.ARROW, new TextLine("from"), LinkEndStyle.ARROW, new TextLine("to"), Direction.RIGHT);
//		}
	}
	
	private void createDownLinks(Glyph one, Glyph two) {
		//for (int i = 0; i < 2; i++) {
		//	new Link(one, two);
			new Link(one, two, LinkEndStyle.ARROW, new TextLine("from"), LinkEndStyle.ARROW, new TextLine("to"), Direction.DOWN);
		//}
	}
	
	@Test
	public void test_42_3_EllipseShapedGlyph() throws IOException {
		Diagram d = createDiagram("ELLIPSE");
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_4_CircleShapedGlyph() throws IOException {
		Diagram d = createDiagram("CIRCLE");
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_5_RoundedRectangleShapedGlyph() throws IOException {
		Diagram d = createDiagram(null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_6_FlowChartSymbolsGlyphs() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : FlowchartShapes.getShapes().keySet()) {
			Glyph g = new Glyph(type, type, type, null, null);
			g.setShapeName(type);
			out.add(g);
		}
		
		addConnectors(out);
		
		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	private void addConnectors(List<XMLElement> out) {
		addConnectors(out, 1);
	}
	
	private void addConnectors(List<XMLElement> out, int distance) {
		for (int i = 0; i < out.size(); i++) {
			int next = (i + distance) % out.size();
			Connected from =(Connected) out.get(i);
			Connected to = (Connected) out.get(next);
			
			new Link(from, to, null, null, null, null, null);
		}

	}

	@Test
	public void test_42_7_FlowChartSymbolsGlyphsNoStereo() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : FlowchartShapes.getShapes().keySet()) {
			Glyph g = new Glyph(type, "", type, null, null);
			g.setShapeName(type);
			out.add(g);
		}
		
		addConnectors(out);
		
		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_8_FlowChartSymbolsGlyphsNoContent() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : FlowchartShapes.getShapes().keySet()) {
			Glyph g = new Glyph(type, "", "", null, null);
			g.setShapeName("fc"+type);
			out.add(g);
		}
		
		addConnectors(out);

		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_9_FlowChartSymbolsContexts() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : FlowchartShapes.getShapes().keySet()) {
			FlexibleShape shape = FlowchartShapes.getShape(type);
			if (shape.canUseForContext()) {
				List<XMLElement> content = new ArrayList<XMLElement>();
				content.add(new Glyph("blah", "blah dhjsfgjg sdhfgjdgs", null, null));
				Context g = new Context(content, true, new TextLine(type), null);
				g.setShapeName(type);
				out.add(g);
				//break;
			}
		}
		
		addConnectors(out);

		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_10_FlowChartSymbolsGlyphsMultiConnected() throws IOException {
		Kite9Log.setLogging(false);
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : FlowchartShapes.getShapes().keySet()) {
			Glyph g = new Glyph(type, "a", type, null, null);
			g.setShapeName(type);
			out.add(g);
		}
		addConnectors(out, 1);
		addConnectors(out, 5);
		addConnectors(out, 5);
		addConnectors(out, 3);
		
		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	

	private void renderDiagramLocal(Diagram d) throws IOException {
		renderDiagram(d);
		renderDiagramSizes(d);
	}
	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}
	
	
	
	@Override
	protected boolean checkImage() {
		return false;
	}
	
	

	@Test
	public void test_42_11_UMLSymbolsGlyphs() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : UMLShapes.getShapes().keySet()) {
				Glyph g = new Glyph("somet", type, null, null);
				g.setShapeName(type);
				out.add(g);
				//break;
		}
		addConnectors(out);

		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_12_UMLSymbolsGlyphsNoStereo() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : UMLShapes.getShapes().keySet()) {
				Glyph g = new Glyph(null, type, null, null);
				g.setShapeName(type);
				out.add(g);
				//break;
		}
		addConnectors(out);

		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_13_UMLSymbolsGlyphsNoContent() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : UMLShapes.getShapes().keySet()) {
				Glyph g = new Glyph(null, null, null, null);
				g.setShapeName(type);
				out.add(g);
				//break;
		}
		addConnectors(out);

		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_14_UMLSymbolsContexts() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		for (String type : UMLShapes.getShapes().keySet()) {
				if (UMLShapes.getShape(type).canUseForContext()) {
					List<XMLElement> content = new ArrayList<XMLElement>();
					Glyph gl = new Glyph("blah", "blah blah rarsdff", null, null);
					gl.setShapeName("umlACTOR");
					content.add(gl);
					Context g = new Context(type, content, true, new TextLine(type), null);
					g.setShapeName(type); //"umlCONTAINER");
					out.add(g);
				}
				//break;
		}
		addConnectors(out);

		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_20_ArrowReservingLabels() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		Glyph g = new Glyph("g", "blah", "blah", null, null);
		Context c = new Context("context", null, true, new TextLine("database my old boy"), null);
		c.setShapeName("fcDECISION");
		out.add(g);
		out.add(c);
		new Link(g, c, "DIAMOND", new TextLine("Watch me move"), "BARBED ARROW", new TextLine("Yowzer"), Direction.RIGHT);
		
		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Ignore("Broken in sprint 7")
	@Test
	public void test_42_21_ArrowReservingConvex() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		Glyph g = new Glyph("g", "blah", "blah", null, null);
		Glyph g2 = new Glyph("g", "blah", "blah", null, null);
		g.setShapeName("fcDOCUMENT");
		out.add(g);
		out.add(g2);
		new Link(g, g2, null, null, null, null, Direction.DOWN);
		new Link(g, g2, null, null, null, null, Direction.DOWN);
		
		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_22_ArrowReservingPrevent() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		Glyph g = new Glyph("diam", "blah", "blah", null, null);
		Glyph g2 = new Glyph("g2", "blah", "blah", null, null);
		Glyph g3 = new Glyph("g3", "blah", "blah", null, null);
		g.setShapeName("fcDECISION");
		out.add(g);
		out.add(g2);
		out.add(g3);
		new Link(g, g2, null, null, null, null, Direction.DOWN);
		new Link(g, g2, null, null, null, null, Direction.DOWN);
		new Link(g, g3, null, null, null, null, Direction.RIGHT);
		new Link(g, g3, null, null, null, null, Direction.RIGHT);
		
		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
	
	@Test
	public void test_42_23_ArrowReservingOkay() throws IOException {
		List<XMLElement> out = new LinkedList<XMLElement>();
		Glyph g = new Glyph("diam", "blah", "blah", null, null);
		Glyph g2 = new Glyph("g2", "blah", "blah", null, null);
		Glyph g3 = new Glyph("g3", "blah", "blah", null, null);
		g.setShapeName("fcDECISION");
		out.add(g);
		out.add(g2);
		out.add(g3);
		new Link(g, g2, null, null, null, null, Direction.DOWN);
		new Link(g, g2, null, null, null, null, Direction.DOWN);
		new Link(g, g3, null, null, null, null, Direction.RIGHT);
		
		Diagram d = new Diagram(out, null);
		renderDiagramLocal(d);
	}
}
