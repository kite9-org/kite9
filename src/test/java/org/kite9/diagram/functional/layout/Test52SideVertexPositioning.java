package org.kite9.diagram.functional.layout;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.xml.DiagramXMLElement;
import org.kite9.framework.xml.XMLElement;

public class Test52SideVertexPositioning extends AbstractLayoutFunctionalTest {


	@Test
	public void test_52_1_ContainerToContainerRight() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g6 = new Glyph("six", "","six ", null, null);
		Context c5 = new Context("five", listOf(g6), true, null, null);
		List<XMLElement> contexts = Test51Grid.createSquareGridContext(g1, g2, g3, g4);
		Context ctx = new Context("outer", contexts, true, null, Layout.GRID);
		ctx.setStyle("layout: grid; grid-size: 2 2;");
		
		new Link(g2, g6);
		new Link(ctx, c5, null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramXMLElement("diagram", Arrays.asList(ctx, c5), null));
	}
	
	@Test
	public void test_52_2_ContainerToContainerDown() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g6 = new Glyph("six", "","six ", null, null);
		Context c5 = new Context("five", listOf(g6), true, null, null);
		List<XMLElement> contexts = Test51Grid.createSquareGridContext(g1, g2, g3, g4);
		Context ctx = new Context("outer", contexts, true, null, Layout.GRID);
		ctx.setStyle("layout: grid; grid-size: 2 2;");
		
		new Link(g2, g6);
		new Link(ctx, c5, null, null, null, null, Direction.DOWN);

		renderDiagram(new DiagramXMLElement("diagram", Arrays.asList(ctx, c5), null));
	}
	
	@Test
	public void test_52_3_ContainerToVertexRight() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		
		Context c1 = new Context("c1", HelpMethods.listOf(g2, g3, g1, g4), true, null, Layout.RIGHT);

		
		new Link(g1, g2, null, null, null, null, null);
		new Link(g1, g2, null, null, null, null, null);
		new Link(g1, g3, null, null, null, null, null);
		new Link(g1, g3, null, null, null, null, null);
		new Link(g1, g4, null, null, null, null, null);
		new Link(g1, g4, null, null, null, null, null);
		new Link(g1, g5, null, null, null, null, null);
	
		new Link(c1, g5, null, null, null, null, Direction.RIGHT);
		new Link(g6, g5, null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(c1, g5, g6), null);
		
		renderDiagram(d1);
	}
	
	@Test
	public void test_52_4_ContainerToVertexUp() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		
		Context c1 = new Context("c1", HelpMethods.listOf(g2, g3, g1, g4), true, null, Layout.UP);

		
		new Link(g1, g2, null, null, null, null, null);
		new Link(g1, g2, null, null, null, null, null);
		new Link(g1, g3, null, null, null, null, null);
		new Link(g1, g3, null, null, null, null, null);
		new Link(g1, g4, null, null, null, null, null);
		new Link(g1, g4, null, null, null, null, null);
		new Link(g1, g5, null, null, null, null, null);
	
		new Link(c1, g5, null, null, null, null, Direction.UP);
		new Link(g6, g5, null, null, null, null, Direction.UP);
		
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(c1, g5, g6), null);
		
		renderDiagram(d1);
	}
	

	@Test
	public void test_52_6_ContainerToContainerDown2() throws Exception {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g4 = new Glyph("four", "","four ", null, null);
		Glyph g6 = new Glyph("six", "","six ", null, null);
		Glyph g7 = new Glyph("seven", "","seven", null, null);
		Context c5 = new Context("five", listOf(g6), true, null, null);
		List<XMLElement> contexts = Test51Grid.createSquareGridContext(g1, g2, g3, g4);
		Context ctx = new Context("outer", contexts, true, null, Layout.GRID);
		ctx.setStyle("layout: grid; grid-size: 2 2;");
		
		new Link(g2, g6);
		new Link(ctx, c5, null, null, null, null, Direction.DOWN);
		new Link(g4, g7, null, null, null, null, Direction.DOWN);
		new Link(g6, g7, null, null, null, null, Direction.RIGHT);

		renderDiagram(new DiagramXMLElement("diagram", Arrays.asList(ctx, c5, g7), null));
	}
	
	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}
}
