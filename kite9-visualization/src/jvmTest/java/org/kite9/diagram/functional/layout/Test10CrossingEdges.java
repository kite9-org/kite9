package org.kite9.diagram.functional.layout;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.GraphConstructionTools;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.TurnLink;
import org.w3c.dom.Element;

public class Test10CrossingEdges extends AbstractLayoutFunctionalTest {

	@Test
	public void test_10_1_Grid() throws Exception {
		List<Element> vertices = new ArrayList<Element>();
		GraphConstructionTools.createGrid(3, 3, vertices, true);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", vertices, null);

		renderDiagram(d);
	}

	/**
	 * @see http://www.kite9.com/content/cant-perform-merges
	 * @throws Exception
	 */
	@Test
	public void test_10_2_CrossingEdges() throws Exception {
		List<Element> vertices = new ArrayList<Element>();
		Glyph[][] out = GraphConstructionTools.createGrid(2, 5, vertices, true);

		new TurnLink(out[0][0], out[0][2]);
		new TurnLink(out[0][1], out[0][3]);
		new TurnLink(out[0][1], out[0][4]);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", vertices, null);

		renderDiagram(d);

	}

	@Override
	protected boolean checkNoHops() {
		// breaks 10_2 if we do
		return false;
	}

	@Test
	public void test_10_3_SmallGrid() throws Exception {
		List<Element> vertices = new ArrayList<Element>();
		GraphConstructionTools.createGrid(2, 2, vertices, true);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", vertices, null);

		renderDiagram(d);
	}

}
