package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.TestingEngine.ElementsMissingException;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.functional.TurnLink;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;

public class Test2Links extends AbstractDisplayFunctionalTest {
	
	

	@Test
	public void test_2_2_2GlyphsArrowFinal() throws Exception {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Glyph two = new Glyph("two", "Stereo", "Two", null, null);
		Arrow a = new Arrow("meets", "meets");
		new Link(a, one);
		new Link(a, two);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two, a));

		renderDiagram(d);
	}

	@Test
	public void test_2_3_2GlyphsHeadedArrow() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two, a));

		renderDiagram(d);
	}

	@Test
	public void test_2_4_2Glyphs2Arrows() throws Exception {
		Glyph one = new Glyph("One", "Stereo", "One", null, null);
		Glyph two = new Glyph("Two", "Stereo", "Two", null, null);
		Arrow a = new Arrow("meets", "meets");
		Arrow b = new Arrow("eats", "eats");
		new TurnLink(a, one);
		new TurnLink(a, two);
		new TurnLink(b, one);
		new TurnLink(b, two);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two, a, b), Layout.HORIZONTAL, null);

		renderDiagram(d);
	}

	@Test
	public void test_2_5_3Glyphs2Arrows() throws Exception {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Glyph two = new Glyph("two", "Stereo", "Two", null, null);
		Glyph three = new Glyph("three",null, "Three", null, null);
		Arrow a = new Arrow("meets", "meets");
		Arrow b = new Arrow("eats", "eats");
		new Link(a, one);
		new Link(a, two);
		new Link(b, three);
		new Link(b, two);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two, three, a, b));

		renderDiagram(d);
	}
	
	/**
	 * Number of edges added doesn't look right
	 * @throws Exception
	 */
	@Test
	public void test_2_6_1Glyph1Arrow() throws Exception {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Arrow a = new Arrow("meets", "meets");
		new TurnLink(a, one);
		new TurnLink(a, one);
		new TurnLink(a, one);
		new TurnLink(a, one);
		new TurnLink(a, one);
		new TurnLink(a, one);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList( one, a));

		renderDiagram(d);
	}

}
