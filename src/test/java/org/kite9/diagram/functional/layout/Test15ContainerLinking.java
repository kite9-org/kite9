package org.kite9.diagram.functional.layout;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.functional.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;

public class Test15ContainerLinking extends AbstractLayoutFunctionalTest {

	@Test
	public void test_15_1_LabelledContainerLinks() throws Exception {

		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Arrow a = new Arrow("a1");

		Context con1 = new Context("con1", createList(g1), true, new TextLine("c1"), null);
		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
		Context con3 = new Context("con3", createList(a), true, new TextLine("c3"), null);
		Context con4 = new Context("con4", null, true, new TextLine("c4"), null);
		Context con5 = new Context("con5", null, true, new TextLine("c\n5", createList(new Symbol("BOB", 'c', SymbolShape.CIRCLE))), null);
		new Link(con1, con2, null, new TextLine("arranges"), LinkEndStyle.ARROW, new TextLine("meets"));
		new Link(g1, a, null, new TextLine("g1end"), null, new TextLine("aend"), null);

		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, con5, con4, con3, con2), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_2_LinkedToThree() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);

		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);

		new Link(con1, g1, null, new TextLine("arranges 1"), LinkEndStyle.ARROW, new TextLine("meets 1 "));
		new Link(con1, g2, LinkEndStyle.ARROW, new TextLine("arranges 2"), LinkEndStyle.ARROW, new TextLine(
				"meets 2"));
		new Link(con1, g3, null, new TextLine("arranges 3"), LinkEndStyle.ARROW, new TextLine("meets 3") ) ;

		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, g1, g2, g3), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_3_LinkedLeft() throws Exception {
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

		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, g1, g2, g3), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_4_LinkedRight() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);

		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);

		new Link(con1, g1, LinkEndStyle.ARROW, new TextLine("arranges 1"), LinkEndStyle.ARROW,
				new TextLine("meets 1"), Direction.LEFT);
		new Link(con1, g2, null, new TextLine("arranges 2"), null, new TextLine("meets 2"), Direction.LEFT);
		new Link(con1, g3, null, new TextLine("arranges 3"), LinkEndStyle.ARROW, new TextLine(
				"meets 3"), Direction.LEFT);

		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, g1, g2, g3), null);
		renderDiagram(d);
	}

	@Test
	public void test_15_5_CrossingLink() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);

		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);

		Context con2 = new Context("con2", createList(con1), true, new TextLine("c2"), null);

		new Link(con1, g1, LinkEndStyle.ARROW, new TextLine("arranges"), LinkEndStyle.ARROW, new TextLine(
				"meets"));

		DiagramXMLElement d = new DiagramXMLElement("D", createList(con2, g1), null);
		renderDiagram(d);
	}
	
//	@Test
//	public void test_15_6_DifficultLink() throws Exception {
//		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);
//		//Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
//		Context con3 = new Context("con3", null, true, new TextLine("c3"), null);
//		//Context con4 = new Context("con4", null, true, new TextLine("c4"), null);
//		new Link(con1, con3);
//		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, con3), Layout.RIGHT, null);
//		renderDiagram(d);
//	}
//	
//	@Test
//	public void test_15_7_MultipleLinks() throws Exception {
//		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);
//		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
//		new TurnLink(con1, con2);
//		new TurnLink(con1, con2);
//		new TurnLink(con1, con2);
//		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, con2), Layout.RIGHT, null);
//		renderDiagram(d);
//	}
//	
//	@Test
//	public void test_15_8_LinkedToThreeUnlabelled() throws Exception {
//		Glyph g1 = new Glyph("g1", "", "g1", null, null);
//		Glyph g2 = new Glyph("g2", "", "g2", null, null);
//		Glyph g3 = new Glyph("g3", "", "g3", null, null);
//
//		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);
//
//		new Link(con1, g1, null, null, LinkEndStyle.ARROW, null);
//		new Link(con1, g2, LinkEndStyle.ARROW, null, LinkEndStyle.ARROW, null);
//		new Link(con1, g3, null, null, LinkEndStyle.ARROW, null ) ;
//
//		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, g1, g2, g3), null);
//		renderDiagram(d);
//	}
//
//	@Test
//	public void test_15_9_MultipleLinksNoLayout() throws Exception {
//		Context con1 = new Context("con1", null, true, new TextLine("c1"), null);
//		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
//		new Link(con1, con2, null, null, null, null, Direction.RIGHT);
//		new Link(con1, con2, null, null, null, null, Direction.RIGHT);
//		new Link(con1, con2, null, null, null, null, Direction.RIGHT);
//		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, con2), null);
//		renderDiagram(d);
//	}
//	
//	@Test
//	public void test_15_10_LabelledContainerLinksSimple() throws Exception {
//
//		Glyph g1 = new Glyph("g1", "", "g1", null, null);
//		Arrow a = new Arrow("a1");
//
//		Context con1 = new Context("con1", createList(g1), true, new TextLine("c1"), null);
//		Context con2 = new Context("con2", null, true, new TextLine("c2"), null);
//		Context con3 = new Context("con3", createList(a), true, new TextLine("c3"), null);
//		new Link(con1, con2, null, new TextLine("arranges"), LinkEndStyle.ARROW, new TextLine("meets"));
//		new Link(g1, a, null, new TextLine("g1end"), null, new TextLine("aend"), null);
//
//		DiagramXMLElement d = new DiagramXMLElement("D", createList(con1, con3, con2), null);
//		renderDiagram(d);
//	}
//
}
