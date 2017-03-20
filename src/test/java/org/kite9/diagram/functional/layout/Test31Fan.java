package org.kite9.diagram.functional.layout;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.GraphConstructionTools;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.xml.DiagramKite9XMLElement;
import org.kite9.framework.xml.Kite9XMLElement;

public class Test31Fan extends AbstractLayoutFunctionalTest {

	
	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Test
	public void test_31_1_Basic2InABox() throws Exception {
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

		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(g1, c1, g6, g5, g7), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_31_2_Basic3InABox() throws Exception {
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
		
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(g1, c1, g5, g6), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_31_3_LotsInABox() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<Kite9XMLElement> out = new ArrayList<Kite9XMLElement>();
		out.add(g1);
		List<Kite9XMLElement> cc = new ArrayList<Kite9XMLElement>();
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
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(out, null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_31_4_RightFanFixedFirst() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<Kite9XMLElement> out = new ArrayList<Kite9XMLElement>();
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
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(out, null);
		renderDiagram(d1);
		
		
	}
	
	
	@Test
	public void test_31_5_LeftFanFixedFirst() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<Kite9XMLElement> out = new ArrayList<Kite9XMLElement>();
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
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(out, null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_31_6_DownFanFixedMiddles() throws Exception {
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		List<Kite9XMLElement> out = new ArrayList<Kite9XMLElement>();
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
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(out, null);
		renderDiagram(d1);
		
		
	}
	
	
	@Test
	public void test_31_7_NoStepFan() throws Exception {
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
	
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(g1, c1), null);
		renderDiagram(d1);
	}
	
	@Test
	public void test_31_8_NoFanTurn() throws Exception {
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
	
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1, g5), null);
		d1.setLayoutDirection(Layout.RIGHT);
		renderDiagram(d1);
	}
	
	@Test
	public void test_31_9_UnnecessaryDogleg() throws Exception {
		generate("dogleg.xml");
	}
	
	
}
