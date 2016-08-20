package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.xml.DiagramXMLElement;

public class Test16BendyArrows extends AbstractFunctionalTest {

	@Test
	public void test_16_1_BendyArrows() throws IOException {
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
		
		DiagramXMLElement d= new DiagramXMLElement("bendy", createList((Contained) a, b, c, ab, ac, bc), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_16_2_Container() throws IOException {
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
		
		DiagramXMLElement d= new DiagramXMLElement("bendy", createList((Contained) a, b, c, ab, ac, bc), null);
		renderDiagram(d);
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}
	
	
}
