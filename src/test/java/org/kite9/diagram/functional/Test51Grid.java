package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;


public class Test51Grid extends AbstractFunctionalTest {

	
	@Test
	public void test_51_1_SimpleGrid() throws IOException {
		Context tl = new Context("tl", null, true,  null, null);
		Context tr = new Context("tr", null, true,  null, null);
		Context bl = new Context("bl", null, true,  null, null);
		Context br = new Context("br", null, true,  null, null);
		
		tr.setStyle("occupies-x: 1; occupies-y: 0;");
		bl.setStyle("occupies-x: 0; occupies-y: 1 1;");
		br.setStyle("occupies-x: 1; occupies-y: 1;");
		
		Context ctx = new Context("outer", Arrays.asList(tl, tr, bl, br), true, null, Layout.GRID);
		ctx.setStyle("layout: grid; grid-columns: 2;"); 
		
		
		renderDiagram(new DiagramXMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_51_2_SupergridMockup() throws IOException {
		Context ctx = createSupergrid(true);
		renderDiagram(new DiagramXMLElement("diagram", Arrays.asList(ctx), null));
	}

	private Context createSupergrid(boolean addLinks) {
		List<XMLElement> contents = new ArrayList<>();
		Context[][] elems = new Context[4][];
		for (int i = 0; i < elems.length; i++) {
			elems[i] = new Context[4];
			for (int j = 0; j < elems[i].length; j++) {
				elems[i][j] = new Context("c" + i + "-" + j, null, true, null, null);
				elems[i][j].setStyle("occupies: "+i+" "+i+" "+j+" "+j+";");
				if (addLinks) {
					if (j > 0) {
						Link l = new Link((XMLElement) elems[i][j], (XMLElement) elems[i][j - 1], "", null, "", null, Direction.RIGHT);
					}
					if (i > 0) {
						Link l = new Link((XMLElement) elems[i][j], (XMLElement) elems[i - 1][j], "", null, "", null, Direction.UP);
					}
				}
				
				contents.add(elems[i][j]);
			}
		}
		
		
		elems[2][1].appendChild(new Glyph("one", "","Some gylph", null, null));
		elems[1][3].appendChild(new Glyph("two", "","sdlfkjsdlkfsdlkfk lksdjf ", null, null));
		
		
		Context ctx = new Context("outer", contents, true, null, null);
		return ctx;
	}
	
	@Test
	public void test_51_3_GridWithMissingBits() throws IOException {
		Context tl = new Context("tl", null, true,  new TextLine("Top \n Left"), null);
		Context tr = new Context("tr", null, true,  new TextLine("Top Right"), null);
		Context br = new Context("br", null, true,  new TextLine("Bottom Right"), null);
		tl.setStyle("occupies-x: 0; occupies-y: 0;");
		tr.setStyle("occupies-x: 1; occupies-y: 0;");
		br.setStyle("occupies-x: 1; occupies-y: 1;");
		
		Context ctx = new Context("inner", Arrays.asList(tl, tr, br), true, null, Layout.GRID);
		ctx.setStyle("layout: grid; grid-size: 2 2;"); 
		
		
		renderDiagram(new DiagramXMLElement("diagram", Arrays.asList(ctx), null));
	}
	

	@Test
	public void test_51_4_ProperSupergrid() throws IOException {
		Context ctx = createSupergrid(false);
		ctx.setStyle("layout: grid; grid-size: 4 4;"); 
		renderDiagram(new DiagramXMLElement("diagram", Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_51_5_GridWithSpanningSquares() throws IOException {
		Context tl = new Context(null, true,  new TextLine("Top \n Left"), null);
		Context tr = new Context(null, true,  new TextLine("Top Right"), null);
		Context bl = new Context(null, true,  new TextLine("Bottom Left"), null);
		Context br = new Context(null, true,  new TextLine("Bottom Right"), null);
		
		tl.setStyle("occupies: 0 1 0 1;");
		tr.setStyle("occupies: 0 2;");
		
		bl.setStyle("occupies: 0 1 2 2");
		br.setStyle("occupies: 2 2 1 2");
		
		Context ctx = new Context(Arrays.asList(tl, tr, bl, br), true, null, Layout.GRID);
		ctx.setStyle("layout: grid; grid-size: 3 3;"); 
		
		
		renderDiagram(new DiagramXMLElement(Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_51_6_GridWithUndirectedConnections() throws IOException {
		
	}
	
	@Test
	public void test_51_7_GridWithDirectedConnections() throws IOException {
		
	}
	
	@Test
	public void test_51_8_ContainerConnections() throws IOException {
		
	}
	
}
