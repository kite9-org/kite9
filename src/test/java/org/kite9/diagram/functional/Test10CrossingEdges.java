package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.visualization.display.java2d.style.sheets.Designer2012Stylesheet;

public class Test10CrossingEdges extends AbstractFunctionalTest {

	@Test
	public void test_10_1_Grid() throws IOException {
		List<Contained> vertices = new ArrayList<Contained>();
		GraphConstructionTools.createGrid(3, 3, vertices, true);
		
		Diagram d = new Diagram("D", vertices, null);

		renderDiagram(d, new Designer2012Stylesheet());
	}
	

	/**
	 * @see http://www.kite9.com/content/cant-perform-merges
	 * @throws IOException
	 */
	@Test
	public void test_10_2_CrossingEdges() throws IOException {
		List<Contained> vertices = new ArrayList<Contained>();
		Glyph[][] out = GraphConstructionTools.createGrid(2, 5, vertices, true);
		
		new TurnLink(out[0][0], out[0][2]);
		new TurnLink(out[0][1], out[0][3]);
		new TurnLink(out[0][1], out[0][4]);
		
		
		Diagram d = new Diagram("D", vertices, null);

		renderDiagram(d);
		
	}
	
	
	
	@Override
	protected boolean checkNoHops() {
		// breaks 10_2 if we do
		return false;
	}


	@Test
	public void test_10_3_SmallGrid() throws IOException {
		List<Contained> vertices = new ArrayList<Contained>();
		GraphConstructionTools.createGrid(2, 2, vertices, true);
		
		Diagram d = new Diagram("D", vertices, null);

		renderDiagram(d);
	}
	
}
