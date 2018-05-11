package org.kite9.diagram.functional.layout;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.NotAddressed;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.dom.elements.DiagramKite9XMLElement;
import org.kite9.diagram.model.position.Direction;

public class Test48PlanarizationProblems extends AbstractLayoutFunctionalTest {



	@Test
	@NotAddressed
	public void test_48_1_PavingProblem() throws Exception {
		Glyph a1 = new Glyph("a1", "a1", null, null, null);
		Glyph a2 = new Glyph("a2", "a2", null, null, null);
		Glyph a3 = new Glyph("a3", "a3", null, null, null);
		Glyph a4 = new Glyph("a4", "a4", null, null, null);
		Glyph a5 = new Glyph("a5", "a5", null, null, null);
		Glyph a6 = new Glyph("a6", "a6", null, null, null);
		Glyph a7 = new Glyph("a7", "a7", null, null, null);
		Glyph a8 = new Glyph("a8", "a8", null, null, null);
		Glyph a9 = new Glyph("a9", "a9", null, null, null);
		Glyph a10 = new Glyph("a10", "a10", null, null, null);
		Glyph a11 = new Glyph("a11", "a11", null, null, null);
		
		new Link(a1, a2, null, null, null, null, Direction.RIGHT);
		new Link(a2, a3, null, null, null, null, Direction.RIGHT);
		new Link(a3, a4, null, null, null, null, Direction.RIGHT);
		new Link(a5, a6, null, null, null, null, Direction.RIGHT);
		new Link(a6, a3, null, null, null, null, Direction.RIGHT);
		new Link(a3, a7, null, null, null, null, Direction.RIGHT);
		new Link(a7, a8, null, null, null, null, Direction.RIGHT);
		new Link(a5, a9, null, null, null, null, Direction.RIGHT);
		new Link(a9, a10, null, null, null, null, Direction.RIGHT);
		new Link(a10, a7, null, null, null, null, Direction.RIGHT);
		new Link(a11, a9, null, null, null, null, Direction.RIGHT);
		
		new Link(a1, a5, null, null, null, null, Direction.DOWN);
		new Link(a1, a6, null, null, null, null, Direction.DOWN);
		new Link(a5, a11, null, null, null, null, Direction.DOWN);
		new Link(a2, a6, null, null, null, null, Direction.DOWN);
		new Link(a6, a9, null, null, null, null, Direction.DOWN);
		new Link(a6, a10, null, null, null, null, Direction.DOWN);
		new Link(a3, a10, null, null, null, null, Direction.DOWN);
		new Link(a4, a7, null, null, null, null, Direction.DOWN);
		new Link(a4, a8, null, null, null, null, Direction.DOWN);
		
		
		
		
		DiagramKite9XMLElement diag = new DiagramKite9XMLElement("dia", createList(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11), null);

		renderDiagram(diag);
	}
	
}
