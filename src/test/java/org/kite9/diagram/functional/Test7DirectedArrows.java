package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.Arrow;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.diagram.xml.Link;
import org.kite9.diagram.xml.LinkEndStyle;

/**
 * Various tests for directed arrows within a single face.
 * 
 * @author robmoffat
 *
 */
public class Test7DirectedArrows extends AbstractFunctionalTest {

	@Test
	public void test_7_1_OneDirectedArrow() throws IOException {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(a, two);
		new Link(a, three);
		Diagram d = new Diagram("D", createList((Contained) one, two, three, a),
				null);

		renderDiagram(d);
	}

	@Test
	public void test_7_2_SeparateDirectedArrows() throws IOException {
		Glyph one = new Glyph("", "One", null, null);
		Glyph two = new Glyph("", "Two", null, null);

		Arrow meets = new Arrow("meets");
		Arrow sees = new Arrow("sees");

		new Link(meets, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(sees, two, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		Diagram d = new Diagram("D", createList((Contained) one, two, meets,
				sees), null);

		renderDiagram(d);
	}

	@Test
	public void test_7_3_SeparateDirectedArrows2() throws IOException {
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
		Diagram d = new Diagram("D", createList((Contained) one, two, meets,
				sees, asks, looks, three, four), null);

		renderDiagram(d);
	}

	@Test
	public void test_7_4_TwoJoinedDirectedArrows() throws IOException {
		Glyph one = new Glyph("One", "", "One", null, null);
		Glyph two = new Glyph("Two", "", "Two", null, null);

		Arrow meets = new Arrow("meets", "meets");
		Arrow sees = new Arrow("sees", "sees");

		new Link(meets, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(sees, two, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new TurnLink(sees, one);
		new TurnLink(meets, two);

		Diagram d = new Diagram("D", createList((Contained) one, two, meets,
				sees), null);

		renderDiagram(d);
	}

	@Test
	public void test_7_5_CompletelyConstrainedArrow() throws IOException {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(a, three, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		Diagram d = new Diagram("D", createList((Contained) one, two, three, a),
				null);

		renderDiagram(d);
	}

	@Test
	public void test_7_6_CompletelyConstrainedArrow2() throws IOException {
		Glyph one = new Glyph("Stereo", "One", null, null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph(null, "Three", null, null);
		Arrow a = new Arrow("meets");
		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(a, two, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(a, three, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		Diagram d = new Diagram("D", createList((Contained) one, two, three, a),
				null);

		renderDiagram(d);
	}

	@Test
	public void test_7_7_ConstrainedBox1() throws IOException {
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

		Diagram d = new Diagram("D", createList((Contained) tl, tr, bl, br, top,
				left, bottom, right), null);

		renderDiagram(d);
	}

	@Test
	public void test_7_8_ConstrainedBox2() throws IOException {
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

		Diagram d = new Diagram("D", createList((Contained) tl, tr, bl, br, top,
				left, bottom, right), null);

		renderDiagram(d);
	}

}
