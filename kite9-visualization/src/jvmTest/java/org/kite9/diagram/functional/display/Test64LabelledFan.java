package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.GraphConstructionTools;
import org.kite9.diagram.adl.*;
import org.kite9.diagram.common.HelpMethods;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class Test64LabelledFan extends AbstractDisplayFunctionalTest {


	@Test
	public void test_64_1_DownFanFixedMiddlesLabels() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<Element> out = new ArrayList<Element>();
		out.add(g0);
		out.add(g1);

		Glyph[] l = GraphConstructionTools.createX("fan", 8, out);
		new Link(g0, g1, null, null, null, null, Direction.DOWN);

		labelledLink(g1, l, 0);
		labelledLink(g1, l, 1);
		labelledLink(g1, l, 2);
		labelledLink(g1, l, 3);
		labelledLink(g1, l, 4);
		labelledLink(g1, l, 5);
		labelledLink(g1, l, 6);
		labelledLink(g1, l, 7);

		new Link(l[0], l[1], null, null, null, null, Direction.RIGHT);
		new Link(l[1], l[2], null, null, null, null, Direction.RIGHT);
		new Link(l[2], l[3], null, null, null, null, Direction.RIGHT);
		new Link(l[3], l[4], null, null, null, null, Direction.RIGHT);
		new Link(l[4], l[5], null, null, null, null, Direction.RIGHT);
		new Link(l[5], l[6], null, null, null, null, Direction.RIGHT);
		new Link(l[6], l[7], null, null, null, null, Direction.RIGHT);

		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(out, null);
		renderDiagram(d1);


	}

	@Test
	public void test_64_2_LargeGlyphLabelled() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		g1.setAttribute("style",CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY+": "+ DiagramElementSizing.MAXIMIZE.toString().toLowerCase());
		List<Element> out = new ArrayList<Element>();
		out.add(g0);
		out.add(g1);

		Glyph[] l = GraphConstructionTools.createX("fan", 8, out);
		new Link(g0, g1, null, null, null, null, Direction.DOWN);

		labelledLink(g1, l, 0);
		labelledLink(g1, l, 1);
		labelledLink(g1, l, 2);
		labelledLink(g1, l, 3);
		labelledLink(g1, l, 4);
		labelledLink(g1, l, 5);
		labelledLink(g1, l, 6);
		labelledLink(g1, l, 7);

		new Link(l[0], l[1], null, null, null, null, Direction.RIGHT);
		new Link(l[1], l[2], null, null, null, null, Direction.RIGHT);
		new Link(l[2], l[3], null, null, null, null, Direction.RIGHT);
		new Link(l[3], l[4], null, null, null, null, Direction.RIGHT);
		new Link(l[4], l[5], null, null, null, null, Direction.RIGHT);
		new Link(l[5], l[6], null, null, null, null, Direction.RIGHT);
		new Link(l[6], l[7], null, null, null, null, Direction.RIGHT);

		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(out, null);
		renderDiagram(d1);


	}

	@Test
	public void test_64_3_DoubleFanningLabelled() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);

		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g0, g1, null, null, null, null, Direction.DOWN);
		new Link(g0, g2, null, null, null, null);

		List<Element> out = new ArrayList<Element>();
		out.add(g0);
		out.add(g1);
		out.add(g2);

		Glyph[] l = GraphConstructionTools.createX("fan", 5, out);

		labelledLink(g1, l, 0);
		labelledLink(g1, l, 1);
		labelledLink(g1, l, 2);
		labelledLink(g1, l, 3);
		labelledLink(g2, l, 2);
		labelledLink(g2, l, 3);
		labelledLink(g2, l, 4);

		new Link(l[0], l[1], null, null, null, null, Direction.RIGHT);
		new Link(l[1], l[2], null, null, null, null, Direction.RIGHT);
		new Link(l[2], l[3], null, null, null, null, Direction.RIGHT);
		new Link(l[3], l[4], null, null, null, null, Direction.RIGHT);

		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(out, null);
		renderDiagram(d1);


	}

	@Test
	public void test_64_4_DifferentDirectionFanning() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);

		new Link(g0, g1, null, null, null, null, Direction.DOWN);
		new Link(g0, g2, null, null, null, null, Direction.RIGHT);

		List<Element> out = new ArrayList<Element>();
		out.add(g0);
		out.add(g1);
		out.add(g2);

		Glyph[] l = GraphConstructionTools.createX("fan", 5, out);

		labelledLink(g1, l, 0);
		labelledLink(g1, l, 1);
		labelledLink(g1, l, 2);
		labelledLink(g1, l, 3);
		labelledLink(g2, l, 2);
		labelledLink(g2, l, 3);
//		labelledLink(g2, l, 4);

		new Link(l[0], l[1], null, null, null, null, Direction.RIGHT);
		new Link(l[1], l[2], null, null, null, null, Direction.RIGHT);
		new Link(l[2], l[3], null, null, null, null, Direction.RIGHT);
		new Link(l[3], l[4], null, null, null, null, Direction.RIGHT);

		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement("diag", out, null);
		renderDiagram(d1);


	}

	private void labelledLink(Glyph g1, Glyph[] l, int i) {
		new Link(g1, l[i], null,
				new TextLabel("from-end output"),
				null,
				new TextLabel("to-end output"),
				null);
	}

	@Override
	protected TestingEngine.Checks checks() {
		TestingEngine.Checks out = new TestingEngine.Checks();
		out.everythingStraight = false;
		out.checkOcclusion = false;
		return out;
	}
}
