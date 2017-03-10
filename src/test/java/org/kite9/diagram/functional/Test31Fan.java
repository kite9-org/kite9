package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.layout.AbstractLayoutFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.HelpMethods;

public class Test31Fan extends AbstractLayoutFunctionalTest {

	
	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Test
	public void test_31_1_Basic2InABox() throws IOException {
		Glyph g1 = new Glyph("g1", "g1", "a", null, null);
		Glyph g2 = new Glyph("g2", "g2", "another long label", null, null);
		Glyph g3 = new Glyph("g3", "g3", "g3", null, null);
		
		new Link(g1, g2, null, null, null, null, null);
		new Link(g1, g3, null, null, null, null, null);
	
		Glyph g5 = new Glyph("g5", "g5", "g5", null, null);
		Glyph g6 = new Glyph("g6", "g6", "g6", null, null);
		Glyph g7 = new Glyph("g7", "g7", "g7", null, null);
		new Link(g1, g5, null, null, null, null, Direction.RIGHT);
		new Link(g1, g6, null, null, null, null, Direction.LEFT);
		new Link(g1, g7, null, null, null, null, Direction.UP);

		Context c1 = new Context("c1", HelpMethods.listOf(g2, g3), true, null, null);

		
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(g1, c1, g6, g5, g7), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_31_2_Basic3InABox() throws IOException {
		Glyph g1 = new Glyph("g1", "g1", "a", null, null);
		Glyph g2 = new Glyph("g2", "g2", "another long label", null, null);
		Glyph g3 = new Glyph("g3", "g3", "blahdy blahdy blah", null, null);
		Glyph g4 = new Glyph("g4", "g4", "andon andon andon", null, null);
		Glyph g5 = new Glyph("g5", "g5", "g5", null, null);
		Glyph g6 = new Glyph("g6", "g6", "g6", null, null);
		new Link(g1, g5, null, null, null, null, Direction.RIGHT);
		new Link(g1, g6, null, null, null, null, Direction.LEFT);
		
		new Link(g1, g2, null, null, null, null, null);
		new Link(g1, g3, null, null, null, null, null);
		new Link(g1, g4, null, null, null, null, null);
		
		Context c1 = new Context("c1", HelpMethods.listOf(g2, g3, g4), true, null, null);
		
		
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(g1, c1, g5, g6), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_31_3_LotsInABox() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<XMLElement> out = new ArrayList<XMLElement>();
		out.add(g1);
		List<XMLElement> cc = new ArrayList<XMLElement>();
		Glyph[] l = GraphConstructionTools.createX("fan", 15, cc);
		Context c1 = new Context("c1", cc,  true, null, null);
		out.add(c1);
		
		new Link(g1, l[0], null, null, null, null, null);
		new Link(g1, l[1], null, null, null, null, null);
		new Link(g1, l[2], null, null, null, null, null);
		new Link(g1, l[3], null, null, null, null, null);
		
		new Link(g1, l[4], null, null, null, null, null);
		new Link(g1, l[5], null, null, null, null, null);
		new Link(g1, l[6], null, null, null, null, null);
		new Link(g1, l[7], null, null, null, null, null);
		new Link(g1, l[8], null, null, null, null, null);
	
		new Link(g1, l[9], null, null, null, null, null);
		new Link(g1, l[10], null, null, null, null, null);
		new Link(g1, l[11], null, null, null, null, null);
		
		new Link(g1, l[12], null, null, null, null, null);
		new Link(g1, l[13], null, null, null, null, null);
		new Link(g1, l[14], null, null, null, null, null);
		
		DiagramXMLElement d1 = new DiagramXMLElement(out, null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_31_4_RightFanFixedFirst() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<XMLElement> out = new ArrayList<XMLElement>();
		out.add(g1);
		Glyph[] l = GraphConstructionTools.createX("fan", 6, out);
		
		new Link(g1, l[0], null, null, null, null, Direction.RIGHT);
		new Link(g1, l[1], null, null, null, null, null);
		new Link(g1, l[2], null, null, null, null, null);
		new Link(g1, l[3], null, null, null, null, null);
		new Link(g1, l[4], null, null, null, null, null);
		new Link(g1, l[5], null, null, null, null, null);
		
		new Link(l[0], l[1], null, null, null, null, Direction.DOWN);
		new Link(l[1], l[2], null, null, null, null, Direction.DOWN);
		new Link(l[2], l[3], null, null, null, null, Direction.DOWN);
		new Link(l[3], l[4], null, null, null, null, Direction.DOWN);
		new Link(l[4], l[5], null, null, null, null, Direction.DOWN);
		
		DiagramXMLElement d1 = new DiagramXMLElement(out, null);
		renderDiagram(d1);
		
		
	}
	
	
	@Test
	public void test_31_5_LeftFanFixedFirst() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<XMLElement> out = new ArrayList<XMLElement>();
		out.add(g1);
		Glyph[] l = GraphConstructionTools.createX("fan", 6, out);
		
		new Link(g1, l[0], null, null, null, null, Direction.LEFT);
		new Link(g1, l[1], null, null, null, null, null);
		new Link(g1, l[2], null, null, null, null, null);
		new Link(g1, l[3], null, null, null, null, null);
		new Link(g1, l[4], null, null, null, null, null);
		new Link(g1, l[5], null, null, null, null, null);
		
		new Link(l[0], l[1], null, null, null, null, Direction.DOWN);
		new Link(l[1], l[2], null, null, null, null, Direction.DOWN);
		new Link(l[2], l[3], null, null, null, null, Direction.DOWN);
		new Link(l[3], l[4], null, null, null, null, Direction.DOWN);
		new Link(l[4], l[5], null, null, null, null, Direction.DOWN);
		
		DiagramXMLElement d1 = new DiagramXMLElement(out, null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_31_6_DownFanFixedMiddles() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<XMLElement> out = new ArrayList<XMLElement>();
		out.add(g1);
		Glyph[] l = GraphConstructionTools.createX("fan", 8, out);
		
		new Link(g1, l[0], null, null, null, null, null);
		new Link(g1, l[1], null, null, null, null, null);
		new Link(g1, l[2], null, null, null, null, Direction.DOWN);
		new Link(g1, l[3], null, null, null, null, Direction.DOWN);
		new Link(g1, l[4], null, null, null, null, null);
		new Link(g1, l[5], null, null, null, null, null);
		new Link(g1, l[6], null, null, null, null, null);
		new Link(g1, l[7], null, null, null, null, null);
		
		new Link(l[0], l[1], null, null, null, null, Direction.RIGHT);
		new Link(l[1], l[2], null, null, null, null, Direction.RIGHT);
		new Link(l[2], l[3], null, null, null, null, Direction.RIGHT);
		new Link(l[3], l[4], null, null, null, null, Direction.RIGHT);
		new Link(l[4], l[5], null, null, null, null, Direction.RIGHT);
		new Link(l[5], l[6], null, null, null, null, Direction.RIGHT);
		new Link(l[6], l[7], null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement d1 = new DiagramXMLElement(out, null);
		renderDiagram(d1);
		
		
	}
	
	
	@Test
	public void test_31_7_NoStepFan() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1 has a very long label so we don't need any fans blah blah blah", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		
		Context c1 = new Context("c1", HelpMethods.listOf(g2, g3, g4, g5), true, null, null);

		
		new Link(g1, g2, null, null, null, null, Direction.UP);
		new Link(g1, g3, null, null, null, null, Direction.UP);
		new Link(g1, g4, null, null, null, null, Direction.UP);
		new Link(g1, g5, null, null, null, null, Direction.UP);
	
		
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(g1, c1), null);
		renderDiagram(d1);
	}
	
	@Test
	public void test_31_8_NoFanTurn() throws IOException {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		
		Context c1 = new Context("c1", HelpMethods.listOf(g2, g3, g1, g4), true, null, Layout.RIGHT);

		
		new Link(g1, g2, null, null, null, null, null);
		new Link(g1, g2, null, null, null, null, null);
		new Link(g1, g3, null, null, null, null, null);
		new Link(g1, g3, null, null, null, null, null);
		new Link(g1, g4, null, null, null, null, null);
		new Link(g1, g4, null, null, null, null, null);
		new Link(g1, g5, null, null, null, null, null);
	
		
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(c1, g5), null);
		d1.setLayoutDirection(Layout.RIGHT);
		renderDiagram(d1);
	}
	
	@Test
	public void test_31_9_UnnecessaryDogleg() throws IOException {
		generate("dogleg.xml");
	}
	
	
}
