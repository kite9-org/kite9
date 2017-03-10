package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.layout.AbstractLayoutFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;

public class Test53BorderTraversal extends AbstractLayoutFunctionalTest {

	@Test
	public void test_53_1_MustLeaveTop() throws IOException {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g6 = new Glyph("six", "","six ", null, null);
		Context c5 = new Context("five", listOf(g6), true, null, null);
		c5.setStyle("traversal: always none none none; ");
		
		Link l = new Link(g2, g6);

		DiagramXMLElement d = new DiagramXMLElement("diagram", Arrays.asList(g1, c5, g2, g3), Layout.RIGHT, null);
		renderDiagram(d);
		mustTurn(d, l);
	}

	@Test
	public void test_53_2_LeavingOnly() throws IOException {
		Glyph a = new Glyph("a", "a", null, null, null);
		Glyph b = new Glyph("b", "b", null, null, null);
		Glyph inner = new Glyph("inner", "inner", null, null, null);
		Context c = new Context("c", Arrays.asList(inner), true, null, null);
		c.setStyle("traversal: leaving; ");
		Glyph d = new Glyph("d", "d", null, null, null);
		Glyph e = new Glyph("e", "e", null, null, null);
		
		// creating an 'S' shape
		new Link(a, b, null, null, null, null, Direction.RIGHT);
		new Link(b, c, null, null, null, null, Direction.DOWN);
		new Link(c, d, null, null, null, null, Direction.DOWN);
		new Link(d, e, null, null, null, null, Direction.RIGHT);
		new Link(inner, e);
		
		// tricky bit, crossing the 's' like  a dollar sign - won't be allowed here
		Link l = new ContradictingLink(a, e, null, null, null, null, Direction.DOWN);
		l.setRank(-1000);

		DiagramXMLElement diag = new DiagramXMLElement("dia", createList(a, b, c, d, e), null);
		renderDiagram(diag);

		mustContradict(diag, l);
		
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Override
	protected boolean checkNoHops() {
		return false;
	}
	
	
}
