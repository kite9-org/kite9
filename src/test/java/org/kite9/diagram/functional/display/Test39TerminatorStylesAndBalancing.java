package org.kite9.diagram.functional.display;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.xml.DiagramKite9XMLElement;
import org.kite9.framework.xml.Kite9XMLElement;

public class Test39TerminatorStylesAndBalancing extends AbstractDisplayFunctionalTest {

	@Test
	public void test_39_1_BalancedTwo() throws Exception {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Arrow a1 = new Arrow("meets");
		Link l = new Link(g1, a1);
		new Link(g2, a1);
		l.setAttribute("style", CSSConstants.LINK_MINIMUM_LENGTH+": 50px; ");
		renderDiagram(new DiagramKite9XMLElement(HelpMethods.listOf(g1, g2, a1), null));
	}
	
	@Test
	public void test_39_2_BalancedThree() throws Exception {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Arrow a1 = new Arrow("meets");
		a1.setAttribute("style", CSSConstants.LINK_GUTTER+": 50px; ");
		new Link(g1, a1);
		new Link(g2, a1);
		new Link(g3, a1);	
		renderDiagram(new DiagramKite9XMLElement(HelpMethods.listOf(g1, g2, g3, a1), null));
	}
	
	@Test
	public void test_39_3_BalancedFour() throws Exception {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Arrow a1 = new Arrow("meets");
		new Link(g1, a1);
		new Link(g2, a1);
		new Link(g3, a1);	
		new Link(g4, a1);	
		
		renderDiagram(new DiagramKite9XMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_4_BalancedFourWithArrowTerminator() throws Exception {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Arrow a1 = new Arrow("meets");
		new TurnLink(g1, a1);
		new TurnLink(g2, a1);
		new TurnLink(g3, a1);	
		new Link(g4, a1, LinkEndStyle.ARROW, null, null, null);	
		
		renderDiagram(new DiagramKite9XMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_5_BalancedFourWithRoundTerminator() throws Exception {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Arrow a1 = new Arrow("meets");
		new TurnLink(g1, a1);
		new TurnLink(g2, a1);
		new TurnLink(g3, a1);	
		new Link(g4, a1, LinkEndStyle.CIRCLE, null, null, null);	
		
		renderDiagram(new DiagramKite9XMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_6_BalancedFourWithTwoTerminators() throws Exception {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g2", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Arrow a1 = new Arrow("meets");
		new TurnLink(g1, a1, LinkEndStyle.CIRCLE, null, null, null);
		new TurnLink(g2, a1);
		new TurnLink(g3, a1, LinkEndStyle.CIRCLE, null, null, null);	
		new TurnLink(g4, a1);	
		
		renderDiagram(new DiagramKite9XMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_7_TerminatorsBasic() throws Exception {
		DiagramKite9XMLElement d = createTerminatorDiagram();
		renderDiagram(d);
	}
	
	@Test
	public void test_39_8_Terminators2012() throws Exception {
		DiagramKite9XMLElement d = createTerminatorDiagram();
		renderDiagram(d);
	}
	
	public static final String[] STYLES = {"ARROW", "ARROW-OPEN", "CIRCLE", "GAP", "NONE", "DIAMOND", "DIAMOND-OPEN", "BARBED-ARROW"};
	@Test 
	public void test_39_9_AllTheTerminators2012() throws Exception {
		List<Kite9XMLElement> elems = new ArrayList<Kite9XMLElement>();
		
		for (String s : STYLES) {
			Glyph a = new Glyph("", "A", null, null);
			Glyph b = new Glyph("", "B", null, null);
			Glyph c = new Glyph("", "C", null, null);
			new Link(a, b, s, null, s, null, Direction.RIGHT);
			new Link(b, a, s, null, s, null, Direction.LEFT);
			new Link(a, c, s, null, s, null, Direction.DOWN);
			new Link(c, a, s, null, s, null, Direction.UP);
			elems.add(a);
			elems.add(b);
			elems.add(c);
		}
		
		DiagramKite9XMLElement d= new DiagramKite9XMLElement(elems, null);
		renderDiagram(d);

	}
	

	private DiagramKite9XMLElement createTerminatorDiagram() {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Context c1 = new Context(null, true, new TextLine("c1"), null);
		Context c2 = new Context(null, true, new TextLine("c1"), null);
		Context c3 = new Context(null, true, new TextLine("c1"), null);
		Context c4 = new Context(null, true, new TextLine("c1"), null);
	
		new Link(g1, c1, LinkEndStyle.CIRCLE, null, LinkEndStyle.CIRCLE, null, Direction.RIGHT);	
		new Link(g2, c2, LinkEndStyle.ARROW, null, LinkEndStyle.ARROW, null, Direction.RIGHT);	
		new Link(g3, c3, LinkEndStyle.GAP, null, LinkEndStyle.GAP, null, Direction.LEFT);	
		new Link(g4, c4, LinkEndStyle.NONE, null, LinkEndStyle.NONE, null, Direction.LEFT);	
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(g1, c1, g2, c2, g3, c3, g4, c4), null);
		//d.setLayoutDirection(Layout.RIGHT);
		return d;
	}
}
