package org.kite9.diagram.functional.layout;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.model.position.Direction;

/**
 * Various tests for directed arrows within a single face.
 * 
 * @author robmoffat
 *
 */
public class Test7DirectedArrows extends AbstractLayoutFunctionalTest {

	@Test
	public void test_7_1_OneDirectedArrow() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(a, two);
		new Link(a, three);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(one, two, three, a),
				null);

		renderDiagram(d);
	}

	@Test
	public void test_7_2_SeparateDirectedArrows() throws Exception {
		Glyph one = new Glyph("", "One", null, null);
		Glyph two = new Glyph("", "Two", null, null);

		Arrow meets = new Arrow("meets");
		Arrow sees = new Arrow("sees");

		new Link(meets, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(sees, two, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(one, two, meets,
				sees), null);

		renderDiagram(d);
	}

	@Test
	public void test_7_3_SeparateDirectedArrows2() throws Exception {
		Glyph one = new Glyph("", "One", null, null);
		Glyph two = new Glyph("", "Two", null, null);
		Glyph three = new Glyph("", "Three", null, null);
		Glyph four = new Glyph("", "Four", null, null);

		Arrow meets = new Arrow("meets");
		Arrow sees = new Arrow("sees");
		Arrow asks = new Arrow("asks");
		Arrow looks = new Arrow("looks");

		new Link(meets, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(sees, two, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(asks, three, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(looks, four, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(one, two, meets,
				sees, asks, looks, three, four), null);

		renderDiagram(d);
	}

	@Test
	public void test_7_4_TwoJoinedDirectedArrows() throws Exception {
		Glyph one = new Glyph("One", "", "One", null, null);
		Glyph two = new Glyph("Two", "", "Two", null, null);

		Arrow meets = new Arrow("meets", "meets");
		Arrow sees = new Arrow("sees", "sees");

		new Link(meets, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(sees, two, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new TurnLink(sees, one);
		new TurnLink(meets, two);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(one, two, meets,
				sees), null);

		renderDiagram(d);
	}

	@Test
	public void test_7_5_CompletelyConstrainedArrow() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(a, three, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(one, two, three, a),
				null);

		renderDiagram(d);
	}

	@Test
	public void test_7_6_CompletelyConstrainedArrow2() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(a, three, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(one, two, three, a),
				null);

		renderDiagram(d);
	}

	@Test
	public void test_7_7_ConstrainedBox1() throws Exception {
		Glyph tl = new Glyph("TL", "", "TL", null, null);
		Glyph br = new Glyph("BR", "", "BR", null, null);
		Glyph tr = new Glyph("TR", "", "TR", null, null);
		Glyph bl = new Glyph("BL", "", "BL", null, null);

		Arrow top = new Arrow("top", "top");
		Arrow left = new Arrow("left", "left");
		Arrow right = new Arrow("right", "right");
		Arrow bottom = new Arrow("bottom", "bottom");

		new Link(top, tl, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(top, tr, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(bottom, bl, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(bottom, br, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);

		new Link(left, tl, null, null, LinkEndStyle.ARROW, null,Direction.UP);
		new Link(left, bl, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(right, tr, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(right, br);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(tl, tr, bl, br, top,
				left, bottom, right), null);

		renderDiagram(d);
	}

	@Test
	public void test_7_8_ConstrainedBox2() throws Exception {
		Glyph tl = new Glyph("TL", "", "TL", null, null);
		Glyph br = new Glyph("BR", "", "BR", null, null);
		Glyph tr = new Glyph("TR", "", "TR", null, null);
		Glyph bl = new Glyph("BL", "", "BL", null, null);

		Arrow top = new Arrow("top","top\ntop\ntop");
		Arrow left = new Arrow("left","left lefty left left");
		Arrow right = new Arrow("right","right");
		Arrow bottom = new Arrow("bottom","bottom");

		new Link(top, tl, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(top, tr, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(bottom, bl, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(bottom, br, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);

		new Link(left, tl, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(left, bl, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(right, tr, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(right, br, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(tl, tr, bl, br, top,
				left, bottom, right), null);

		renderDiagram(d);
	}

}
