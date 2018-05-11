package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.dom.elements.DiagramKite9XMLElement;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.adl.TextLine;

public class Test1ArrowGlyphContainer extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_1_1_Glyph() throws Exception {
		Kite9XMLElement one = new Glyph("Stereo\nMike", "Rob's Glyph df  sfsdfs  weferw asfsdf sdfsd\nLine 2", null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one));
		renderDiagram(d);
	}

	@Test
	public void test_1_2_GlyphInContainerFinal() throws Exception {
		Kite9XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Kite9XMLElement con = new Context("Context", createList(one), true, null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}

	@Test
	public void test_1_3_TwoGlyphsFinal() throws Exception {
		Kite9XMLElement one = new Glyph("Stereo", "One", null, null);
		Kite9XMLElement two = new Glyph(null, "Two", null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_4_GlyphSymbolsAndText() throws Exception {
		Kite9XMLElement one = new Glyph("Stereoy", "One", 
			listOf(new TextLine("Line 1\nThis has more than one row"), new TextLine("Second Line")), 
			listOf(new Symbol("Sym1", 'a', SymbolShape.SQUARE),
					new Symbol("Sym1", 'a', SymbolShape.CIRCLE)
					,
					new Symbol("Sym2", 'f', SymbolShape.DIAMOND),
				   new Symbol("Sym3", 'k', SymbolShape.HEXAGON)
	));
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_5_TwoArrowsFinal() throws Exception {
		Kite9XMLElement one = new Arrow("One\nMultiline");
		Kite9XMLElement two = new Arrow("Two");
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two));
		renderDiagram(d);
	}

	@Test
	public void test_1_6_TwoGlyphsInContainerFinal() throws Exception {
		Kite9XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Kite9XMLElement two = new Glyph("Stereo", "Two", listOf(new TextLine("Line 1"), new TextLine("Second Line")), null);
		Kite9XMLElement con = new Context("Context", createList(one, two), true, null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_7_TwoArrowsInContainerFinal() throws Exception {
		Kite9XMLElement one = new Arrow("One");
		Kite9XMLElement two = new Arrow("Two");
		Kite9XMLElement con = new Context("Context", createList(one, two), true, null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_8_EmptyGlyph() throws Exception {
		Kite9XMLElement one = new Glyph(null, null, null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one));

		renderDiagram(d);
	}
}
