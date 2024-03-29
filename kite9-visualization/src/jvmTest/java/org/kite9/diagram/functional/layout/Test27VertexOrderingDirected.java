package org.kite9.diagram.functional.layout;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.GraphConstructionTools;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.HopLink;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TurnLink;
import org.w3c.dom.Element;
import org.kite9.diagram.model.position.Direction;

public class Test27VertexOrderingDirected extends AbstractLayoutFunctionalTest {


	@Test
	public void test_27_1_LooseOrdering() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 10, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][1], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][1], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][2], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][3], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][5], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][6], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][4], out[0][9], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][4], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}



	@Test
	@Ignore("Stack overflow issue")
	public void test_27_2_ZigZag() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 8, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][1], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][3], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][4], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][5], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][6], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][7], out[0][0], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		new TurnLink(out[0][0], out[0][1]);
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	@Test
	public void test_27_3_BigE() throws Exception {
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
		
		new Link(out[1][0], out[0][0], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[1][0], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[1][2], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[1][3], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	
	
	@Test
    /** @see http://www.kite9.com/content/non-optimal-creation-routes-ie-one-route-prevents-another */
	public void test_27_4_BigPatch() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 9, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][0], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][0], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.UP);
		new Link(out[0][0], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.UP);
		new Link(out[0][0], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][0], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	@Test
	public void test_27_5_SimpleC() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 6, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][1], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][4], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][5], out[0][3]);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}

	@Test
	public void test_27_6_Inner() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 8, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new HopLink(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new HopLink(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new HopLink(out[0][1], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		
		new HopLink(out[0][4], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new HopLink(out[0][4], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new HopLink(out[0][6], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][5], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		
		new HopLink(out[0][0], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new HopLink(out[0][3], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		

		renderDiagram(new DiagramKite9XMLElement(out2, null));
		
	}

	
	@Test
	public void test_27_7_HopOver() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 9, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		// top row
		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new HopLink(out[0][1], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		
		
		// next row
		new Link(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][3], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new HopLink(out[0][4], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][3], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][5], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		
		// last row
		new Link(out[0][6], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][7], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}
	
	@Test
	public void test_27_9_NaziCross() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 9, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][1], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.UP);
		new Link(out[0][0], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][3], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
		new Link(out[0][5], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);		
		new Link(out[0][0], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.UP);
		new Link(out[0][7], out[0][8], LinkEndStyle.ARROW, null, null, null, Direction.LEFT);
	
		
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}
	
	@Test
	public void test_27_10_BuddiesWithDirection() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 7, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][0], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		
		new Link(out[0][3], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][5], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}
	
	@Test
	public void test_27_11_NotAdjacentConnections() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 8, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		// first row
		new Link(out[0][0], out[0][1], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][1], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new Link(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		
		// second row
		new HopLink(out[0][5], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		new HopLink(out[0][4], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.RIGHT);
		
		// tynes
		new Link(out[0][0], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new HopLink(out[0][1], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new HopLink(out[0][2], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][3], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));
		
		
	}
	
	@Test
	@Ignore("Currently failing in travis")
	public void test_27_12_ZigZagUD() throws Exception {
		List<Element> contents = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", 8, 1, contents, null);

		Context overall = new Context("co", contents, true, null, null);
		List<Element> out2 = new ArrayList<Element>();
		out2.add(overall);

		new Link(out[0][1], out[0][2], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][2], out[0][3], LinkEndStyle.ARROW, null, null, null, Direction.UP);
		new Link(out[0][3], out[0][4], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][4], out[0][5], LinkEndStyle.ARROW, null, null, null, Direction.UP);
		new Link(out[0][5], out[0][6], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);
		new Link(out[0][6], out[0][7], LinkEndStyle.ARROW, null, null, null, Direction.UP);
		new Link(out[0][7], out[0][0], LinkEndStyle.ARROW, null, null, null, Direction.DOWN);

		new TurnLink(out[0][0], out[0][1]);
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));

	}



	@Override
	protected boolean checkMidConnections() {
		return false;  // can't be done for 21_7
	}
	
	
}
