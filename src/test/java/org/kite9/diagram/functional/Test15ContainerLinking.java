package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.Arrow;
import org.kite9.diagram.xml.Context;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.diagram.xml.Link;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.diagram.xml.Symbol;
import org.kite9.diagram.xml.TextLine;
import org.kite9.diagram.xml.Symbol.SymbolShape;

public class Test15ContainerLinking extends AbstractFunctionalTest {

	@Test
	public void test_15_1_LabelledContainerLinks() throws IOException {

		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Arrow a = new Arrow("a1");

		Context con1 = new Context("con1", createList((Contained) g1), true, new TextLine("c1"), null);
		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
		Context con3 = new Context("con3", createList((Contained) a), true, new TextLine("c3"), null);
		Context con4 = new Context("con4", null, true, new TextLine("c4"), null);
		Context con5 = new Context("con5", null, true, new TextLine("c\n5", createList(new Symbol("BOB", 'c', SymbolShape.CIRCLE))), null);
		new Link(con1, con2, null, new TextLine("arranges"), LinkEndStyle.ARROW, new TextLine("meets"));
		new Link(g1, a, null, new TextLine("g1end"), null, new TextLine("aend"), null);

		Diagram d = new Diagram("D", createList((Contained) con1, con5, con4, con3, con2), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_2_LinkedToThree() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);

		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);

		new Link(con1, g1, null, new TextLine("arranges 1"), LinkEndStyle.ARROW, new TextLine("meets 1 "));
		new Link(con1, g2, LinkEndStyle.ARROW, new TextLine("arranges 2"), LinkEndStyle.ARROW, new TextLine(
				"meets 2"));
		new Link(con1, g3, null, new TextLine("arranges 3"), LinkEndStyle.ARROW, new TextLine("meets 3") ) ;

		Diagram d = new Diagram("D", createList((Contained) con1, g1, g2, g3), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_3_LinkedLeft() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);

		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);

		new Link(con1, g1, null, new TextLine("arranges 1"), LinkEndStyle.ARROW, new TextLine(
				"meets 1"), Direction.RIGHT);
		new Link(con1, g2, LinkEndStyle.ARROW, new TextLine("arranges 2"), null, new TextLine(
				"meets 2"), Direction.RIGHT);
		new Link(con1, g3, null, new TextLine("arranges 3"), LinkEndStyle.ARROW, new TextLine(
				"meets 3"), Direction.RIGHT);

		Diagram d = new Diagram("D", createList((Contained) con1, g1, g2, g3), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_4_LinkedRight() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);

		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);

		new Link(con1, g1, LinkEndStyle.ARROW, new TextLine("arranges 1"), LinkEndStyle.ARROW,
				new TextLine("meets 1"), Direction.LEFT);
		new Link(con1, g2, null, new TextLine("arranges 2"), null, new TextLine("meets 2"), Direction.LEFT);
		new Link(con1, g3, null, new TextLine("arranges 3"), LinkEndStyle.ARROW, new TextLine(
				"meets 3"), Direction.LEFT);

		Diagram d = new Diagram("D", createList((Contained) con1, g1, g2, g3), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_5_CrossingLink() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);

		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);

		Context con2 = new Context("con2", createList((Contained) con1), true, new TextLine("c2"), null);

		new Link(con1, g1, LinkEndStyle.ARROW, new TextLine("arranges"), LinkEndStyle.ARROW, new TextLine(
				"meets"));

		Diagram d = new Diagram("D", createList((Contained) con2, g1), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_15_6_DifficultLink() throws IOException {
		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);
		//Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
		Context con3 = new Context("con3", null, true, new TextLine("c3"), null);
		//Context con4 = new Context("con4", null, true, new TextLine("c4"), null);
		new Link(con1, con3);
		Diagram d = new Diagram("D", createList((Contained) con1, con3), Layout.RIGHT, null);
		renderDiagram(d);
	}
	
	@Test
	public void test_15_7_MultipleLinks() throws IOException {
		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);
		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
		new TurnLink(con1, con2);
		new TurnLink(con1, con2);
		new TurnLink(con1, con2);
		Diagram d = new Diagram("D", createList((Contained) con1, con2), Layout.RIGHT, null);
		renderDiagram(d);
	}
	
	@Test
	public void test_15_8_LinkedToThreeUnlabelled() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);

		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);

		new Link(con1, g1, null, null, LinkEndStyle.ARROW, null);
		new Link(con1, g2, LinkEndStyle.ARROW, null, LinkEndStyle.ARROW, null);
		new Link(con1, g3, null, null, LinkEndStyle.ARROW, null ) ;

		Diagram d = new Diagram("D", createList((Contained) con1, g1, g2, g3), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_9_MultipleLinksNoLayout() throws IOException {
		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);
		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
		new Link(con1, con2, null, null, null, null, Direction.RIGHT);
		new Link(con1, con2, null, null, null, null, Direction.RIGHT);
		new Link(con1, con2, null, null, null, null, Direction.RIGHT);
		Diagram d = new Diagram("D", createList((Contained) con1, con2), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_15_10_LabelledContainerLinksSimple() throws IOException {

		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Arrow a = new Arrow("a1");

		Context con1 = new Context("con1", createList((Contained) g1), true, new TextLine("c1"), null);
		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
		Context con3 = new Context("con3", createList((Contained) a), true, new TextLine("c3"), null);
		new Link(con1, con2, null, new TextLine("arranges"), LinkEndStyle.ARROW, new TextLine("meets"));
		new Link(g1, a, null, new TextLine("g1end"), null, new TextLine("aend"), null);

		Diagram d = new Diagram("D", createList((Contained) con1, con3, con2), null);
		renderDiagram(d);
	}

}
