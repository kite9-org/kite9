package org.kite9.diagram.functional.display;

import java.util.Arrays;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Cell;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.xml.DiagramKite9XMLElement;


public class Test56Grid extends AbstractDisplayFunctionalTest {

	
	@Test
	public void test_56_5_GridWithSpanningSquares() throws Exception {
		Context ctx = createTwoLayerGridContext(null, null, null, null);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	/**
	 * Problem with grouping is that we really need to group according to how likely the
	 * subgroups are to be on the same line.  The closer we can predict this, the higher the priority.
	 */
//	@Ignore("Currently broken - grouping rules need some extra work.")
	@Test
	public void test_56_7_GridWithDirectedConnections() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four", null, null);
		
		Context ctx = createTwoLayerGridContext(g1, g2, g3, g4); 

		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g1, g3, null, null, null, null, Direction.DOWN);
		new Link(g1, g4, null, null, null, null, Direction.RIGHT);
		new Link(g2, g4, null, null, null, null, Direction.DOWN);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}

	private Context createTwoLayerGridContext(Glyph g1, Glyph g2, Glyph g3, Glyph g4) {
		Cell tl = new Cell("tl", Arrays.asList(g1), null, null);
		Cell tr = new Cell("tr", Arrays.asList(g2), null, null);
		Cell bl = new Cell("bl", Arrays.asList(g3), null, null);
		Cell br = new Cell("br", Arrays.asList(g4), null, null);
		
		tl.setAttribute("style", "kite9-occupies: 0 0;");
		bl.setAttribute("style", "kite9-occupies: 0 1;");

		tr.setAttribute("style", "kite9-occupies: 0 0;");
		br.setAttribute("style", "kite9-occupies: 0 1;");

		Cell l = new Cell("l", Arrays.asList(tl, bl), null, null);
		Cell r = new Cell("r", Arrays.asList(tr, br), null, null);
		l.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 1 2; kite9-occupies: 0 0; kite9-padding: 5px;"); 
		r.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 1 2; kite9-occupies: 1 0; kite9-padding: 10px;"); 
		
		Context ctx = new Context("outer", Arrays.asList(l, r), true, null, Layout.GRID);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 1;");
		return ctx;
	}
	
	@Test
	public void test_56_21_GridWithDirectedConnections2() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four", null, null);
		
		Context ctx = createTwoLayerGridContext(g1, g2, g3, g4); 

		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g1, g3, null, null, null, null, Direction.DOWN);
		new Link(g1, g4, null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}

}
