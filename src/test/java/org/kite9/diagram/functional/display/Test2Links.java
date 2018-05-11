package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.ContradictingLink;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.dom.elements.DiagramKite9XMLElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;

public class Test2Links extends AbstractDisplayFunctionalTest {
	
	

	@Test
	public void test_2_2_2GlyphsArrowFinal() throws Exception {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Glyph two = new Glyph("two", "Stereo", "Two", null, null);
		Arrow a = new Arrow("meets", "meets");
		new Link(a, one);
		new Link(a, two);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, a));

		renderDiagram(d);
	}

	@Test
	public void test_2_3_2GlyphsHeadedArrow() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, a));

		renderDiagram(d);
	}

	@Test
	public void test_2_4_2Glyphs2Arrows() throws Exception {
		Glyph one = new Glyph("One", "Stereo", "One", null, null);
		Glyph two = new Glyph("Two", "Stereo", "Two", null, null);
		Arrow meets = new Arrow("meets", "meets");
		Arrow eats = new Arrow("eats", "eats");
		new Link(meets, one, null, null, null, null, Direction.LEFT);
		new TurnLink(meets, two);
		new ContradictingLink(eats, one, null, null, null, null, Direction.UP);
		new ContradictingLink(eats, two, null, null, null, null, Direction.DOWN);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, meets, eats), Layout.HORIZONTAL, null);

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
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, three, a, b));

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
		TurnLink tl1 = new TurnLink(a, one);
		TurnLink tl2 =new TurnLink(a, one);
		TurnLink tl3 =new TurnLink(a, one);
		TurnLink tl4 =new TurnLink(a, one);
		TurnLink tl5 =new TurnLink(a, one);
		TurnLink tl6 =new TurnLink(a, one);
		tl1.setID("tl1");
		tl2.setID("tl2");
		tl3.setID("tl3");
		tl4.setID("tl4");
		tl5.setID("tl5");
		tl6.setID("tl6");
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( one, a));

		renderDiagram(d);
	}
	
	/**
	 * Number of edges added doesn't look right
	 * @throws Exception
	 */
	@Test
	public void test_2_7_1Glyph1Arrow2() throws Exception {
		Glyph one = new Glyph("one", "Stereo", "One", null, null);
		Arrow a = new Arrow("meets", "meets");
		TurnLink tl1 = new TurnLink(a, one);
		TurnLink tl2 = new TurnLink(a, one);
		TurnLink tl3 = new TurnLink(a, one);
		tl1.setAttribute("id", "tl1");
		tl2.setAttribute("id", "tl2");
		tl3.setAttribute("id", "tl3");
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( one, a));

		renderDiagram(d);
	}

}
