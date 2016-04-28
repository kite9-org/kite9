package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.Contained;

public class Test14ContainerOrdering extends AbstractFunctionalTest {

	// the players

	Glyph one;
	Glyph two;
	Glyph three;
	Glyph four;

	Context con1;
	Arrow outside;
	Diagram d;

	@Before
	public void setUp() {
		one = new Glyph("a0", "", "a0", null, null);
		two = new Glyph("a1", "", "a1", null, null);
		three = new Glyph("a2", "", "a2", null, null);
		four = new Glyph("a3", "", "a3", null, null);
		con1 = new Context("b1", createList((Contained) one, (Contained) two, (Contained) three, (Contained) four),
				true, new TextLine("inside"),  Layout.RIGHT);
		outside = new Arrow("outside", "outside");
		d = new Diagram("The Diagram", createList((Contained) con1, (Contained) outside), null);
	}

	@Test
	public void test_14_1_OneOutsideConnection() throws IOException {
		new Link(outside, two);
		renderDiagram(d);
	}
	
	@Test
	public void test_14_2_OneInsideConnection() throws IOException {
		new Link(one, three);
		renderDiagram(d);
	}
	
	@Test
	public void test_14_3_OneInsideOneOutside() throws IOException {
		new Link(outside, three);
		new Link(one, four);
		renderDiagram(d);
	}
	
	@Test
	public void test_14_4_OneInsideAndOutside() throws IOException {
		new Link(outside, two);
		new Link(two, four);
		renderDiagram(d);
	}
	
	@Test
	/**
	 * @see http://www.kite9.com/content/planarization-no-merges-available-145
	 */
	public void test_14_5_TwoSeparateInside() throws IOException {
		new Link(one, three);
		new Link(two, four);
		renderDiagram(d);
	}
	
	@Test
	public void test_14_6_TwoJoinedInside() throws IOException {
		new Link(one, three);
		new Link(three, four);
		renderDiagram(d);
	}
	
	@Test
	public void test_14_7_contextDirection() throws IOException {
		Glyph[] g = new Glyph[12];
		for (int i = 0; i < g.length; i++) {
			g[i] = new Glyph(""+i, null, ""+i, null, null);
		}
	
		Context leftToRight = new Context(listOf(g[0], g[1], g[2]), true, new TextLine("Left to Right"), Layout.RIGHT);
		Context bottomToTop = new Context(listOf(g[3], g[4], g[5]), true, new TextLine("Bottom to Top"), Layout.UP);
		Context topToBottom = new Context(listOf(g[6], g[7], g[8]), true, new TextLine("Top to Bottom"), Layout.DOWN);
		Context rightToLeft = new Context(listOf(g[9], g[10], g[11]), true, new TextLine("Right to Left"), Layout.LEFT);
		
		Diagram d1 = new Diagram("my_diagram", listOf(leftToRight, bottomToTop, topToBottom, rightToLeft), null);
		
		renderDiagram(d1);
		
	}
	
	@Test
	public void test_14_8_PileOnFour() throws IOException {
		new Link(outside, four);
		new Link(one, four);
		new Link(two, four);
		new Link(three, four);
		renderDiagram(d);
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}
	
	
}
