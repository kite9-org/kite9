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
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TurnLink;
import org.w3c.dom.Element;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;

public class Test28VertexOrderingMixed extends AbstractLayoutFunctionalTest {

	@Test
	public void test_28_1_VertexPushing() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 10, 1, contents, Layout.HORIZONTAL);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][2], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][7], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		new TurnLink(out[0][1], out[0][2]);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}


	@Test
	public void test_28_2_VertexPushing() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 10, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][2], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][7], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		new Link(out[0][1], out[0][8]);

		new Link(out[0][4], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][6], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		new Link(out[0][5], out[0][0]);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	@Test
	public void test_28_3_BigERelaxed() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 5, 2, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		new Link(out[1][0], out[1][1], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[1][0], out[1][2], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[1][0], out[1][3], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[1][0], out[1][4], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		
		new TurnLink(out[1][1], out[0][1]);
		new TurnLink(out[1][1], out[0][3]);
		new TurnLink(out[1][2], out[0][2]);
		new TurnLink(out[1][3], out[0][1]);
		
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}
	
	@Test
	public void test_28_4_FragmentDirected() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 6, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][3], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][4], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][1], out[0][2]);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}
	
	
	@Test
	public void test_28_5_BuddiesAndSeparateOrderMerging() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 8, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][4], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][6], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		
		new Link(out[0][0], out[0][2]);
		new Link(out[0][2], out[0][4]);
		new Link(out[0][4], out[0][6]);
		
		new Link(out[0][1], out[0][3]);
		new Link(out[0][3], out[0][5]);
		new Link(out[0][5], out[0][7]);
		
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}
	
	
}
