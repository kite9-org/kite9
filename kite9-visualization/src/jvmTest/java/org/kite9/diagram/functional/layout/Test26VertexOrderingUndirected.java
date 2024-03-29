package org.kite9.diagram.functional.layout;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.GraphConstructionTools;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.w3c.dom.Element;
import org.kite9.diagram.model.position.Layout;

public class Test26VertexOrderingUndirected extends AbstractLayoutFunctionalTest {

	@Test
	public void test_26_1_BigSquare() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 4, 4, contents, Layout.HORIZONTAL);

		Context top = new Context("top", listOf(contents.get(0), contents.get(1)), true, null, Layout.HORIZONTAL);
		Context bottom = new Context("bottom", listOf(contents.get(2), contents.get(3)), true, null, Layout.HORIZONTAL);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(top);
		out2.add(bottom);

		new Link(out[0][0], out[1][0]);
		new Link(out[1][1], out[3][1]);
		new Link(out[2][2], out[3][2]);
		new Link(out[2][3], out[0][3]);

		renderDiagram(new DiagramKite9XMLElement("bob", out2, Layout.VERTICAL, null));

	}

	@Test
	public void test_26_2_BigRow() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 6, 4, contents, Layout.HORIZONTAL);

		Context top = new Context("top", listOf(contents.get(0), contents.get(1)), true, null, Layout.HORIZONTAL);
		Context bottom = new Context("bottom", listOf(contents.get(2), contents.get(3)), true, null, Layout.HORIZONTAL);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(top);
		out2.add(bottom);

		new TurnLink(out[0][0], out[1][0]);
		new TurnLink(out[1][1], out[2][1]);
		new TurnLink(out[2][2], out[3][2]);
		new TurnLink(out[3][3], out[0][3]);

		renderDiagram(new DiagramKite9XMLElement("bob", out2, Layout.HORIZONTAL, null));

	}

	@Test
	public void test_26_3_SimpleLine() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 10, 1, contents, Layout.HORIZONTAL);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][2], out[0][1]);
		new Link(out[0][7], out[0][8]);

		new Link(out[0][1], out[0][8]);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	@Test
	public void test_26_4_Loose() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 10, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new TurnLink(out[0][1], out[0][4]);
		new TurnLink(out[0][1], out[0][5]);
		new TurnLink(out[0][2], out[0][6]);
		new TurnLink(out[0][3], out[0][6]);
		new TurnLink(out[0][5], out[0][8]);
		new TurnLink(out[0][6], out[0][8]);
		new TurnLink(out[0][4], out[0][9]);
		new TurnLink(out[0][4], out[0][7]);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	@Test
	public void test_26_5_Unlinked() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		GraphConstructionTools.createXContainers("g", 10, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	@Test
	public void test_26_6_BigSquare2() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 4, 4, contents, null);

		Context top = new Context("top", listOf(contents.get(0), contents.get(1)), true, null, null);
		Context bottom = new Context("bottom", listOf(contents.get(2), contents.get(3)), true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(top);
		out2.add(bottom);

		new Link(out[0][0], out[1][0]);
		new TurnLink(out[1][1], out[3][1]);
		new Link(out[2][2], out[3][2]);
		new TurnLink(out[2][3], out[0][3]);

		renderDiagram(new DiagramKite9XMLElement("bob", out2, null));

	}
	
	
	@Test
	public void test_26_7_BigSquare3() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 4, 4, contents, Layout.VERTICAL);

		Context top = new Context("top", listOf(contents.get(0), contents.get(1)), true, null, Layout.HORIZONTAL);
		Context bottom = new Context("bottom", listOf(contents.get(2), contents.get(3)), true, null, Layout.HORIZONTAL);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(top);
		out2.add(bottom);

		new Link(out[0][0], out[1][0]);
		new Link(out[1][1], out[3][1]);
		new Link(out[2][2], out[3][2]);
		new Link(out[2][3], out[0][3]);

		renderDiagram(new DiagramKite9XMLElement("bob", out2, null));

	}
	
	@Test
	public void test_26_8_BigRow2() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 6, 4, contents, Layout.HORIZONTAL);

		Context top = new Context("top", listOf(contents.get(0), contents.get(1)), true, null, Layout.HORIZONTAL);
		Context bottom = new Context("bottom", listOf(contents.get(2), contents.get(3)), true, null, Layout.HORIZONTAL);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(top);
		out2.add(bottom);

		new Link(out[0][0], out[1][0]);
		new Link(out[1][1], out[2][1]);
		new Link(out[2][2], out[3][2]);

		renderDiagram(new DiagramKite9XMLElement("bob", out2, Layout.HORIZONTAL, null));

	}
	

	@Test
	public void test_26_9_BigSquareDebug() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 4, 4, contents, null);

		Context top = new Context("top", listOf(contents.get(0), contents.get(1)), true, null, null);
		Context bottom = new Context("bottom", listOf(contents.get(2), contents.get(3)), true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(top);
		out2.add(bottom);

		new Link(out[2][3], out[0][3]);

		renderDiagram(new DiagramKite9XMLElement("bob", out2, null));

	}
	

}
