package org.kite9.diagram.functional.layout;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.dom.elements.DiagramKite9XMLElement;

public class Test16BendyArrows extends AbstractLayoutFunctionalTest {

	@Test
	public void test_16_1_BendyArrows() throws Exception {
		Glyph a = new Glyph("", "a", null, null);
		Glyph b = new Glyph("", "b", null, null);
		Glyph c = new Glyph("", "c", null, null);
		Arrow ab= new Arrow("ab");
		Arrow bc= new Arrow("bc");
		Arrow ac= new Arrow("ac");
		
		new Link(ab, a);
		new Link(ab, b);
		new Link(ac, a);
		new Link(ac, c);
		new Link(bc, b);
		new Link(bc, c);
		
		DiagramKite9XMLElement d= new DiagramKite9XMLElement("bendy", createList(a, b, c, ab, ac, bc), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_16_2_Container() throws Exception {
		Glyph a = new Glyph("", "a", null, null);
		Glyph b = new Glyph("", "b", null, null);
		Glyph c = new Glyph("", "c", null, null);
		Arrow ab= new Arrow("ab");
		Arrow bc= new Arrow("bc");
		Arrow ac= new Arrow("ac");
		
		new Link(ab, a);
		new Link( ab, b);
		new Link(ac, a);
		new Link(ac, c);
		new Link(bc, b);
		new Link(bc, c);
		
		DiagramKite9XMLElement d= new DiagramKite9XMLElement("bendy", createList(a, b, c, ab, ac, bc), null);
		renderDiagram(d);
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}
	
	
}
