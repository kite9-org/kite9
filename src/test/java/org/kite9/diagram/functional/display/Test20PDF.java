package org.kite9.diagram.functional.display;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.framework.xml.DiagramKite9XMLElement;
import org.kite9.framework.xml.LinkEndStyle;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;

public class Test20PDF extends AbstractDisplayFunctionalTest {

	@Test
	public void test_20_1_GlyphFinal() throws IOException {
		Kite9XMLElement one = new Glyph("RG", "Stereo", "Rob's Glyph", null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);

		renderDiagramPDF(d);
	}

	@Test
	public void test_20_2_GlyphWithTextSymbol() throws IOException {
		Kite9XMLElement one = new Glyph("one", "Stereo", "One", createList(new TextLine("Here is line 1", createList(new Symbol(
				"Some text", 'a', SymbolShape.CIRCLE), new Symbol("Some text", 'A', SymbolShape.DIAMOND), new Symbol(
				"Some text", 'A', SymbolShape.HEXAGON))), new TextLine("Here is line 2"),
				new TextLine("Here is line 3")), createList(new Symbol("Some text", 'q', SymbolShape.DIAMOND)));
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagramPDF(d);
	}

	@Test
	public void test_20_3_GlyphWithSymbolOnly() throws IOException {
		Kite9XMLElement one = new Glyph("one", "", "One", null, createList(new Symbol("Some text", 'a', SymbolShape.CIRCLE),
				new Symbol("Some text", 'a', SymbolShape.DIAMOND), new Symbol("Some text", 'a', SymbolShape.HEXAGON)));
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagramPDF(d);
	}

	@Test
	public void test_20_4_KeyWith1Symbol() throws IOException {

		Glyph a = new Glyph("a", "", "a", null, null);

		Key k = new Key("some bold text", null, createList(new Symbol("Some unholy information", 'S',
				SymbolShape.CIRCLE)));

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagramPDF(d);
	}

	@Test
	public void test_20_5_SingleContainerLink() throws IOException {

		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Arrow a = new Arrow("a1", "a1");

		Context con1 = new Context("con1", createList(g1), true, new TextLine("c1"), null);
		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
		Context con3 = new Context("con3", createList(a), true, new TextLine("c3"), null);
		Context con4 = new Context("con4", null, true, new TextLine("c4"), null);
		Context con5 = new Context("con5", null, true, new TextLine("c5"), null);
		new Link(con1, con2, null, new TextLine("arranges"), LinkEndStyle.ARROW, new TextLine("meets"));
		new Link(g1, a, null, new TextLine("g1end"), null, new TextLine("aend"), null);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(con1, con5, con4, con3, con2), null);
		renderDiagramPDF(d);
	}

	@Test
	public void test_20_6_ArrowOutsideContainer() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);

		Context con1 = new Context("b1", createList(one), true, null, null);

		Arrow a = new Arrow("links", "links");

		new Link(a, one);
		new Link(a, two);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con1, a, two), null);
		renderDiagramPDF(d);
	}

	@Test
	public void test_20_7_ArrowToMultipleElements() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);
		Glyph three = new Glyph("three", "", "three", null, null);

		Context con1 = new Context("b1", createList(one, two, three), true, null, null);

		Arrow a = new Arrow("links", "links");

		new TurnLink(a, one);
		new Link(a, two);
		new TurnLink(a, three);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con1, a), null);
		renderDiagramPDF(d);
	}
}
