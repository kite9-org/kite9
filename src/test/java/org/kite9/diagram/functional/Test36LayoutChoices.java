package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visualization.format.pos.DiagramChecker;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.HelpMethods;

public class Test36LayoutChoices extends AbstractFunctionalTest {

	public static DiagramXMLElement doNestedDirected(Layout cl1, Layout cl2, Layout dl, Direction going) throws IOException {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		
		Context con1 = new Context("con1", HelpMethods.listOf(g0, g1, g2), true, new TextLine("c1"), cl1);
		
		
		Context con2 = new Context("con2", HelpMethods.listOf(g3, g4), true, new TextLine("c2"), cl2);
		
		createLink(going, g1, g4);
		createLink(going, g2, g3);
		createLink(going, g0, g3);
		
		DiagramXMLElement d = new DiagramXMLElement("D", HelpMethods.listOf(con1, con2),dl, null);
		return d;
	}

	public static DiagramXMLElement doSimpleComb(Layout cl, Layout dl, Direction going, boolean misorder) throws IOException {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);

		Glyph gn0 = new Glyph("gn0", "", "gn0", null, null);
		Glyph gn1 = new Glyph("gn1", "", "gn1", null, null);
		Glyph gn2 = new Glyph("gn2", "", "gn2", null, null);
		Glyph gn3 = new Glyph("gn3", "", "gn3", null, null);
		Glyph gn4 = new Glyph("gn4", "", "gn4", null, null);
		
		Context con1 = new Context("con1", misorder ? HelpMethods.listOf(g3, g2, g0, g1, g4, g5) :
			HelpMethods.listOf(g0, g1, g2, g3, g4, g5) , true, new TextLine("c1"), cl);
		
		
		Context con2 = new Context("con2", misorder ? HelpMethods.listOf(gn0, gn4, gn3, gn1, gn2) :
			HelpMethods.listOf(gn0, gn1, gn2, gn3, gn4), true, new TextLine("c2"), cl);
		
		createLink(going, g0, gn0);
		createLink(going, g1, gn0);
		
		createLink(going, g1, gn1);
		createLink(going, g2, gn1);
		
		createLink(going, g2, gn2);
		createLink(going, g3, gn2);
		
		createLink(going, g3, gn3);
		createLink(going, g4, gn3);
		
		createLink(going, g4, gn4);
		createLink(going, g5, gn4);
		
		DiagramXMLElement d = new DiagramXMLElement("D", HelpMethods.listOf(con1, con2),dl, null);
		return d;
	}

	private static Link createLink(Direction going, Glyph g0, Glyph gn0) {
		if (going != null) {
			return new Link(g0, gn0, null, null, null, null, going);
		} else {
			return new TurnLink(g0, gn0, null, null, null, null, going);
		}
	}
	
	public static DiagramXMLElement doArrowComb(Layout conl, Layout dl, Direction going) throws IOException {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		
		Arrow a1 = new Arrow("a1", "a1");
		Arrow a2 = new Arrow("a2", "a2");
		Arrow a3 = new Arrow("a3", "a3");
		Arrow a4 = new Arrow("a4", "a4");
		Arrow a5 = new Arrow("a5", "a5");
		Arrow a6 = new Arrow("a6", "a6");

		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		
		Context con1 = new Context("con1", HelpMethods.listOf(g0, g1, g2, g3), true, new TextLine("c1"), conl);
		
		Context cona = new Context("cona", HelpMethods.listOf(a6, a5, a4, a3, a2, a1), true, new TextLine("con a"), conl);
		
		Context con2 = new Context("con2", HelpMethods.listOf(g4, g5, g6), true, new TextLine("c2"), conl);
		
		new Link(g0, a1, null, null, null, null, going);
		new Link(g1, a2, null, null, null, null, going);
		
		new Link(g1, a3, null, null, null, null, going);
		new Link(g2, a4, null, null, null, null, going);
		
		new Link(g2, a5, null, null, null, null, going);
		new Link(g3, a6, null, null, null, null, going);
		
		new Link(a1, g4, null, null, null, null, going);
		new Link(a2, g4, null, null, null, null, going);
		
		new Link(a3, g5, null, null, null, null, going);
		new Link(a4, g5, null, null, null, null, going);
		
		new Link(a5, g6, null, null, null, null, going);
		new Link(a6, g6, null, null, null, null, going);
		
		DiagramXMLElement d = new DiagramXMLElement("D", HelpMethods.listOf(con1, con2, cona), dl, null);
		return d;
	}
	
	

	public static DiagramXMLElement doPyramid(Layout l, Layout dl, Direction going) throws IOException {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		
		Arrow a1 = new Arrow("a1", "a1");
		Arrow a2 = new Arrow("a2", "a2");
		Arrow a3 = new Arrow("a3", "a3");
		Arrow a4 = new Arrow("a4", "a4");
		Arrow a5 = new Arrow("a5", "a5");
		Arrow a6 = new Arrow("a6", "a6");
		Arrow a7 = new Arrow("a7", "a7");
		Arrow a8 = new Arrow("a8", "a8");

		
		Context con1 = new Context("con1", HelpMethods.listOf(g0), true, new TextLine("c1"), l);
		Context con2 = new Context("con2", HelpMethods.listOf(g1, g2), true, new TextLine("c2"), l);
		Context con3 = new Context("con3", HelpMethods.listOf(g3, g4, g5, g6), true, new TextLine("c3"), l);
		Context cona = new Context("coa4", HelpMethods.listOf(a6, a5, a4, a3, a7, a8, a2, a1), true, new TextLine("con a"), l);
		
		createLink(going, g1, g0);
		createLink(going, g2, g0);
		
		createLink(going, g3, g1);
		createLink(going, g4, g1);
		createLink(going, g5, g2);
		createLink(going, g6, g2);
		
		new Link(a1, g3, null, null, null, null, going);
		new Link(a2, g3, null, null, null, null, going);
		new Link(a3, g4, null, null, null, null, going);
		new Link(a4, g4, null, null, null, null, going);
		new Link(a5, g5, null, null, null, null, going);
		new Link(a6, g5, null, null, null, null, going);
		new Link(a7, g6, null, null, null, null, going);
		new Link(a8, g6, null, null, null, null, going);
		
		DiagramXMLElement d = new DiagramXMLElement("D", HelpMethods.listOf(con1, con2, con3, cona),dl, null);
		return d;
	}
	
	@Test
	public void test_36_1_SimpleCombHorizontal() throws IOException {
		renderDiagram(Test36LayoutChoices.doSimpleComb(Layout.HORIZONTAL, Layout.VERTICAL, Direction.UP, true));
	}
	
	@Test
	public void test_36_2_NestedDirectedSlack() throws IOException {
		renderDiagram(doNestedDirected(Layout.RIGHT, null, Layout.VERTICAL, Direction.UP));
	}
	
	@Test
	public void test_36_5_SimpleCombSlack() throws IOException {
		renderDiagram(doSimpleComb(null, Layout.VERTICAL, Direction.UP, true));
	}
	
	@Test
	public void test_36_6_SimpleCombSlackAll() throws IOException {
//		Diagram d2 = 
			renderDiagram(doSimpleComb(null, null, null, true));
//		if ((assertDirection(d2, Direction.UP) || assertDirection(d2, Direction.DOWN))) {
//			// ok
//		}  else {
//			throw new DiagramChecker.ExpectedLayoutException("Should be up or down");
//		}
		
	}

	private boolean assertDirection(DiagramXMLElement d2, Direction d) {
		try {
			DiagramAssert.assertInDirection(d, 
					getById("g5", d2), 
					getById("g4",d2), 
					getById("g3",d2), 
					getById("g2", d2),
					getById("g1", d2),
					getById("g0", d2));
			
			DiagramAssert.assertInDirection(d, 
					getById("gn4",d2), 
					getById("gn3",d2), 
					getById("gn2", d2),
					getById("gn1", d2),
					getById("gn0", d2));
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	@Test
	public void test_36_7_ArrowCombHorizontal() throws IOException {
		renderDiagram(Test36LayoutChoices.doArrowComb(Layout.HORIZONTAL, Layout.VERTICAL, Direction.UP));	
	}
	
	@Test
	public void test_36_8_ArrowCombSlack() throws IOException {
		renderDiagram(doArrowComb(null, Layout.VERTICAL, Direction.UP));	
	}

	@Test
	public void test_36_9_PyramidHoriz() throws IOException {
		renderDiagram(doPyramid(Layout.HORIZONTAL, Layout.VERTICAL, Direction.UP));
	}
	
	@Test
	public void test_36_10_PyramidVert() throws IOException {
		renderDiagram(doPyramid(Layout.VERTICAL, Layout.HORIZONTAL, Direction.RIGHT));
	}
	
	@Test
	public void test_36_11_PyramidSlack() throws IOException {
		renderDiagram(doPyramid(null, Layout.VERTICAL, Direction.UP));
	}
	
	@Test
	public void test_36_12_PyramidSlack2() throws IOException {
		renderDiagram(doPyramid(null, null, Direction.UP));
	}
	
	@Test
	public void test_36_13_ArrowCombVertical() throws IOException {
		renderDiagram(Test36LayoutChoices.doArrowComb(Layout.VERTICAL, Layout.HORIZONTAL, Direction.LEFT));	
	}
	

	@Test
	public void test_36_14_SimpleCombSlackAllButOrdered() throws IOException {
		DiagramXMLElement d2 = renderDiagram(doSimpleComb(null, null, null, false));
		if ((assertDirection(d2, Direction.UP) || assertDirection(d2, Direction.DOWN))) {
			// ok
		}  else {
			throw new DiagramChecker.ExpectedLayoutException("Should be up or down");
		}
		
	}

	
}
