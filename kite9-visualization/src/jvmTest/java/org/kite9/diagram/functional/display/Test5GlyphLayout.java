package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.adl.TextLine;

public class Test5GlyphLayout extends AbstractDisplayFunctionalTest {

	@Test
	public void test_5_1_SimpleGlyphFinal() throws Exception {
		Glyph one = new Glyph("", "Rob's Glyph", null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagram(d);
	}

	@Test
	public void test_5_2_TypedGlyphFinal() throws Exception {
		Glyph one = new Glyph("Type", "Rob's Glyph", null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);

		renderDiagram(d);
	}

	@Test
	public void test_5_3_GlyphWithTextFinal() throws Exception {
		Glyph one = new Glyph("Stereo", "One", createList(new TextLine("Here is line 1"), new TextLine(
				"Here is line 2")), null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagram(d);
	}

	@Test
	public void test_5_4_GlyphWithSymbol() throws Exception {
		Symbol s = new Symbol("Some text", 'a', SymbolShape.CIRCLE);
		Glyph one = new Glyph("Stereo", "One", null, createList(s, s,
				new Symbol("Some text", 'B', SymbolShape.DIAMOND), new Symbol("Some text", 'm', SymbolShape.HEXAGON)));
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagram(d);
	}

	@Test
	public void test_5_5_GlyphWithTextSymbol() throws Exception {
		Glyph one = new Glyph("Stereo", "One",
				createList(
						new TextLine("Here is line 1"),
						new TextLine("Here is line 2"), 
						new TextLine("Here is line 3")),
				createList(new Symbol("Some text", 'q', SymbolShape.DIAMOND)));
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagram(d);
	}

	@Test
	public void test_5_6_GlyphWithSymbolOnly() throws Exception {
		Glyph one = new Glyph("", "One", null, createList(new Symbol("Some text", 'a', SymbolShape.CIRCLE),
				new Symbol("Some text", 'a', SymbolShape.DIAMOND), new Symbol("Some text", 'a', SymbolShape.HEXAGON)));
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_5_7_GlyphWithMultilineTextSymbol() throws Exception {
		Glyph one = new Glyph("Stereo\ntwoer", "One", createList(
					new TextLine("Here is line 1"),
					new TextLine("Here is line 2\nand it goes onto multiple\nlines"), 
					new TextLine("Here is line 3")), createList(new Symbol("Some text", 'q', SymbolShape.DIAMOND)));
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_5_8_TemplatedGlyphFinal() throws Exception {
		Glyph one = new Glyph("", "Rob's Glyph", createList(
				new TextLine("Proves XPath selector works")), null);
		one.setAttribute("class", "hibaby");
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagram(d);
	}
	
}
