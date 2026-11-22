package org.kite9.diagram.functional.display;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TurnLink;
import org.w3c.dom.Element;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;

public class Test21InvisibleContexts extends AbstractDisplayFunctionalTest {

	int id = 0;

	List<Element> createGlyphs(int count) {
		List<Element> out = new ArrayList<Element>(count);
		for (int i = 0; i < count; i++) {
			id++;
			Glyph g = new Glyph("id" + id, "bob", "id" + id, null, null);
			out.add(g);
		}
		return out;
	}

	@Test
	public void test_21_1_3x3NoLinks() throws Exception {
		List<Element> row1 = createGlyphs(3);
		List<Element> row2 = createGlyphs(3);
		List<Element> row3 = createGlyphs(3);

		Context c1 = new Context("ctx1", row1, false, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, false, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, false, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((Element) c1, c2, c3), true, null, Layout.DOWN);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", createList((Element) cc), null);
		renderDiagram(d);

	}

	@Test
	public void test_21_2_Nx3NoLinks() throws Exception {
		List<Element> row1 = createGlyphs(8);
		List<Element> row2 = createGlyphs(7);
		List<Element> row3 = createGlyphs(5);

		Context c1 = new Context("ctx1", row1, false, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, false, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, false, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((Element) c1, c2, c3), true, null, Layout.DOWN);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", createList((Element) cc), null);
		renderDiagram(d);

	}

	@Test
	public void test_21_3_Nx3SomeLinksWithDirectedContext() throws Exception {
		List<Element> row1 = createGlyphs(8);
		List<Element> row2 = createGlyphs(7);
		List<Element> row3 = createGlyphs(15);

		Context c1 = new Context("ctx1", row1, true, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, true, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, true, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((Element) c1, c2, c3), true, null, Layout.DOWN);

		// new Link(c1, c2, null, null, null, null, Direction.DOWN);
		// new Link(c2, c3, null, null, null, null, Direction.DOWN);

		new Link((Element) c1.getChildNodes().item(2), (Element) c2.getChildNodes().item(3), null, null, null, null,
				null);
		// new Link((Connected) c1.getContents().get(4), (Connected)
		// c2.getContents().get(3), null, null, null);
		new Link((Element) c1.getChildNodes().item(4), (Element) c3.getChildNodes().item(4), null, null, null, null,
				null);
		//
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", createList((Element) cc), Layout.DOWN, null);
		renderDiagram(d);

	}

	@Test
	public void test_21_4_Nx3SomeLinksWithLinkedContexts() throws Exception {
		List<Element> row1 = createGlyphs(8);
		List<Element> row2 = createGlyphs(7);
		List<Element> row3 = createGlyphs(15);

		Context c1 = new Context("ctx1", row1, true, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, true, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, true, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((Element) c1, c2, c3), true, null, null);

		new Link(c1, c2, null, null, null, null, Direction.DOWN);
		new Link(c2, c3, null, null, null, null, Direction.DOWN);

		new Link((Element) c1.getChildNodes().item(2), (Element) c2.getChildNodes().item(3), null, null, null, null,
				null);
		// new Link((Connected) c1.getContents().get(4), (Connected)
		// c2.getContents().get(3), null, null, null);
		new Link((Element) c1.getChildNodes().item(4), (Element) c3.getChildNodes().item(4), null, null, null, null,
				null);
		//
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", createList((Element) cc), Layout.DOWN, null);
		renderDiagram(d);

	}

	@Test
	public void test_21_5_hiddenContext() throws Exception {
		Glyph rs = new Glyph("ridley_scott", "Director", "Ridley Scott", null, null);
		LinkBody directed = new LinkBody("directed");

		new Link(directed, rs);

		Glyph bladerunner = new Glyph("film", "Bladerunner", null, null);
		Glyph glad = new Glyph("film", "Gladiator", null, null);
		Glyph thelma = new Glyph("film", "Thelma & Louise", null, null);

		new TurnLink(directed, bladerunner, null, null, LinkEndStyle.ARROW, null);
		new TurnLink(directed, glad, null, null, LinkEndStyle.ARROW, null);
		new TurnLink(directed, thelma, null, null, LinkEndStyle.ARROW, null);

		Context hidden = new Context("ch", listOf(bladerunner, glad, thelma), false, null, Layout.RIGHT);

		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement("my_diagram", listOf(rs, directed, hidden), null);
		renderDiagram(d1);

	}
}
