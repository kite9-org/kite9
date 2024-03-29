package org.kite9.diagram.functional.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Cell;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Grid;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.w3c.dom.Element;
import org.kite9.diagram.model.position.Direction;


public class Test56Grid extends AbstractDisplayFunctionalTest {

	
	@Test
	public void test_56_1_GridWithSpanningSquares() throws Exception {
		Grid ctx = createTwoLayerGridContext(null, null, null, null, 10, 10);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	/**
	 * Problem with grouping is that we really need to group according to how likely the
	 * subgroups are to be on the same line.  The closer we can predict this, the higher the priority.
	 */
	@Test
	public void test_56_2_GridWithDirectedConnections() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four", null, null);
		
		Grid ctx = createTwoLayerGridContext(g1, g2, g3, g4, 10, 10); 

		new Link(g2, g1, null, null, "DIAMOND", null, Direction.LEFT);
		new Link(g3, g1, "CIRCLE", null, "CIRCLE", null, Direction.UP);
		new Link(g4, g1, "ARROW", null, null, null, Direction.LEFT);
		
		new Link(g2, g4, null, null, null, null, Direction.DOWN);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}

	
	private Grid createSupergrid(boolean addLinks, boolean addContentLink, int size) {
		List<Element> contents = new ArrayList<>();
		Cell[][] elems = new Cell[size][];
		for (int i = 0; i < elems.length; i++) {
			elems[i] = new Cell[size];
			for (int j = 0; j < elems[i].length; j++) {
				elems[i][j] = new Cell("c" + i + "-" + j, null);
				elems[i][j].setAttribute("style", "--kite9-occupies: "+i+" "+i+" "+j+" "+j+";");
				if (addLinks) {
					if (j > 0) {
						new Link(elems[i][j], elems[i][j - 1], "", null, "", null, Direction.RIGHT);
					}
					if (i > 0) {
						new Link(elems[i][j], elems[i - 1][j], "", null, "", null, Direction.UP);
					}
				}
				
				contents.add(elems[i][j]);
			}
		}
		
		
		Glyph g1 = new Glyph("one", "one-ster","Some gylph", null, null);
		Glyph g2 = new Glyph("two", "two-ster","sdlfkjsdlkfsdlkfk lksdjf ", null, null);
		Glyph g3 = new Glyph("three", "three-ster","sdlfkjsdlkfsdlkfk lksdjf ", null, null);
		Glyph g4 = new Glyph("four", "","four ster sdlfkjsdlkfsdlkfk lksdjf ", null, null);
		elems[2][1].appendChild(g1);
		elems[1][3].appendChild(g2);
		elems[3][0].appendChild(g3);
		elems[0][0].appendChild(g4);
		
		if (addContentLink) {
			new TurnLink(g1, g2);
			new TurnLink(g1, g2);
			new TurnLink(g1, g3);
			new TurnLink(g1, g4);
			new TurnLink(g2, g4);
		}
		
		
		Grid ctx = new Grid("outer", contents, null);
		return ctx;
	}

	private Grid createTwoLayerGridContext(Glyph g1, Glyph g2, Glyph g3, Glyph g4, int leftPad, int rightPad) {
		Cell tl = new Cell("tl", Arrays.asList(g1));
		Cell tr = new Cell("tr", Arrays.asList(g2));
		Cell bl = new Cell("bl", Arrays.asList(g3));
		Cell br = new Cell("br", Arrays.asList(g4));
		
		tl.setAttribute("style", "--kite9-occupies: 0 0; --kite9-min-size: 100px 100px; ");
		bl.setAttribute("style", "--kite9-occupies: 0 1; --kite9-min-size: 40px 40px; ");

		tr.setAttribute("style", "--kite9-occupies: 0 0; --kite9-min-size: 40px 40px; ");
		br.setAttribute("style", "--kite9-occupies: 0 1; --kite9-min-size: 40px 10px; ");
		Grid lg = new Grid("lg", Arrays.asList(tl, bl), null);
		Grid rg = new Grid("lg", Arrays.asList(tr, br), null);

		Cell l = new Cell("l", Arrays.asList(lg));
		Cell r = new Cell("r", Arrays.asList(rg));
		l.setAttribute("style", "--kite9-occupies: 0 0; --kite9-padding: "+leftPad+"px");
		r.setAttribute("style", "--kite9-occupies: 1 0; --kite9-padding: "+rightPad+"px");
		
		Grid ctx = new Grid("outer", Arrays.asList(l, r), null);
		ctx.setAttribute("style", "--kite9-layout: grid; --kite9-grid-size: 2 1;");

		return ctx;
	}

	@Test
	public void test_56_4_OddSupergrid() throws Exception {
		Grid ctx = createSupergrid(false, true, 5);
		ctx.setAttribute("style", "--kite9-layout: grid; --kite9-grid-size: 5 5;");
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	@Ignore("Parameter replacement for grid contents - not implemented yet")
	public void test_56_5_Triangles() throws Exception {
		Grid ctx = createTwoLayerGridContext(null, null, null, null, 10, 10);
		ctx.setAttribute("class", "triangles");
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_56_6_BadlySizedGrid() throws Exception {
		Glyph g2 = new Glyph("two", "","two ", null, null);

		Glyph g1 = new Glyph("one", "","one", null, null);
		Cell tl = new Cell("tl", Arrays.asList(g1));
		Cell br = new Cell("br", Arrays.asList());
		
		tl.setAttribute("style", "--kite9-occupies: 0 0; ");
		br.setAttribute("style", "--kite9-occupies: 1 1; --kite9-min-size: 100px 100px;");
		
		Grid ctx = new Grid("outer", Arrays.asList(tl, br), null);
		ctx.setAttribute("style", "--kite9-layout: grid; --kite9-grid-size: 2 2; ");
		new Link(g2, g1, null, null, "DIAMOND", null, Direction.LEFT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g2), null));
	}
	
	@Test
	public void test_56_7_SingleColumnGrid() throws Exception {
		Cell t1 = new Cell("1", Arrays.asList());
		Cell t2 = new Cell("2", Arrays.asList());
		Cell t3 = new Cell("3", Arrays.asList());
		Cell t4 = new Cell("4", Arrays.asList());
		
		t1.setAttribute("style", "fill: green; --kite9-min-size: 50px 50px; ");
		t2.setAttribute("style", "fill: red; --kite9-min-size: 50px 50px; ");
		t3.setAttribute("style", "fill: blue; --kite9-min-size: 50px 50px; ");
		t4.setAttribute("style", "fill: yellow; --kite9-min-size: 50px 50px; ");
		
		Grid ctx = new Grid("outer", Arrays.asList(t1, t2, t3, t4), null);
		ctx.setAttribute("style", "--kite9-layout: grid;");
		
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_56_8_SparseGrid() throws Exception {
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);

		Glyph g1 = new Glyph("one", "","one", null, null);
		Cell t1 = new Cell("1", Arrays.asList(g2));
		Cell t2 = new Cell("2", Arrays.asList());
		Cell t3 = new Cell("3", Arrays.asList(g3));
		Cell t4 = new Cell("4", Arrays.asList());
		
		t1.setAttribute("style", "--kite9-occupies: 6 6 2 2; --kite9-min-size: 50px 50px; ");
		t2.setAttribute("style", "--kite9-occupies: 6 6 0 0; --kite9-min-size: 50px 50px; ");
		t3.setAttribute("style", "--kite9-occupies: 3 3 3 3; --kite9-min-size: 50px 50px; ");
		t4.setAttribute("style", "--kite9-occupies: 3 3 1 1; --kite9-min-size: 50px 50px; ");
		
		Grid ctx = new Grid("outer", Arrays.asList(t1, t2, t3, t4), null);
		ctx.setAttribute("style", "--kite9-layout: grid;");
		new Link(g2, g1, null, null, "DIAMOND", null, null);
		new TurnLink(g2, g3, null, null, null, null, null);
	
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g1), null));
	}
	
	@Test
	public void test_56_9_CrazyOrdinals() throws Exception {
		Glyph g2 = new Glyph("MM", "","MM ", null, null);
		Glyph g3 = new Glyph("BR", "","BR ", null, null);

		Glyph g1 = new Glyph("TL", "","TL", null, null);
		Cell t1 = new Cell("1", Arrays.asList(g1));
		Cell t2 = new Cell("2", Arrays.asList(g2));
		Cell t3 = new Cell("3", Arrays.asList(g3));
		
		t1.setAttribute("style", "--kite9-occupies: -5 -4 -3 -2; --kite9-min-size: 50px 50px; ");
		t2.setAttribute("style", "--kite9-occupies: 0 0 1 1; --kite9-min-size: 50px 50px; ");
		t3.setAttribute("style", "--kite9-occupies: 8 8 8 8; --kite9-min-size: 50px 50px; ");
		
		Grid ctx = new Grid("outer", Arrays.asList(t1, t2, t3), null);
		ctx.setAttribute("style", "--kite9-layout: grid;");
		new Link(g2, g1, null, null, "DIAMOND", null, null);
		new TurnLink(g2, g3, null, null, null, null, null);
	
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_56_10_MissingOrdinals() throws Exception {
		Glyph g1 = new Glyph("1", "","1", null, null);
		Glyph g2 = new Glyph("2", "","2 ", null, null);
		Glyph g3 = new Glyph("3", "","3 ", null, null);
		Glyph g4 = new Glyph("4", "","4 ", null, null);
		Glyph g5 = new Glyph("A", "","A ", null, null);

		Cell t1 = new Cell("c1", Arrays.asList(g1));
		Cell t2 = new Cell("c2", Arrays.asList(g2));
		Cell t3 = new Cell("c3", Arrays.asList(g3));
		Cell t4 = new Cell("c4", Arrays.asList(g4));
		Cell t5 = new Cell("c5", Arrays.asList(g5));
		
		t5.setAttribute("style", "--kite9-occupies: 2 2 2 2; ");
		
		Grid ctx = new Grid("outer", Arrays.asList(t1, t2, t3, t4, t5), null);
		ctx.setAttribute("style", "--kite9-layout: grid; --kite9-grid-columns: 3; ");
		new Link(g2, g1, null, null, "DIAMOND", null, null);
		new TurnLink(g2, g3, null, null, null, null, null);
	
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
}
