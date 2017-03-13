package org.kite9.diagram.functional.layout;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.functional.AbstractLayoutFunctionalTest;
import org.kite9.diagram.xml.DiagramXMLElement;

public class Test3MultiArrows extends AbstractLayoutFunctionalTest {

	@Test
	public void test_3_1_3Glyphs1ArrowFinal() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Arrow a = new Arrow("meets");
		new Link( a, one);
		new Link(a, two);
		new Link( a, three);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList( one, two, three, a), null);

		renderDiagram(d);
	}

	@Test
	public void test_3_2_4Glyphs2Arrows() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Glyph four = new Glyph(null, "Four", null, null);

		Arrow a = new Arrow("meets");
		new Link(a, one);
		new Link(a, two);
		new Link(a, three);

		Arrow b = new Arrow("eats");
		new Link(b, two);
		new Link( b, three);
		new Link(b, four);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two, three, four, a, b), null);

		renderDiagram(d);
	}

	@Test
	public void test_3_3_4Glyphs3Arrows() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Glyph four = new Glyph(null, "Four", null, null);

		Arrow a = new Arrow("meets");
		new TurnLink(a, one);
		new TurnLink(a, two);
		new TurnLink(a, three);

		Arrow b = new Arrow("eats");
		new TurnLink(b, two);
		new TurnLink(b, three);
		new TurnLink(b, four);

		Arrow c = new Arrow("gets");
		new TurnLink(c, one);
		new TurnLink(c, three);
		new TurnLink(c, four);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two, three, four, a, b, c), null);

		renderDiagram(d);
	}
	
	@Test
	public void test_3_4_4Glyphs3PointArrows() throws Exception {
		Glyph one = new Glyph("g1", "Stereo", "One", null, null);
		Glyph two = new Glyph("g2", "Stereo", "Two", null, null);
		Glyph three = new Glyph("g3", null, "Three", null, null);
		Glyph four = new Glyph("g4", null, "Four", null, null);

		Arrow a = new Arrow("a1", "");
		new TurnLink(a, one);
		new TurnLink(a, two);
		new TurnLink(a, three);

		Arrow b = new Arrow("a2","");
		new TurnLink(b, two);
		new TurnLink(b, three);
		new TurnLink(b, four);

		Arrow c = new Arrow("a3","");
		new TurnLink(c, one);
		new TurnLink(c, three);
		new TurnLink(c, four);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two, three, four, a, b, c), null);

		renderDiagram(d);
	}
}
