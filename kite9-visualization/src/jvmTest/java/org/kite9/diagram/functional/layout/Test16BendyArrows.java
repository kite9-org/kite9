package org.kite9.diagram.functional.layout;

import org.junit.jupiter.api.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;

public class Test16BendyArrows extends AbstractLayoutFunctionalTest {

	@Test
	public void test_16_1_BendyArrows() throws Exception {
		Glyph a = new Glyph("", "a", null, null);
		Glyph b = new Glyph("", "b", null, null);
		Glyph c = new Glyph("", "c", null, null);
		LinkBody ab = new LinkBody("ab");
		LinkBody bc = new LinkBody("bc");
		LinkBody ac = new LinkBody("ac");

		new Link(ab, a);
		new Link(ab, b);
		new Link(ac, a);
		new Link(ac, c);
		new Link(bc, b);
		new Link(bc, c);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("bendy", createList(a, b, c, ab, ac, bc), null);
		renderDiagram(d);
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

}
