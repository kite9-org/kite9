package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.Contained;

public class Test6ContainersAndArrows extends AbstractFunctionalTest {

	@Test
	public void test_6_1_ArrowOutsideContainer() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);

		Contained con1 = new Context("b1", createList((Contained) one), true, null, null);

		Arrow a = new Arrow("links", "links");

		new Link(a, one, null, null, null, null, Direction.UP);
		new Link(a, two);

		Diagram d = new Diagram("The Diagram", createList(con1, a, two), null);
		renderDiagram(d);
	}

	@Test
	public void test_6_2_ArrowInsideContainerFinal() throws IOException {
		Glyph one = new Glyph("", "one", null, null);
		Glyph two = new Glyph("", "two", null, null);
		Arrow a = new Arrow("links");

		Contained con1 = new Context("b1", createList((Contained) one, a), true, null, null);

		new Link(a, one);
		new Link(a, two);

		Diagram d = new Diagram("The Diagram", createList(con1, two), null);
		renderDiagram(d);
	}

	@Test
	public void test_6_3_ArrowToMultipleElements() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);
		Glyph three = new Glyph("three", "", "three", null, null);

		Contained con1 = new Context("b1", createList((Contained) one, two, three), true, null, null);

		Arrow a = new Arrow("links", "links");

		new TurnLink(a, one);
		new TurnLink(a, two);
		new TurnLink(a, three);

		Diagram d = new Diagram("The Diagram", createList(con1, a), null);
		renderDiagram(d);
	}
}
