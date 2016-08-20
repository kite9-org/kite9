package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.HelpMethods;


public class Test39TerminatorStylesAndBalancing extends AbstractFunctionalTest {
	
	@Override
	protected boolean checkDiagramSize() {
		return true;
	}
	
	@Override
	protected boolean checkImage() {
		return true;
	}

	@Test
	public void test_39_1_BalancedTwo() throws IOException {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Arrow a1 = new Arrow("meets");
		new Link(g1, a1);
		new Link(g2, a1);
		
		renderDiagram(new DiagramXMLElement(HelpMethods.listOf(g1, g2, a1), null));
	}
	
	@Test
	public void test_39_2_BalancedThree() throws IOException {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Arrow a1 = new Arrow("meets");
		new Link(g1, a1);
		new Link(g2, a1);
		new Link(g3, a1);	
		renderDiagram(new DiagramXMLElement(HelpMethods.listOf(g1, g2, g3, a1), null));
	}
	
	@Test
	public void test_39_3_BalancedFour() throws IOException {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Arrow a1 = new Arrow("meets");
		new Link(g1, a1);
		new Link(g2, a1);
		new Link(g3, a1);	
		new Link(g4, a1);	
		
		renderDiagram(new DiagramXMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_4_BalancedFourWithArrowTerminator() throws IOException {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Arrow a1 = new Arrow("meets");
		new TurnLink(g1, a1);
		new TurnLink(g2, a1);
		new TurnLink(g3, a1);	
		new Link(g4, a1, LinkEndStyle.ARROW, null, null, null);	
		
		renderDiagram(new DiagramXMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_5_BalancedFourWithRoundTerminator() throws IOException {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g1", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Arrow a1 = new Arrow("meets");
		new TurnLink(g1, a1);
		new TurnLink(g2, a1);
		new TurnLink(g3, a1);	
		new Link(g4, a1, LinkEndStyle.CIRCLE, null, null, null);	
		
		renderDiagram(new DiagramXMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_6_BalancedFourWithTwoTerminators() throws IOException {
		Glyph g1 = new Glyph("g1", null, "g1", null, null);
		Glyph g2 = new Glyph("g2", null, "g2", null, null);
		Glyph g3 = new Glyph("g3", null, "g3", null, null);
		Glyph g4 = new Glyph("g4", null, "g4", null, null);
		Arrow a1 = new Arrow("meets");
		new TurnLink(g1, a1, LinkEndStyle.CIRCLE, null, null, null);
		new TurnLink(g2, a1);
		new TurnLink(g3, a1, LinkEndStyle.CIRCLE, null, null, null);	
		new TurnLink(g4, a1);	
		
		renderDiagram(new DiagramXMLElement(HelpMethods.listOf(g1, g2, a1, g3, g4), null));
	}
	
	@Test
	public void test_39_7_TerminatorsBasic() throws IOException {
		DiagramXMLElement d = createTerminatorDiagram();
		renderDiagram(d);
	}
	
	@Test
	public void test_39_8_Terminators2012() throws IOException {
		DiagramXMLElement d = createTerminatorDiagram();
		renderDiagram(d);
	}
	
	public static final String[] STYLES = {"ARROW", "ARROW OPEN", "CIRCLE", "GAP", "NONE", "DIAMOND", "DIAMOND OPEN", "BARBED ARROW"};
	@Test 
	public void test_39_9_AllTheTerminators2012() throws IOException {
		List<XMLElement> elems = new ArrayList<XMLElement>();
		
		for (String s : STYLES) {
			Glyph a = new Glyph("", "A", null, null);
			Glyph b = new Glyph("", "B", null, null);
			new Link(a, b, s, null, s, null, Direction.RIGHT);
			Link l2 = new Link(a, b, s, null, s, null, Direction.RIGHT);
			l2.setStyle("stroke-width: 1px; stroke-dasharray: 5px 3px 5px; stroke: red");
			elems.add(a);
			elems.add(b);
		}
		
		DiagramXMLElement d= new DiagramXMLElement(elems, null);
		renderDiagram(d);

	}
	

	private DiagramXMLElement createTerminatorDiagram() {
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
		
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(g1, c1, g2, c2, g3, c3, g4, c4), null);
		//d.setLayoutDirection(Layout.RIGHT);
		return d;
	}
}
