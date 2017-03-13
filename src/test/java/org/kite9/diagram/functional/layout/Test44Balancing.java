package org.kite9.diagram.functional.layout;

import org.junit.Test;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.AbstractLayoutFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.HelpMethods;

public class Test44Balancing extends AbstractLayoutFunctionalTest {

	@Test
	public void test_44_1_SingleGlyph() throws Exception {
		Glyph a = new Glyph("a", "", "a", null, null);
		DiagramXMLElement d = new DiagramXMLElement("d", HelpMethods.listOf(a), new Key("this is a very long piece of text", "", null));
		renderDiagram(d);
	}
	
	@Test
	public void test_44_2_UnjoinedGlyphs() throws Exception {
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		DiagramXMLElement d = new DiagramXMLElement("d", HelpMethods.listOf(a, b), new Key("this is a very long piece of text", "", null));
		d.setLayoutDirection(Layout.RIGHT);
		renderDiagram(d);
	}
	
	@Test
	public void test_44_3_JoinedGlyphs() throws Exception {
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		new Link(a,b);
		DiagramXMLElement d = new DiagramXMLElement("d", HelpMethods.listOf(a, b), new Key("this is a very long piece of text", "", null));
		d.setLayoutDirection(Layout.RIGHT);
		renderDiagram(d);
	}
	
	@Test
	public void test_44_4_SimpleV() throws Exception {
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		Glyph c = new Glyph("c", "", "c", null, null);
		
		new Link(a,b, null, null, null, null, Direction.RIGHT);
		new Link(a,c, null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement d = new DiagramXMLElement("d", HelpMethods.listOf(a, b, c), new Key("this is a very long piece of text", "", null));
		renderDiagram(d);
	}
	
	@Test
	public void test_44_5_Optimisable() throws Exception {
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b1 = new Glyph("b1", "", "b1", null, null);
		Glyph b2 = new Glyph("b2", "", "b2", null, null);
		Glyph c = new Glyph("c", "", "c", null, null);
		Glyph longone = new Glyph("long", "", "very long glyph to keep everything separate", null, null);
		
		new Link(a,b1, null, null, null, null, Direction.RIGHT);
		new Link(a,b2, null, null, null, null, Direction.RIGHT);
		
		new Link(b1,b2, null, null, null, null, Direction.RIGHT);
		
		// 2 links from b2 to c - should optimise to reduce that.
		new Link(b1,c, null, null, null, null, Direction.RIGHT);
		new Link(b2,c, null, null, null, null, Direction.RIGHT);
		new Link(b2,c, null, null, null, null, Direction.RIGHT);
		
		new Link(a,longone, null, null, null, null, Direction.RIGHT);
		new Link(longone,c, null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement d = new DiagramXMLElement("d", HelpMethods.listOf(a, b1, b2, c, longone), null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_44_6_SharableSlack() throws Exception {
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b1 = new Glyph("b1", "", "b1", null, null);
		Glyph b2 = new Glyph("b2", "", "b2", null, null);
		
		Glyph c1 = new Glyph("c1", "", "c1", null, null);
		Glyph c2 = new Glyph("c2", "", "c2", null, null);
		Glyph c3 = new Glyph("c3", "", "c3", null, null);
		
		Glyph d = new Glyph("d", "", "d", null, null);
		Glyph longone = new Glyph("long", "", "very long glyph to keep everything separate", null, null);
		
		new Link(a,b1, null, null, null, null, Direction.RIGHT);
		new Link(b1,b2, null, null, null, null, Direction.RIGHT);
		new Link(b2,d, null, null, null, null, Direction.RIGHT);
		
		new Link(a,c1, null, null, null, null, Direction.RIGHT);
		new Link(c1,c2, null, null, null, null, Direction.RIGHT);
		new Link(c2,c3, null, null, null, null, Direction.RIGHT);
		new Link(c3,d, null, null, null, null, Direction.RIGHT);
				
		new Link(a,longone, null, null, null, null, Direction.RIGHT);
		new Link(longone,d, null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement dia = new DiagramXMLElement("dia", HelpMethods.listOf(a, b1, b2, c1, c2, c3, d, longone), null);
		renderDiagram(dia);
	}
	
 	
}
