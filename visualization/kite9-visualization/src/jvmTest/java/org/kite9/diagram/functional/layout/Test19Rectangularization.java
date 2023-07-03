package org.kite9.diagram.functional.layout;

import org.junit.Before;
import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.model.position.Direction;

public class Test19Rectangularization extends AbstractLayoutFunctionalTest {

	Glyph a;
	Glyph b;
	Glyph c;
	Glyph d;

	LinkBody a1;
	LinkBody b1;
	LinkBody c1;
	LinkBody d1;

	@Before
	public void setUp() {
		a = new Glyph("A", "", "A", null, null);
		b = new Glyph("B", "", "B", null, null);
		c = new Glyph("C", "", "C", null, null);
		d = new Glyph("D", "", "D", null, null);

		a1 = new LinkBody("a1","a1");
		b1 = new LinkBody("b1", "b1");
		c1 = new LinkBody("c1", "c1");
		d1 = new LinkBody("d1", "d1");
	}

	@Test
	public void test_19_1_LinksOnDifferentSides() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		
		new Link(a, a1, null, null, null, null, Direction.UP);
		new Link(b, b1, null, null, null, null, Direction.LEFT);
		new Link(c, c1, null, null, null, null, Direction.RIGHT);
		new Link(d, d1, null, null, null, null, Direction.DOWN);
		
		DiagramKite9XMLElement diag = new DiagramKite9XMLElement("blah", createList(inner, a1, b1, c1, d1), null);
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_2_LinksOnSameSides() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		
		new Link(a, a1, null, null, null, null, Direction.UP);
		new Link(b, b1, null, null, null, null, Direction.DOWN);
		new Link(c, c1, null, null, null, null, Direction.UP);
		new Link(d, d1, null, null, null, null, Direction.DOWN);
		DiagramKite9XMLElement diag = new DiagramKite9XMLElement("blah", createList(inner, a1, b1, c1, d1), null);
		
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_3_UndirectedLinks() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		
		new Link(a, a1);
		new Link(b, b1);
		new Link(c, c1);
		new Link( d, d1);
		
		DiagramKite9XMLElement diag = new DiagramKite9XMLElement("blah", createList(inner, a1, b1, c1, d1), null);
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_4_TallArrow() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		
		new Link(a, a1, null, null, null, null, Direction.RIGHT);
		new TurnLink(b, a1);
		new TurnLink(c, a1);
		new TurnLink(d, a1);
		
		DiagramKite9XMLElement diag = new DiagramKite9XMLElement("blah", createList(inner, a1), null);
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_5_WideArrow() throws Exception {
		Context inner = new Context("inner", createList(a, b, c, d), true, null, null);
		
		new Link(a, a1, null, null, null, null, Direction.UP);
		new TurnLink(b, a1);
		new TurnLink(c, a1);
		new TurnLink(d, a1);
		
		DiagramKite9XMLElement diag = new DiagramKite9XMLElement("blah", createList(inner, a1), null);
		
		renderDiagram(diag);
	}
	
	@Test
	public void test_19_6_WideAndTallArrow() throws Exception {
		
		new Link(a, a1, null, null, null, null, Direction.UP);
		new Link(b, a1, null, null, null, null, Direction.UP);
		new Link(c, a1, null, null, null, null, Direction.RIGHT);
		new Link(d, a1, null, null, null, null, Direction.RIGHT);
		
		DiagramKite9XMLElement diag = new DiagramKite9XMLElement("blah", createList(a,b,c,d, a1), null);
		
		renderDiagram(diag);
	}
}
