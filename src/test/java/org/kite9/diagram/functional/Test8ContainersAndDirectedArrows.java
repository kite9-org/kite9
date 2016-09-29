package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;

public class Test8ContainersAndDirectedArrows extends AbstractFunctionalTest {

	@Test
	public void test_8_1_DirectedArrowOutsideToInside() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);

		Context con1 = new Context("b1", createList(one), true, null, null);

		Arrow a = new Arrow("links", "links");

		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new Link(a, two);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con1, a, two), null);
		renderDiagram(d);
	}

	@Test
	public void test_8_2_DirectedArrowInsideContainer() throws IOException {
		Glyph one = new Glyph("", "one", null, null);
		Glyph two = new Glyph("", "two", null, null);
		Arrow a = new Arrow("links", "links");

		Context con1 = new Context("b1", createList(one, a), true, null, null);

		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(a, two);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con1, two), null);
		renderDiagram(d);
	}

	@Test
	public void test_8_3_ArrowToMultipleElements() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);
		Glyph three = new Glyph("three", "three", null, null);

		Context con1 = new Context("b1", createList(one, two, three), true, null, null);

		Arrow a = new Arrow("links", "links");

		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new TurnLink(a, two);
		new TurnLink(a, three);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con1, a), null);
		renderDiagram(d);
	}

	@Test
	public void test_8_4_ArrowToSingleContainer() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);
		Glyph three = new Glyph("three", "three", null, null);

		Context con1 = new Context("b1", createList(one, two, three), true, null, null);

		Arrow a = new Arrow("links", "links");

		new TurnLink(a, three);
		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new TurnLink(a, two);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con1, a), null);
		renderDiagram(d);
	}

	@Test
	public void test_8_5_ArrowToMultipleContainers() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);
		Glyph three = new Glyph("three", "", "three", null, null);

		Context con1 = new Context("b1", createList(one, two), true, null, null);
		Context con2 = new Context("b2", createList(con1, three), true, null, null);

		Arrow a = new Arrow("links", "links");

		new Link(a, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);
		new TurnLink(a, two);
		new TurnLink(a, three);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con2, a), null);
		renderDiagram(d);
	}
	
	@Test
	@NotAddressed
	public void test_8_6_ContainerWithLayoutConnection() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);

		Context con1 = new Context("b1", createList(one), true, null, null);
		Glyph two = new Glyph("two", "", "two", null, null);

		Link l = new Link(con1, two, null, null, null, null, Direction.DOWN);
		l.setShapeName("INVISIBLE");
		new Link(one, two);

		
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con1, two), null);
		renderDiagram(d);

	}

	@Test
	public void test_8_7_ArrowToMultipleContainers() throws IOException {
		Glyph one = new Glyph("one", "", "one", null, null);
		Glyph two = new Glyph("two", "", "two", null, null);
		Glyph three = new Glyph("three", "", "three", null, null);

		Context con1 = new Context("b1", createList(one, two), true, null, Layout.RIGHT);
		Context con2 = new Context("b2", createList(con1), true, null, null);
		Context con3 = new Context("b2", createList(con2), true, null, null);
		Context con4 = new Context("b2", createList(con3), true, null, null);

		new Link(three, one, null, null, LinkEndStyle.ARROW, null, Direction.DOWN);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(three, con4), null);
		renderDiagram(d);
	}
}
