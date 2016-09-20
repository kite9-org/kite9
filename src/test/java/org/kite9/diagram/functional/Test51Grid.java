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
		Context tl = new Context(null, true,  new TextLine("Top \n Left"), null);
		Context tr = new Context(null, true,  new TextLine("Top Right"), null);
		Context bl = new Context(null, true,  new TextLine("Bottom Left"), null);
		Context br = new Context(null, true,  new TextLine("Bottom Right"), null);
		
		tr.setStyle("grid-x: 1; grid-y: 0;");
		bl.setStyle("grid-x: 0; grid-y: 1,1;");
		br.setStyle("grid-x: 1; grid-y: 1;");
		
		Context ctx = new Context(Arrays.asList(tl, tr, bl, br), true, null, Layout.GRID);
		ctx.setStyle("layout: grid; grid-size: 2, 2;"); 
		
		
		renderDiagram(new DiagramXMLElement(Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_51_2_SupergridMockup() throws IOException {
		List<XMLElement> contents = new ArrayList<>();
		Context[][] elems = new Context[4][];
		for (int i = 0; i < elems.length; i++) {
			elems[i] = new Context[4];
			for (int j = 0; j < elems[i].length; j++) {
				elems[i][j] = new Context("c"+i+"-"+j, null, true, null, null);
				if (j > 0) {
					Link l = new Link((XMLElement) elems[i][j], (XMLElement) elems[i][j-1], "", null, "", null, Direction.RIGHT);
				}
				if (i > 0) {
					Link l = new Link((XMLElement) elems[i][j], (XMLElement) elems[i-1][j], "", null, "", null, Direction.UP);				
				}
				
				contents.add(elems[i][j]);
			}
		}
		
		
		elems[2][1].appendChild(new Glyph("one", "","Some gylph", null, null));
		elems[1][3].appendChild(new Glyph("two", "","sdlfkjsdlkfsdlkfk lksdjf ", null, null));
		
		
		Context ctx = new Context(contents, true, null, null);
		//ctx.setStyle("layout: grid; grid-size: 2, 2;"); 
		
		
		renderDiagram(new DiagramXMLElement(Arrays.asList(ctx), null));
	}
	
	@Test
	public void test_51_3_GridWithMissingBits() throws IOException {
		Context tl = new Context(null, true,  new TextLine("Top \n Left"), null);
		Context tr = new Context(null, true,  new TextLine("Top Right"), null);
		Context br = new Context(null, true,  new TextLine("Bottom Right"), null);
		tl.setStyle("grid-x: 0; grid-y: 0;");
		tr.setStyle("grid-x: 1; grid-y: 0;");
		br.setStyle("grid-x: 1; grid-y: 1;");
		
		Context ctx = new Context(Arrays.asList(tl, tr, br), true, null, Layout.GRID);
		ctx.setStyle("layout: grid; grid-size: 2, 2;"); 
		
		
		renderDiagram(new DiagramXMLElement(Arrays.asList(ctx), null));
	}
}
