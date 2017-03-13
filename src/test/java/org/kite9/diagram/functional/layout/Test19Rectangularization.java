package org.kite9.diagram.functional.layout;

import org.junit.Before;
import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.functional.AbstractLayoutFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.DiagramXMLElement;

public class Test19Rectangularization extends AbstractLayoutFunctionalTest {

	Glyph a;
	Glyph b;
	Glyph c;
	Glyph d;

	Arrow a1;
	Arrow b1;
	Arrow c1;
	Arrow d1;

	@Before
	public void setUp() {
		a = new Glyph("A", "", "A", null, null);
		b = new Glyph("B", "", "B", null, null);
		c = new Glyph("C", "", "C", null, null);
		d = new Glyph("D", "", "D", null, null);

		a1 = new Arrow("a1","a1");
		b1 = new Arrow("b1", "b1");
		c1 = new Arrow("c1", "c1");
		d1 = new Arrow("d1", "d1");
	}

	@Test
	public void test_19_1_LinksOnDifferentSides() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		DiagramXMLElement diag = new DiagramXMLElement("blah", createList(inner, a1, b1, c1, d1), null);
		
		new Link(a, a1, null, null, null, null, Direction.UP);
		new Link(b, b1, null, null, null, null, Direction.LEFT);
		new Link(c, c1, null, null, null, null, Direction.RIGHT);
		new Link(d, d1, null, null, null, null, Direction.DOWN);
		
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_2_LinksOnSameSides() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		DiagramXMLElement diag = new DiagramXMLElement("blah", createList(inner, a1, b1, c1, d1), null);
		
		new Link(a, a1, null, null, null, null, Direction.UP);
		new Link(b, b1, null, null, null, null, Direction.DOWN);
		new Link(c, c1, null, null, null, null, Direction.UP);
		new Link(d, d1, null, null, null, null, Direction.DOWN);
		
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_3_UndirectedLinks() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		DiagramXMLElement diag = new DiagramXMLElement("blah", createList(inner, a1, b1, c1, d1), null);
		
		new Link(a, a1);
		new Link(b, b1);
		new Link(c, c1);
		new Link( d, d1);
		
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_4_TallArrow() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		DiagramXMLElement diag = new DiagramXMLElement("blah", createList(inner, a1), null);
		
		new Link(a, a1, null, null, null, null, Direction.RIGHT);
		new TurnLink(b, a1);
		new TurnLink(c, a1);
		new TurnLink(d, a1);
		
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_5_WideArrow() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		DiagramXMLElement diag = new DiagramXMLElement("blah", createList(inner, a1), null);
		
		new Link(a, a1, null, null, null, null, Direction.UP);
		new TurnLink(b, a1);
		new TurnLink(c, a1);
		new TurnLink(d, a1);
		
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_6_WideAndTallArrow() throws Exception {
		DiagramXMLElement diag = new DiagramXMLElement("blah", createList(a,b,c,d, a1), null);
		
		new Link(a, a1, null, null, null, null, Direction.UP);
		new Link(b, a1, null, null, null, null, Direction.UP);
		new Link(c, a1, null, null, null, null, Direction.RIGHT);
		new Link(d, a1, null, null, null, null, Direction.RIGHT);
		
		
		renderDiagram(diag);
	}
}
