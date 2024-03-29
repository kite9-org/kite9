package org.kite9.diagram.functional.display;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TextLabel;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.dom.css.CSSConstants;
import org.w3c.dom.Element;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.common.HelpMethods;

public class Test39TerminatorStylesAndBalancing extends AbstractDisplayFunctionalTest {

	@Test
	public void test_39_1_BalancedTwo() throws Exception {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		LinkBody a1 = new LinkBody("meets");
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
		LinkBody a1 = new LinkBody("meets");
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
		LinkBody a1 = new LinkBody("meets");
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
		LinkBody a1 = new LinkBody("meets");
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
		LinkBody a1 = new LinkBody("meets");
		new TurnLink(g1, a1);
		new TurnLink(g2, a1);
		new TurnLink(g3, a1);	
		Link l = new Link(g4, a1, LinkEndStyle.CIRCLE, null, null, null);	
		l.setAttribute("style", "--kite9-minimum-length: 40px");
		
		renderDiagram(new DiagramKite9XMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_6_BalancedFourWithTwoTerminators() throws Exception {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g2", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		LinkBody a1 = new LinkBody("meets");
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
	
	public static final String[] STYLES = {"ARROW", "ARROW-OPEN", "CIRCLE", "GAP", "NONE", "DIAMOND", "DIAMOND-OPEN", "BARBED-ARROW"};
	@Test 
	public void test_39_9_AllTheTerminators2012() throws Exception {
		List<Element> elems = new ArrayList<Element>();
		
		for (String s : STYLES) {
			Glyph a = new Glyph("", "A", null, null);
			Glyph b = new Glyph("", "B", null, null);
			Glyph c = new Glyph("", "C", null, null);
			Glyph d = new Glyph("", "D", null, null);
			Glyph e = new Glyph("", "E", null, null);
			new Link(a, b, null, null, s, null, Direction.RIGHT);
			new Link(a, c, null, null, s, null, Direction.LEFT);
			new Link(a, d, null, null, s, null, Direction.DOWN);
			new Link(a, e, null, null, s, null, Direction.UP);
			elems.add(a);
			elems.add(b);
			elems.add(c);
			elems.add(d);
			elems.add(e);
		}

		DiagramKite9XMLElement d= new DiagramKite9XMLElement(elems, null);
		renderDiagram(d);

	}
	

	private DiagramKite9XMLElement createTerminatorDiagram() {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Context c1 = new Context(null, true, new TextLabel("c1"), null);
		Context c2 = new Context(null, true, new TextLabel("c1"), null);
		Context c3 = new Context(null, true, new TextLabel("c1"), null);
		Context c4 = new Context(null, true, new TextLabel("c1"), null);
	
		new Link(g1, c1, LinkEndStyle.CIRCLE, null, LinkEndStyle.CIRCLE, null, Direction.RIGHT);	
		new Link(g2, c2, LinkEndStyle.ARROW, null, LinkEndStyle.ARROW, null, Direction.RIGHT);	
		new Link(g3, c3, LinkEndStyle.GAP, null, LinkEndStyle.GAP, null, Direction.LEFT);	
		new Link(g4, c4, LinkEndStyle.NONE, null, LinkEndStyle.NONE, null, Direction.LEFT);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(g1, c1, g2, c2, g3, c3, g4, c4), null);
		//d.setLayoutDirection(Layout.RIGHT);
		return d;
	}
}
