package org.kite9.diagram.functional.layout;

import java.util.Arrays;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.ContradictingLink;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.xml.DiagramKite9XMLElement;

public class Test53BorderTraversal extends AbstractLayoutFunctionalTest {

	@Test
	public void test_53_1_MustLeaveTop() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g6 = new Glyph("six", "","six ", null, null);
		Context c5 = new Context("five", listOf(g6), true, null, null);
		c5.setAttribute("style", "traversal: always none none none; ");
		
		Link l = new Link(g2, g6);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("diagram", Arrays.asList(g1, c5, g2, g3), Layout.RIGHT, null);
		d = renderDiagram(d);
		mustTurn(d, l);
	}

	@Test
	public void test_53_2_LeavingOnly() throws Exception {
		Glyph a = new Glyph("a", "a", null, null, null);
		Glyph b = new Glyph("b", "b", null, null, null);
		Glyph inner = new Glyph("inner", "inner", null, null, null);
		Context c = new Context("c", Arrays.asList(inner), true, null, null);
		c.setAttribute("style", "traversal: leaving; ");
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

		DiagramKite9XMLElement diag = new DiagramKite9XMLElement("dia", createList(a, b, c, d, e), null);
		diag = renderDiagram(diag);

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
