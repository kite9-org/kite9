package org.kite9.diagram.functional.layout;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;

@Tag("minimal")
public class Test3MultiArrows extends AbstractLayoutFunctionalTest {

	@Test
	public void test_3_1_3Glyphs1ArrowFinal() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		LinkBody a = new LinkBody("meets");
		new Link(a, one);
		new TurnLink(a, two);
		new TurnLink(a, three);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, three, a), null);

		renderDiagram(d);
	}

	@Test
	public void test_3_2_4Glyphs2Arrows() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Glyph four = new Glyph(null, "Four", null, null);

		LinkBody a = new LinkBody("meets");
		new Link(a, one);
		new TurnLink(a, two);
		new TurnLink(a, three);

		LinkBody b = new LinkBody("eats");
		new TurnLink(b, two);
		new TurnLink(b, three);
		new Link(b, four);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, three, four, a, b),
				null);

		renderDiagram(d);
	}

	@Test
	public void test_3_3_4Glyphs3Arrows() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Glyph four = new Glyph(null, "Four", null, null);

		LinkBody a = new LinkBody("meets");
		new TurnLink(a, one);
		new TurnLink(a, two);
		new TurnLink(a, three);

		LinkBody b = new LinkBody("eats");
		new TurnLink(b, two);
		new TurnLink(b, three);
		new TurnLink(b, four);

		LinkBody c = new LinkBody("gets");
		new TurnLink(c, one);
		new TurnLink(c, three);
		new TurnLink(c, four);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, three, four, a, b, c),
				null);

		renderDiagram(d);
	}

	@Test
	public void test_3_4_4Glyphs3PointArrows() throws Exception {
		Glyph one = new Glyph("g1", "Stereo", "One", null, null);
		Glyph two = new Glyph("g2", "Stereo", "Two", null, null);
		Glyph three = new Glyph("g3", null, "Three", null, null);
		Glyph four = new Glyph("g4", null, "Four", null, null);

		LinkBody a = new LinkBody("a1", "a1");
		new TurnLink(a, one);
		new TurnLink(a, two);
		new TurnLink(a, three);

		LinkBody b = new LinkBody("a2", "a2");
		new TurnLink(b, two);
		new TurnLink(b, three);
		new TurnLink(b, four);

		LinkBody c = new LinkBody("a3", "a3");
		new TurnLink(c, one);
		new TurnLink(c, three);
		new TurnLink(c, four);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, three, four, a, b, c),
				null);

		renderDiagram(d);
	}
}
