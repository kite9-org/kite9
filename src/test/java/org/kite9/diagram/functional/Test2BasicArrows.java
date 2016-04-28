package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.functional.TestingEngine.ElementsMissingException;
import org.kite9.diagram.primitives.Contained;

public class Test2BasicArrows extends AbstractFunctionalTest {
	@Test(expected=ElementsMissingException.class)
	public void test_2_1_1GlyphsOneEdge() throws IOException {
		Glyph one = new Glyph("Stereo", "One", null, null);
		new TurnLink(one, one);
		Diagram d = new Diagram("The Diagram", createList((Contained) one), null);

		renderDiagram(d);
	}

	@Test
	public void test_2_2_2GlyphsArrowFinal() throws IOException {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Glyph two = new Glyph("two", "Stereo", "Two", null, null);
		Arrow a = new Arrow("meets", "meets");
		new Link(a, one);
		new Link(a, two);
		Diagram d = new Diagram("The Diagram", createList((Contained) one, two, a), null);

		renderDiagram(d);
	}

	@Test
	public void test_2_3_2GlyphsHeadedArrow() throws IOException {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, null);
		Diagram d = new Diagram("The Diagram", createList((Contained) one, two, a), null);

		renderDiagram(d);
	}

	@Test
	public void test_2_4_2Glyphs2Arrows() throws IOException {
		Glyph one = new Glyph("One", "Stereo", "One", null, null);
		Glyph two = new Glyph("Two", "Stereo", "Two", null, null);
		Arrow a = new Arrow("meets", "meets");
		Arrow b = new Arrow("eats", "eats");
		new TurnLink(a, one);
		new TurnLink(a, two);
		new TurnLink(b, one);
		new TurnLink(b, two);
		Diagram d = new Diagram("The Diagram", createList((Contained) one, two, a, b), null);

		renderDiagram(d);
	}

	@Test
	public void test_2_5_3Glyphs2Arrows() throws IOException {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Glyph two = new Glyph("two", "Stereo", "Two", null, null);
		Glyph three = new Glyph("three",null, "Three", null, null);
		Arrow a = new Arrow("meets", "meets");
		Arrow b = new Arrow("eats", "eats");
		new Link(a, one);
		new Link(a, two);
		new Link(b, three);
		new Link(b, two);
		Diagram d = new Diagram("The Diagram", createList((Contained) one, two, three, a, b), null);

		renderDiagram(d);
	}
	
	/**
	 * Number of edges added doesn't look right
	 * @throws IOException
	 */
	@Test
	public void test_2_6_1Glyph1Arrow() throws IOException {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Arrow a = new Arrow("meets", "meets");
		new TurnLink(a, one);
		new TurnLink(a, one);
		new TurnLink(a, one);
		new TurnLink(a, one);
		new TurnLink(a, one);
		new TurnLink(a, one);
		Diagram d = new Diagram("The Diagram", createList((Contained) one, a), null);

		renderDiagram(d);
	}

}
