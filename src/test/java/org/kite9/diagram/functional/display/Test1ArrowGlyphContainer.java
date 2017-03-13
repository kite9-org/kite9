package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.functional.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;

public class Test1ArrowGlyphContainer extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_1_1_Glyph() throws Exception {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one));
		renderDiagram(d);
	}

	@Test
	public void test_1_2_GlyphInContainerFinal() throws Exception {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		XMLElement con = new Context("Context", createList(one), true, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}

	@Test
	public void test_1_3_TwoGlyphsFinal() throws Exception {
		XMLElement one = new Glyph("Stereo", "One", null, null);
		XMLElement two = new Glyph("Stereo", "Two", null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_4_GlyphSymbolsAndText() throws Exception {
		XMLElement one = new Glyph("Stereo", "One", 
			listOf(new TextLine("Line 1"), new TextLine("Second Line")), 
			listOf(new Symbol("Sym1", 'a', SymbolShape.CIRCLE),
					new Symbol("Sym2", 'f', SymbolShape.CIRCLE)));
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_5_TwoArrowsFinal() throws Exception {
		XMLElement one = new Arrow("One");
		XMLElement two = new Arrow("Two");
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two));
		renderDiagram(d);
	}

	@Test
	public void test_1_6_TwoGlyphsInContainerFinal() throws Exception {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		XMLElement two = new Glyph("Stereo", "Two", null, null);
		XMLElement con = new Context("Context", createList(one, two), true, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_7_TwoArrowsInContainerFinal() throws Exception {
		XMLElement one = new Arrow("One");
		XMLElement two = new Arrow("Two");
		XMLElement con = new Context("Context", createList(one, two), true, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_8_EmptyGlyph() throws Exception {
		XMLElement one = new Glyph(null, null, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one));

		renderDiagram(d);
	}
}
