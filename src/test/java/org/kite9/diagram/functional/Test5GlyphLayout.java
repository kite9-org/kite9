package org.kite9.diagram.functional;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.primitives.StylesheetReference;

public class Test5GlyphLayout extends AbstractFunctionalTest {

	@Override
	protected boolean checkDiagramSize() {
		return true;
	}

	@Test
	public void test_5_1_SimpleGlyphFinal() throws IOException {
		Contained one = new Glyph("", "Rob's Glyph", null, null);
		Diagram d = new Diagram("The Diagram", createList(one), null);
		renderDiagram(d);
	}

	@Test
	public void test_5_2_TypedGlyphFinal() throws IOException {
		Contained one = new Glyph("Type", "Rob's Glyph", null, null);
		Diagram d = new Diagram("The Diagram", createList(one), null);

		renderDiagram(d);
	}

	@Test
	public void test_5_3_GlyphWithTextFinal() throws IOException {
		Contained one = new Glyph("Stereo", "One", createList(new TextLine("Here is line 1"), new TextLine(
				"Here is line 2")), null);
		Diagram d = new Diagram("The Diagram", createList(one), null);
		renderDiagram(d);
	}

	@Test
	public void test_5_4_GlyphWithSymbol() throws IOException {
		Symbol s = new Symbol("Some text", 'a', SymbolShape.CIRCLE);
		Contained one = new Glyph("Stereo", "One", null, createList(s, s,
				new Symbol("Some text", 'B', SymbolShape.DIAMOND), new Symbol("Some text", 'm', SymbolShape.HEXAGON)));
		Diagram d = new Diagram("The Diagram", createList(one), null);
		renderDiagram(d);
	}

	@Test
	public void test_5_5_GlyphWithTextSymbol() throws IOException {
		Contained one = new Glyph("Stereo", "One", createList(
					new TextLine("Here is line 1", createList(new Symbol(
				"Some text", 'a', SymbolShape.CIRCLE), new Symbol("Some text", 'A', SymbolShape.DIAMOND), new Symbol(
				"Some text", 'A', SymbolShape.HEXAGON))), 
					new TextLine("Here is line 2"),
					new TextLine("Here is line 3")), createList(new Symbol("Some text", 'q', SymbolShape.DIAMOND)));
		Diagram d = new Diagram("The Diagram", createList(one), null);
		renderDiagram(d);
	}

	@Test
	public void test_5_6_GlyphWithSymbolOnly() throws IOException {
		Contained one = new Glyph("", "One", null, createList(new Symbol("Some text", 'a', SymbolShape.CIRCLE),
				new Symbol("Some text", 'a', SymbolShape.DIAMOND), new Symbol("Some text", 'a', SymbolShape.HEXAGON)));
		Diagram d = new Diagram("The Diagram", createList(one), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_5_7_GlyphWithMultilineTextSymbol() throws IOException {
		Contained one = new Glyph("Stereo", "One", createList(
					new TextLine("Here is line 1"),
					new TextLine("Here is line 2\nand it goes onto multiple\nlines", createList(new Symbol(
				"Some text", 'a', SymbolShape.CIRCLE), new Symbol("Some text", 'A', SymbolShape.DIAMOND), new Symbol(
				"Some text", 'A', SymbolShape.HEXAGON))), 
					new TextLine("Here is line 3")), createList(new Symbol("Some text", 'q', SymbolShape.DIAMOND)));
		Diagram d = new Diagram("The Diagram", createList(one), null);
		renderDiagram(d);
	}
}
