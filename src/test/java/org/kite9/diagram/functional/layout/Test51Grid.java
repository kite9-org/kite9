package org.kite9.diagram.functional.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Cell;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.ContradictingLink;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Grid;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.batik.format.Kite9TranscoderException;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.functional.TestingEngine.ElementsMissingException;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;


public class Test51Grid extends AbstractLayoutFunctionalTest {

	
	@Test
	public void test_51_1_SimpleGrid() throws Exception {
		Context tl = new Context("tl", null, true,  null, null);
		Context tr = new Context("tr", null, true,  null, null);
		Context bl = new Context("bl", null, true,  null, null);
		Context br = new Context("br", null, true,  null, null);
		
		tr.setAttribute("style", "kite9-occupies-x: 1; kite9-occupies-y: 0;");
		bl.setAttribute("style", "kite9-occupies-x: 0; kite9-occupies-y: 1 1;");
		br.setAttribute("style", "kite9-occupies-x: 1; kite9-occupies-y: 1;");
		
		Context ctx = new Context("outer", Arrays.asList(tl, tr, bl, br), true, null, Layout.GRID);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-columns: 2;"); 
		
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_51_2_SupergridMockup() throws Exception {
		Context ctx = createMockSupergrid(4);
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	private Context createMockSupergrid(int size) {
		List<Kite9XMLElement> contents = new ArrayList<>();
		Context[][] elems = new Context[size][];
		for (int i = 0; i < elems.length; i++) {
			elems[i] = new Context[size];
			for (int j = 0; j < elems[i].length; j++) {
				elems[i][j] = new Context("c" + i + "-" + j, null, true,  null , null);
				elems[i][j].setAttribute("style", "kite9-occupies: "+i+" "+i+" "+j+" "+j+";");
					if (j > 0) {
						new Link(elems[i][j], elems[i][j - 1], "", null, "", null, Direction.RIGHT);
					}
					if (i > 0) {
						new Link(elems[i][j], elems[i - 1][j], "", null, "", null, Direction.UP);
					}
				
				contents.add(elems[i][j]);
			}
		}
		
		
		Glyph g1 = new Glyph("one", "ster","Some gylph", null, null);
		Glyph g2 = new Glyph("two", "ster","sdlfkjsdlkfsdlkfk lksdjf ", null, null);
		Glyph g3 = new Glyph("three", "ster","sdlfkjsdlkfsdlkfk lksdjf ", null, null);
		Glyph g4 = new Glyph("four", "","sdlfkjsdlkfsdlkfk lksdjf ", null, null);
		elems[2][1].appendChild(g1);
		elems[1][3].appendChild(g2);
		elems[3][0].appendChild(g3);
		elems[0][0].appendChild(g4);

		Context ctx = new Context("outer", contents, true, null, null);
		return ctx;
	}

	private Grid createSupergrid(boolean addContentLink, int size) {
		List<Kite9XMLElement> contents = new ArrayList<>();
		Cell[][] elems = new Cell[size][];
		for (int i = 0; i < elems.length; i++) {
			elems[i] = new Cell[size];
			for (int j = 0; j < elems[i].length; j++) {
				elems[i][j] = new Cell("c" + i + "-" + j, null);
				elems[i][j].setAttribute("style", "kite9-occupies: "+i+" "+i+" "+j+" "+j+";");				
				contents.add(elems[i][j]);
			}
		}
		
		
		Glyph g1 = new Glyph("one", "ster","Some gylph", null, null);
		Glyph g2 = new Glyph("two", "ster","sdlfkjsdlkfsdlkfk lksdjf ", null, null);
		Glyph g3 = new Glyph("three", "ster","sdlfkjsdlkfsdlkfk lksdjf ", null, null);
		Glyph g4 = new Glyph("four", "","sdlfkjsdlkfsdlkfk lksdjf ", null, null);
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
		ctx.setAttribute("style", "kite9-grid-size: "+size+" "+size+";");
		return ctx;
	}
	
	/**
	 * Labels cannot be rendered on gridded containers.
	 * @throws Exception
	 */
	@Test
	public void test_51_3_GridWithMissingBits() throws Exception {
		Cell tl = new Cell("tl", createGlyphContents("Top\nLeft"));
		Cell tr = new Cell("tr", createGlyphContents("Top\nRight"));
		Cell br = new Cell("br", createGlyphContents("Bottom\nRight"));
		tl.setAttribute("style", "kite9-occupies-x: 0; kite9-occupies-y: 0;");
		tr.setAttribute("style", "kite9-occupies-x: 1; kite9-occupies-y: 0;");
		br.setAttribute("style", "kite9-occupies-x: 1; kite9-occupies-y: 1;");
		
		Grid ctx = new Grid("inner", Arrays.asList(tl, tr, br), null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;"); 
		
		
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx)));
	}
	
	private List<Kite9XMLElement> createGlyphContents(String text) {
		return Collections.singletonList(new Glyph(null, text, null, null));
	}


	@Test
	public void test_51_4_ProperSupergrid() throws Exception {
		Grid ctx = createSupergrid(false, 4);
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_51_6_GridWithUndirectedConnections() throws Exception {
		Grid ctx = createSupergrid(true, 4);
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	
	private Grid createThreeLayerGridContext(Glyph g1, Glyph g2, Glyph g3, Glyph g4) {
		Cell tl1 = new Cell("tl1", Arrays.asList(g1));
		Cell tl2 = new Cell("tl2", Arrays.asList(g2));
		Cell bl = new Cell("bl", Arrays.asList(g3));
		Cell r = new Cell("r", Arrays.asList(g4));
		
		tl1.setAttribute("style", "kite9-occupies: 0 0;");
		tl2.setAttribute("style", "kite9-occupies: 1 0;");

		Grid tl = new Grid("tl", Arrays.asList(tl1, tl2), null);
		tl.setAttribute("style", "kite9-occupies: 0 0; kite9-grid-size: 2 1;");
		bl.setAttribute("style", "kite9-occupies: 0 1;");
		
		Grid l = new Grid("l", Arrays.asList(tl, bl),  null);
		l.setAttribute("style", "kite9-grid-size: 1 2; kite9-occupies: 0 0;"); 
		r.setAttribute("style", "kite9-occupies: 1 0;"); 
		
		Grid ctx = new Grid("outer", Arrays.asList(l, r), null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 1;");
		return ctx;
	}
	
	public static List<Kite9XMLElement> createSquareGridContext(Glyph g1, Glyph g2, Glyph g3, Glyph g4) {
		Cell tl = new Cell("tl", listOf(g1));
		Cell tr = new Cell("tr", listOf(g2));
		Cell bl = new Cell("bl", listOf(g3));
		Cell br = new Cell("br", listOf(g4));
		
		tl.setAttribute("style", "kite9-occupies: 0 0;");
		tr.setAttribute("style", "kite9-occupies: 1 0;");
		
		bl.setAttribute("style", "kite9-occupies: 0 1");
		br.setAttribute("style", "kite9-occupies: 1 1");
		
		List<Kite9XMLElement> contexts = Arrays.asList(tl, tr, bl, br);
		return contexts;
	}

	/**
	 * Labels cannot be rendered on gridded containers.
	 * @throws Exception
	 */
	@Test(expected=Kite9TranscoderException.class)
	public void test_51_8_GridWithLabels() throws Exception {
		Cell tl = new Cell("tl", Arrays.asList(new TextLine("tll", "label", "Top \n Left")));
		Cell tr = new Cell("tr", Arrays.asList(new TextLine("trl","label", "Top Right")));
		Cell br = new Cell("br", Arrays.asList(new TextLine("brl","label",  "Bottom Right")));
		tl.setAttribute("style", "kite9-occupies-x: 0; kite9-occupies-y: 0; ");
		tr.setAttribute("style", "kite9-occupies-x: 1; kite9-occupies-y: 0; ");
		br.setAttribute("style", "kite9-occupies-x: 1; kite9-occupies-y: 1; ");
		
		Grid ctx = new Grid("inner", Arrays.asList(tl, tr, br), null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2; kite9-padding: 4px; "); 
		
		
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), new TextLine("key", "key", "Bits missing")));
	}
	
	@Test
	public void test_51_9_IllegalDirectedConnections() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		
		Grid ctx = setupContext(g1, g2, g3, g4); 
		
		new ContradictingLink(g1, g3, null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_51_10_IllegalDirectedConnections2() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		
		Grid ctx = setupContext(g1, g2, g3, g4); 
		
		new ContradictingLink(g2, g3, null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}

	private Grid setupContext(Glyph g1, Glyph g2, Glyph g3, Glyph g4) {
		Grid ctx = new Grid("outer", createSquareGridContext(g1, g2, g3, g4), null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		return ctx;
	}
	
	/**
	 * Shouldn't be able to link grid cells to anything
	 */
	@Test(expected=ElementsMissingException.class)
	public void test_51_11_ContainerConnectionInsideDirected() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new Link(contexts.get(0), g5, null, null, null, null, null);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}
	
	/**
	 * Shouldn't be able to link grid cells to anything
	 * @throws Exception
	 */
	@Test(expected=ElementsMissingException.class)
	public void test_51_12_ContainerConnectionInsideUndirected() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new Link(contexts.get(0), g3, null, null, null, null, null);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}
	
	@Test
	public void test_51_13_ContainerConnectionOutsideUndirected() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new Link(g1, g5, null, null, null, null, null);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}
	
	@Test
	public void test_51_14_ContainerConnectionOutsideDirectedFacing() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new ContradictingLink(g4, g5, null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}
	
	@Test
	public void test_51_15_ContainerConnectionOutsideDirectedThroughContainer() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new ContradictingLink(g1, g5, null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}
	
	@Test
	public void test_51_16_ContainerConnectionParentOutsideUndirected() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new Link(ctx, g5, null, null, null, null, null);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}
	
	@Test(expected=ElementsMissingException.class)
	public void test_51_17_ContainerConnectionParentInsideUndirected() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new Link(ctx, g2, null, null, null, null, null);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}
	
	/**
	 * Link contradicts grid ordering
	 */
	@Test
	public void test_51_18_InternalConnectionContradiction1() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new ContradictingLink(g1, g2, null, null, null, null, Direction.LEFT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}
	
	
	@Test
	public void test_51_19_InternalConnectionContradiction2() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g5 = new Glyph("five", "","five ", null, null);
		List<Kite9XMLElement> contexts = createSquareGridContext(g1, g2, g3, g4);
		Grid ctx = new Grid("outer", contexts,null);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 2 2;");
		
		new ContradictingLink(g1, g4, null, null, null, null, Direction.DOWN);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g5), null));
	}

	@Test
	public void test_51_20_3LayerGridWithDirectedConnections() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four", null, null);
		
		Grid ctx = createThreeLayerGridContext(g1, g2, g3, g4); 

		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g1, g3, null, null, null, null, Direction.DOWN);
		new Link(g2, g4, null, null, null, null, Direction.RIGHT);
		new Link(g3, g4, null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}

	@Test
	public void test_51_22_OddSupergrid() throws Exception {
		Grid ctx = createSupergrid(true, 5);
		ctx.setAttribute("style", "kite9-layout: grid; kite9-grid-size: 5 5;"); 
		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx), null));
	}

	@Test
	public void test_51_23_SingleCellGrid() throws Exception {
		Glyph g2 = new Glyph("two", "","two ", null, null);

		Glyph g1 = new Glyph("one", "","one", null, null);
		Cell tl = new Cell("tl", Arrays.asList(g1));
		
		Grid ctx = new Grid("outer", Arrays.asList(tl), null);
		ctx.setAttribute("style", "kite9-layout: grid;");
		new Link(g2, g1, null, null, "DIAMOND", null, Direction.LEFT);

		renderDiagram(new DiagramKite9XMLElement("diagram", Arrays.asList(ctx, g2), null));
	}
	
	

}
