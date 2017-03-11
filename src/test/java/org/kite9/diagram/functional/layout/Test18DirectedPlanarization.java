package org.kite9.diagram.functional.layout;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.HopLink;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.functional.NotAddressed;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.HelpMethods;

public class Test18DirectedPlanarization extends AbstractLayoutFunctionalTest {

	@Test
	public void test_18_1_AtoB() throws Exception {
		Glyph a = new Glyph("", "A", null, null);
		Glyph b = new Glyph("", "B", null, null);
		new Link(a, b, null, null, null, null, Direction.UP);
		DiagramXMLElement d = new DiagramXMLElement("test", createList(a, b), null);

		renderDiagram(d);

	}

	@Test
	public void test_18_2_FourPointStar() throws Exception {
		Glyph a = new Glyph("", "A", null, null);
		Glyph b = new Glyph("", "B", null, null);
		Glyph c = new Glyph("", "C", null, null);
		Glyph d = new Glyph("", "D", null, null);
		Glyph e = new Glyph("", "E", null, null);

		new Link(a, c, null, null, null, null, Direction.RIGHT);
		new Link(b, c, null, null, null, null, Direction.DOWN);
		new Link(d, c, null, null, null, null, Direction.UP);
		new Link(e, c, null, null, null, null, Direction.LEFT);
		DiagramXMLElement diag = new DiagramXMLElement("test", createList(a, b, c, d, e), null);

		renderDiagram(diag);

	}

	@Test
	public void test_18_3_VerticalBoxes() throws Exception {
		Glyph s = new Glyph("S", "", "S", null, null);
		Glyph e = new Glyph("E", "", "E", null, null);

		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);
		Glyph three = new Glyph("three", "", "three", null, null);

		new Link(s, one, null, null, null, null, Direction.RIGHT);
		new Link(s, two, null, null, null, null, Direction.RIGHT);
		new Link(s, three, null, null, null, null, Direction.RIGHT);

		new Link(e, one, null, null, null, null, Direction.LEFT);
		new Link(e, two, null, null, null, null, Direction.LEFT);
		new Link(e, three, null, null, null, null, Direction.LEFT);

		DiagramXMLElement d = new DiagramXMLElement("test", createList(s, e, one, two, three), null);

		renderDiagram(d);

	}

	@Test
	public void test_18_4_FourPointCross() throws Exception {
		Glyph a1 = new Glyph("A1", "", "A1", null, null);
		Glyph b1 = new Glyph("B1", "", "B1", null, null);
		Glyph a2 = new Glyph("A2", "", "A2", null, null);
		Glyph b2 = new Glyph("B2", "", "B2", null, null);

		Glyph c = new Glyph("C", "", "C", null, null);
		Glyph d1 = new Glyph("D1", "", "D1", null, null);
		Glyph e1 = new Glyph("E1", "", "E1", null, null);
		Glyph d2 = new Glyph("D2", "", "D2", null, null);
		Glyph e2 = new Glyph("E2", "", "E2", null, null);

		new Link(a1, c, null, null, null, null, Direction.RIGHT);
		new Link(b1, c, null, null, null, null, Direction.DOWN);
		new Link(d1, c, null, null, null, null, Direction.UP);
		new Link(e1, c, null, null, null, null, Direction.LEFT);

		new Link(a2, c, null, null, null, null, Direction.RIGHT);
		new Link(b2, c, null, null, null, null, Direction.DOWN);
		new Link(d2, c, null, null, null, null, Direction.UP);
		new Link(e2, c, null, null, null, null, Direction.LEFT);

		new Link(a1, a2, null, null, null, null, Direction.DOWN);
		new Link(b1, b2, null, null, null, null, Direction.RIGHT);
		new Link(d1, d2, null, null, null, null, Direction.RIGHT);
		new Link(e1, e2, null, null, null, null, Direction.DOWN);

		DiagramXMLElement diag = new DiagramXMLElement("test", createList(a1, b1, c, d1, e1, a2, b2, d2, e2), null);

		renderDiagram(diag);

	}

	@Test
	/** @see http://www.kite9.com/content/non-optimal-creation-routes-ie-one-route-prevents-another */
	public void test_18_5_FourPointStarWithLoop() throws Exception {
		Glyph a = new Glyph("A", null, "A", null, null);
		Glyph b = new Glyph("B", null, "B", null, null);
		Glyph c = new Glyph("C", null, "C", null, null);
		Glyph d = new Glyph("D", null, "D", null, null);
		Glyph e = new Glyph("E", null, "E", null, null);

		new Link(a, c, null, null, null, null, Direction.RIGHT);
		new Link(b, c, null, null, null, null, Direction.DOWN);
		new Link(d, c, null, null, null, null, Direction.UP);
		new Link(e, c, null, null, null, null, Direction.LEFT);

		// the loop - must go round the outside
		new TurnLink(a, e);

		DiagramXMLElement diag = new DiagramXMLElement("test", createList(a, b, c, d, e), null);

		renderDiagram(diag);

	}

	@Test
	public void test_18_6_UndirectedMultiple() throws Exception {
		Glyph from = new Glyph("from", "From", null, null, null);
		Glyph to = new Glyph("to", "To", null, null, null);

		Arrow a = new Arrow("a", "a");
		Arrow b = new Arrow("b", "b");
		Arrow c = new Arrow("c", "c");
		Arrow d = new Arrow("d", "d");
		Arrow e = new Arrow("e", "e");

		new TurnLink(from, a);
		new TurnLink(from, b);
		new TurnLink(from, c);
		new TurnLink(from, d);
		new TurnLink(from, e);
		new TurnLink(to, a);
		new TurnLink(to, b);
		new TurnLink(to, c);
		new TurnLink(to, d);
		new TurnLink(to, e);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(from, to, a, b, c, d, e), null);

		renderDiagram(diag);

	}

	@Test
	public void test_18_7_TrickyBuddyTop() throws Exception {
		Glyph flat = new Glyph("flat", "flat", null, null, null);
		Glyph buda = new Glyph("buda", "buda", null, null, null);
		Glyph budb = new Glyph("budb", "budb", null, null, null);
		Glyph buda2 = new Glyph("buda2", "buda2", null, null, null);
		Glyph budb2 = new Glyph("budb2", "budb2", null, null, null);
		Glyph mid1 = new Glyph("mid1", "mid1", null, null, null);
		Glyph mid2 = new Glyph("mid2", "mid2", null, null, null);

		new Link(flat, buda, null, null, null, null, Direction.DOWN);
		new Link(flat, budb, null, null, null, null, Direction.DOWN);
		new Link(buda, buda2, null, null, null, null, Direction.DOWN);
		new Link(budb, budb2, null, null, null, null, Direction.DOWN);
		new Link(mid1, mid2, null, null, null, null, Direction.DOWN);

		// tricky part
		new Link(budb2, mid2, null, null, null, null, Direction.LEFT);
		new Link(buda2, mid2, null, null, null, null, Direction.RIGHT);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(flat, buda, budb, buda2, budb2, mid1, mid2), null);

		renderDiagram(diag);

	}

	@Test
	public void test_18_8_TrickyBuddyLeft() throws Exception {
		Glyph flat = new Glyph("flat", "flat", null, null, null);
		Glyph buda = new Glyph("buda", "buda", null, null, null);
		Glyph budb = new Glyph("budb", "budb", null, null, null);
		Glyph buda2 = new Glyph("buda2", "buda2", null, null, null);
		Glyph budb2 = new Glyph("budb2", "budb2", null, null, null);
		Glyph mid1 = new Glyph("mid1", "mid1", null, null, null);
		Glyph mid2 = new Glyph("mid2", "mid2", null, null, null);

		new Link(flat, buda, null, null, null, null, Direction.RIGHT);
		new Link(flat, budb, null, null, null, null, Direction.RIGHT);
		new Link(buda, buda2, null, null, null, null, Direction.RIGHT);
		new Link(budb, budb2, null, null, null, null, Direction.RIGHT);
		new Link(mid1, mid2, null, null, null, null, Direction.RIGHT);

		// tricky part
		new Link(budb2, mid2, null, null, null, null, Direction.UP);
		new Link(buda2, mid2, null, null, null, null, Direction.DOWN);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(flat, buda, budb, buda2, budb2, mid1, mid2), null);

		renderDiagram(diag);

	}

	@Test
	public void test_18_9_FourPointStar4Middle() throws Exception {
		Glyph a = new Glyph("A", "", "A", null, null);
		Glyph b = new Glyph("B", "", "B", null, null);
		Glyph c = new Glyph("C", "", "C", null, null);
		Glyph d = new Glyph("D", "", "D", null, null);
		Glyph m1 = new Glyph("m1", "", "m1", null, null);
		Glyph m2 = new Glyph("m2", "", "m2", null, null);
		Glyph m3 = new Glyph("m3", "", "m3", null, null);
		Glyph m4 = new Glyph("m4", "", "m4", null, null);

		new HopLink(a, m1, null, null, null, null, Direction.RIGHT);
		new HopLink(a, m4, null, null, null, null, Direction.RIGHT);
		new HopLink(b, m1, null, null, null, null, Direction.DOWN);
		new HopLink(b, m2, null, null, null, null, Direction.DOWN);
		new HopLink(c, m4, null, null, null, null, Direction.UP);
		new HopLink(c, m3, null, null, null, null, Direction.UP);
		new HopLink(d, m2, null, null, null, null, Direction.LEFT);
		new HopLink(d, m3, null, null, null, null, Direction.LEFT);
		DiagramXMLElement diag = new DiagramXMLElement("test", createList(a, b, c, d, m1, m2, m3, m4), null);

		renderDiagram(diag);

	}

	@Test
	public void test_18_10_TrickyBuddyTopVariation() throws Exception {
		Glyph flat = new Glyph("flat", "flat", null, null, null);
		Glyph buda = new Glyph("buda", "buda", null, null, null);
		Glyph budb = new Glyph("budb", "budb", null, null, null);
		Glyph buda2 = new Glyph("buda2", "buda2", null, null, null);
		Glyph budb2 = new Glyph("budb2", "budb2", null, null, null);
		Glyph mid1 = new Glyph("mid1", "mid1", null, null, null);
		Glyph mid2 = new Glyph("mid2", "mid2", null, null, null);

		new Link(flat, buda, null, null, null, null, Direction.DOWN);
		new Link(flat, budb, null, null, null, null, Direction.DOWN);
		new Link(buda, buda2, null, null, null, null, Direction.DOWN);
		new Link(budb, budb2, null, null, null, null, Direction.DOWN);
		new Link(mid1, mid2, null, null, null, null, Direction.RIGHT);

		// tricky part
		new Link(budb2, mid2, null, null, null, null, Direction.LEFT);
		new Link(buda2, mid1, null, null, null, null, Direction.RIGHT);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(flat, buda, budb, buda2, budb2, mid1, mid2), null);

		renderDiagram(diag);

	}

	@Test
	public void test_18_11_SimpleBuddyLeft() throws Exception {
		Glyph flat = new Glyph("flat", "flat", null, null, null);
		Glyph buda = new Glyph("buda", "buda", null, null, null);
		Glyph budb = new Glyph("budb", "budb", null, null, null);
		Glyph mid1 = new Glyph("mid1", "mid1", null, null, null);

		new Link(flat, buda, null, null, null, null, Direction.RIGHT);
		new Link(flat, budb, null, null, null, null, Direction.RIGHT);

		// tricky part
		new Link(buda, mid1, null, null, null, null, Direction.UP);
		new Link(budb, mid1, null, null, null, null, Direction.DOWN);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(flat, buda, budb, mid1), null);

		renderDiagram(diag);

	}
	
	@Test
	public void test_18_12_TrickySSingleDirectedMerge() throws Exception {
		Glyph a = new Glyph("a", "a", null, null, null);
		Glyph b = new Glyph("b", "b", null, null, null);
		Context c = new Context("c", emptyContained(), true, null, null);
		Glyph d = new Glyph("d", "d", null, null, null);
		Glyph e = new Glyph("e", "e", null, null, null);
		
		// creating an 'S' shape
		new Link(a, b, null, null, null, null, Direction.RIGHT);
		new Link(b, c, null, null, null, null, Direction.DOWN);
		new Link(c, d, null, null, null, null, Direction.DOWN);
		new Link(d, e, null, null, null, null, Direction.RIGHT);
		
		// tricky bit, crossing the 's' like  a dollar sign
		new Link(a, e, null, null, null, null, Direction.DOWN);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(a, b, c, d, e), null);

		renderDiagram(diag);
	}

	private List<XMLElement> emptyContained() {
		return Collections.emptyList();
	}
	@Test
	public void test_18_13_TrickySSingleDirectedMergeUsingInternalGlyphs() throws Exception {
		Glyph a = new Glyph("a", "a", null, null, null);
		Glyph b = new Glyph("b", "b", null, null, null);

		Glyph c1 =  new Glyph("c1", "c1", null, null, null);
		Glyph c2 =  new Glyph("c2", "c2", null, null, null);
		Context c = new Context("c",createList(c1, c2), true, null, null);
		
		Glyph d = new Glyph("d", "d", null, null, null);
		Glyph e = new Glyph("e", "e", null, null, null);
		
		// creating an 'S' shape
		new Link(a, b, null, null, null, null, Direction.RIGHT);
		new Link(b, c1, null, null, null, null, Direction.DOWN);
		new HopLink(c2, c1, null, null, null, null, Direction.RIGHT);
		
		new Link(c2, d, null, null, null, null, Direction.DOWN);
		new Link(d, e, null, null, null, null, Direction.RIGHT);
		
		// tricky bit, crossing the 's' like  a dollar sign
		new HopLink(a, e, null, null, null, null, Direction.DOWN);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(a, b, c, d, e), null);

		renderDiagram(diag);
	}
	
	@Test
	public void test_18_14_TrickyOverlappingProblem() throws Exception {
		Glyph a = new Glyph("x3y0", "x3y0", null, null, null);
		
		Glyph c1 =  new Glyph("x2y1", "x2y1", null, null, null);
		Glyph c2 =  new Glyph("x4y1", "x4y1", null, null, null);
		Context c = new Context("y1c",createList(c1, c2), true, null, null);
		
		Glyph d1 =  new Glyph("x1y2", "x1y2", null, null, null);
		Glyph d2 =  new Glyph("x3y2", "x3y2", null, null, null);
		Context d = new Context("y2c",createList(d1, d2), true, null, null);
		
		Glyph b1 = new Glyph("x0y0", "x0y0", null, null, null);
		Glyph b2 = new Glyph("x0y1", "x0y1", null, null, null);
		Glyph b3 = new Glyph("x0y2", "x0y2", null, null, null);
		
		new HopLink(c1, c2, null, null, null, null, Direction.RIGHT);
		new HopLink(d1, d2, null, null, null, null, Direction.RIGHT);
		
		new HopLink(a, d2, null, null, null, null, Direction.DOWN);
		
		new Link(b1, a, null, null, null, null, Direction.RIGHT);
		new Link(b2, c, null, null, null, null, Direction.RIGHT);
		new Link(b3, d, null, null, null, null, Direction.RIGHT);
		
		new Link(b1, b2, null, null, null, null, Direction.DOWN);
		new Link(b2, b3, null, null, null, null, Direction.DOWN);
		
		Glyph e1 = new Glyph("x1y3", "x1y3", null, null, null);
		Glyph e2 = new Glyph("x2y3", "x2y3", null, null, null);
		Glyph e3 = new Glyph("x3y3", "x3y3", null, null, null);
		Glyph e4 = new Glyph("x4y3", "x4y3", null, null, null);
		
		new Link(d1, e1, null, null, null, null, Direction.DOWN);
		new HopLink(c1, e2, null, null, null, null, Direction.DOWN);
		new Link(d2, e3, null, null, null, null, Direction.DOWN);
		new Link(c2, e4, null, null, null, null, Direction.DOWN);
		
		new Link(e1, e2, null, null, null, null, Direction.RIGHT);
		new Link(e2, e3, null, null, null, null, Direction.RIGHT);
		new Link(e3, e4, null, null, null, null, Direction.RIGHT);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(a, b1, c, d, b2, b3, e1, e2, e3, e4), null);

		renderDiagram(diag);
	}
	
	@Test
	public void test_18_16_FlemishBond() throws Exception {
		Glyph a1 = new Glyph("a1", "a1", null, null, null);
		Glyph a2 = new Glyph("a2", "a2", null, null, null);
		Glyph a3 = new Glyph("a3", "a3", null, null, null);
		Glyph a4 = new Glyph("a4", "a4", null, null, null);
		Glyph a5 = new Glyph("a5", "a5", null, null, null);
		Glyph b1 = new Glyph("b1", "b1", null, null, null);
		Glyph b2 = new Glyph("b2", "b2", null, null, null);
		Glyph b3 = new Glyph("b3", "b3", null, null, null);
		Glyph b4 = new Glyph("b4", "b4", null, null, null);
		Glyph b5 = new Glyph("b5", "b5", null, null, null);
		
		Context ac1 = new Context("ac1", HelpMethods.listOf(a1, a2), true, null, Layout.RIGHT);
		Context ac2 = new Context("ac2", HelpMethods.listOf(a3, a4), true, null, Layout.RIGHT);
		Context a = new Context("a", HelpMethods.listOf(ac1, ac2, a5), true, null, Layout.RIGHT);
		
		Context bc1 = new Context("bc1", HelpMethods.listOf(b2, b3), true, null, Layout.RIGHT);
		Context bc2 = new Context("bc2", HelpMethods.listOf(b4, b5), true, null, Layout.RIGHT);
		Context b = new Context("b", HelpMethods.listOf(b1, bc1, bc2), true, null, Layout.RIGHT);
		
		new Link(a1, b1, null, null, null, null, Direction.DOWN);
		new Link(a2, b2, null, null, null, null, Direction.DOWN);
		new Link(a3, b3, null, null, null, null, Direction.DOWN);
		new Link(a4, b4, null, null, null, null, Direction.DOWN);
		new Link(a5, b5, null, null, null, null, Direction.DOWN);
		
		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(a, b ), Layout.DOWN,null);

		renderDiagram(diag);
	}
	
	@Test
	public void test_18_17_UnavoidableCrossing() throws Exception {
		Glyph a = new Glyph("A", "", "A", null, null);
		Glyph b = new Glyph("B", "", "B", null, null);
		Glyph c = new Glyph("C", "", "C", null, null);
		Glyph d = new Glyph("D", "", "D", null, null);
		
		Glyph m1 = new Glyph("m1", "", "m1", null, null);
		Glyph m2 = new Glyph("m2", "", "m2", null, null);
		Glyph m3 = new Glyph("m3", "", "m3", null, null);
		Glyph m4 = new Glyph("m4", "", "m4", null, null);

		new HopLink(a, c, null, null, null, null, Direction.RIGHT);
		new HopLink(a, b, null, null, null, null, Direction.RIGHT);
		new HopLink(b, m1, null, null, null, null, Direction.UP);
		new HopLink(b, m3, null, null, null, null, Direction.DOWN);
		new HopLink(c, m2, null, null, null, null, Direction.UP);
		new HopLink(c, m4, null, null, null, null, Direction.DOWN);
		new HopLink(m1, m2, null, null, null, null, Direction.LEFT);
		new HopLink(m3, m4, null, null, null, null, Direction.LEFT);
		
		// trickier...
		new Link(b, d, null, null, null, null, Direction.RIGHT);
		new HopLink(c, d, null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement diag = new DiagramXMLElement("test", createList(a, b, c, d, m1, m2, m3, m4), null);

		renderDiagram(diag);

	}
	
	@Test
	public void test_18_18_CuriousNeighbours1() throws Exception {
		Glyph a = new Glyph("A", "", "A", null, null);
		Glyph b = new Glyph("B", "", "B", null, null);
		Glyph c = new Glyph("C", "", "C", null, null);
		Glyph d = new Glyph("D", "", "D", null, null);
		
		Glyph m1 = new Glyph("m1", "", "m1", null, null);
		Glyph m2 = new Glyph("m2", "", "m2", null, null);


		new Link(a, c, null, null, null, null, Direction.DOWN);
		new Link(a, b, null, null, null, null, Direction.DOWN);
		new Link(c, m1, null, null, null, null, Direction.RIGHT);
		new Link(d, m2, null, null, null, null, Direction.RIGHT);
		new Link(m1, m2, null, null, null, null, Direction.DOWN);
		new Link(d, b, null, null, null, null, Direction.DOWN);
		
		DiagramXMLElement diag = new DiagramXMLElement("test", createList(a, b, c, d, m1, m2), null);

		renderDiagram(diag);

	}
	
	@Test
	public void test_18_19_CuriousNeighbours2() throws Exception {
		Glyph a = new Glyph("A", "", "A", null, null);
		Glyph b = new Glyph("B", "", "B", null, null);
		Glyph c = new Glyph("C", "", "C", null, null);
		Glyph d = new Glyph("D", "", "D", null, null);
		
		Glyph e = new Glyph("E", "", "E", null, null);

		new Link(a, c, null, null, null, null, Direction.DOWN);
		new Link(a, b, null, null, null, null, Direction.DOWN);
		new Link(c, e, null, null, null, null, Direction.RIGHT);
		new Link(d, e, null, null, null, null, Direction.RIGHT);
		new Link(d, b, null, null, null, null, Direction.DOWN);
		
		DiagramXMLElement diag = new DiagramXMLElement("test", createList(a, b, c, d, e), null);

		renderDiagram(diag);

	}
	
	@Test
	public void test_18_20_PavementContainers() throws Exception {
		Glyph tl = new Glyph("tl", "", "tl", null, null);
		Glyph tm = new Glyph("tm", "", "tm", null, null);
		Glyph tr = new Glyph("tr", "", "tr", null, null);
		Glyph ml = new Glyph("ml", "", "ml", null, null);
		Glyph mm = new Glyph("mm", "", "mm", null, null);
		Glyph mr = new Glyph("mr", "", "mr", null, null);
		Glyph bl = new Glyph("bl", "", "bl", null, null);
		Glyph bm = new Glyph("bm", "", "bm", null, null);
		Glyph br = new Glyph("br", "", "br", null, null);
		
		
		new Link(tl, tm, null, null, null, null, Direction.RIGHT);
		new Link(tm, tr, null, null, null, null, Direction.RIGHT);
		
		new Link(ml, mm, null, null, null, null, Direction.RIGHT);
		new Link(mm, mr, null, null, null, null, Direction.RIGHT);
		
		new Link(bl, bm, null, null, null, null, Direction.RIGHT);
		new Link(bm, br, null, null, null, null, Direction.RIGHT);
		
		new Link(tl, ml, null, null, null, null, Direction.DOWN);
		new Link(ml, bl, null, null, null, null, Direction.DOWN);

		new Link(tm, mm, null, null, null, null, Direction.DOWN);
		new Link(mm, bm, null, null, null, null, Direction.DOWN);

		new Link(tr, mr, null, null, null, null, Direction.DOWN);
		new Link(mr, br, null, null, null, null, Direction.DOWN);
		
		Context ac1 = new Context("up", HelpMethods.listOf(tl, tm), true, null, null);
		Context ac2 = new Context("right", HelpMethods.listOf(tr, mr), true, null,null);
		Context ac3 = new Context("left", HelpMethods.listOf(ml, bl), true, null, null);
		Context ac4 = new Context("bottom", HelpMethods.listOf(bm, br), true, null, null);

		DiagramXMLElement diag = new DiagramXMLElement("test", createList(ac1, ac2, ac3, ac4, mm), null);

		renderDiagram(diag);
	}
	
	@Test
	public void test_18_21_BuddyOutsideContainer() throws Exception {
		Glyph a = new Glyph("A", "", "A", null, null);
		Glyph b = new Glyph("B", "", "B", null, null);
		Glyph c = new Glyph("C", "", "C", null, null);
		
		Glyph one = new Glyph("1", "", "1", null, null);
		Glyph two = new Glyph("2", "", "2", null, null);
		Glyph three = new Glyph("3", "", "3", null, null);
		Glyph four = new Glyph("4", "", "4", null, null);
		
		
		Context ac1 = new Context("up", HelpMethods.listOf(a, b, c), true, null, Layout.DOWN);
		new Link(a, one, null, null, null, null, Direction.UP);
		new Link(a, three, null, null, null, null, Direction.DOWN);
		new Link(c, two, null, null, null, null, Direction.UP);
		new Link(c, four, null, null, null, null, Direction.DOWN);

		new Link(one, two, null, null, null, null, Direction.RIGHT);
		new Link(three, four, null, null, null, null, Direction.RIGHT);
		
//		new Link(a, b, null, null, null, null, Direction.RIGHT);
//		new Link(b, c, null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement diag = new DiagramXMLElement("test", createList(ac1, one, two, three, four), null);

		renderDiagram(diag);
	}
	
	@Test
	@NotAddressed("Goes wrong sometimes, not identified why yet")
	public void test_18_22_WrongVerticalOrdering() throws Exception {
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b1 = new Glyph("b1", "", "b1", null, null);
		Glyph b2 = new Glyph("b2", "", "b2", null, null);
		
		Glyph c1 = new Glyph("c1", "", "c1", null, null);
		Glyph c2 = new Glyph("c2", "", "c2", null, null);
		Glyph c3 = new Glyph("c3", "", "c3", null, null);
		
		Glyph d = new Glyph("d", "", "d", null, null);
		Glyph longone = new Glyph("long", "", "very long glyph to keep everything separate", null, null);
		
		new Link(a,b1, null, null, null, null, Direction.RIGHT);
		new Link(b1,b2, null, null, null, null, Direction.RIGHT);
		new Link(b2,d, null, null, null, null, Direction.RIGHT);
		
		new Link(a,c1, null, null, null, null, Direction.RIGHT);
		new Link(c1,c2, null, null, null, null, Direction.RIGHT);
		new Link(c2,c3, null, null, null, null, Direction.RIGHT);
		new Link(c3,d, null, null, null, null, Direction.RIGHT);
		
		
		// try taking this one out
		new Link(c3,b2, null, null, null, null, Direction.RIGHT);
		
		new Link(a,longone, null, null, null, null, Direction.RIGHT);
		new Link(longone,d, null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement dia = new DiagramXMLElement("dia", HelpMethods.listOf(a, b1, b2, c1, c2, c3, d, longone), null);
		renderDiagram(dia);
	}
}
