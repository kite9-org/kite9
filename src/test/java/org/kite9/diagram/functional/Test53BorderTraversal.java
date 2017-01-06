package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.visualization.format.pos.DiagramChecker;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.logging.LogicException;

public class Test53BorderTraversal extends AbstractFunctionalTest {

	@Test
	public void test_53_1_MustLeaveTop() throws IOException {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g6 = new Glyph("six", "","six ", null, null);
		Context c5 = new Context("five", listOf(g6), true, null, null);
		c5.setStyle("traversal: always none none none; ");
		
		new Link(g2, g6);

		DiagramXMLElement d = new DiagramXMLElement("diagram", Arrays.asList(g1, c5, g2, g3), Layout.RIGHT, null);
		renderDiagram(d);
		DiagramChecker.checkConnnectionElements(d, new DiagramChecker.ConnectionAction() {
			
			@Override
			public void action(RouteRenderingInformation rri, Object d, Connection c) {
				if (d != DiagramChecker.MULTIPLE_DIRECTIONS) {
					throw new LogicException("Should be turning");
				}
			}
		});
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

		DiagramChecker.checkConnnectionElements(diag, new DiagramChecker.ConnectionAction() {
			
			@Override
			public void action(RouteRenderingInformation rri, Object d, Connection c) {
				if (c == l) {
					if (d != DiagramChecker.SET_CONTRADICTING) {
						throw new LogicException("Should be contradicting");
					}
				}
			}
		});
		
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
