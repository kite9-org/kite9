package org.kite9.diagram.functional.layout;

import java.util.Collections;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.dom.elements.DiagramKite9XMLElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.common.HelpMethods;

public class Test35ContainerAndVertexLinking extends AbstractLayoutFunctionalTest  {

	
	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Test
	public void test_35_1_ContentsAndContainerLinked() throws Exception {
		generate("edge_route_problem.xml");
	}
	
	@Test
	public void test_35_2_ForceContentLink() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		
		Context con1 = new Context("con1", HelpMethods.listOf(g1, g2, g3), true, new TextLine("c1"), Layout.DOWN);
		new Link(con1, g4);
		new Link(con1, g5);
		new Link(g1, g4);
		new Link(g2, g4);
		new Link(g3, g5);
		new Link(g6, g1);
		new Link(g6, g2);
		new Link(g6, g3);
		new Link(g6, con1);
		new Link(g0, con1);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList( g0, con1, g4, g5, g6), Layout.RIGHT, null);
		renderDiagram(d);
	}
	
	@Test
	public void test_35_3_NestedContextLink() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		
		Context con1 = new Context("inside", HelpMethods.listOf(g1, g2, g3), true, new TextLine("inside"), Layout.DOWN);
		Context con2 = new Context("outside", HelpMethods.listOf(con1, g4, g5), true, new TextLine("outside"), Layout.DOWN);
		
		new Link(con1, g4);
		new Link(con1, g5);
		new Link(g1, g4);
		new Link(g2, g4);
		new Link(g3, g5);
		new Link(g6, g1);
		new Link(g6, g2);
		new Link(g6, g3);
		new Link(g6, con1);
		new Link(g0, con1);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(g0, con2, g6), Layout.RIGHT, null);
		renderDiagram(d);
	}
	
	@Test
	public void test_35_4_TwoContextLink() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		Glyph g7 = new Glyph("g7", "", "g7", null, null);	
		
		Context con1 = new Context("con1", HelpMethods.listOf(g1, g2, g3), true, new TextLine("c1"), Layout.DOWN);
		Context con2 = new Context("con2", HelpMethods.listOf(g4, g5, g6), true, new TextLine("c2"), Layout.DOWN);
		
		new Link(con1, g0);
		new Link(con2, g0);
		new Link(con2, g7);
		new Link(con1, g7);
		
		new Link(g1, g7);
		new Link(g2, g7);
		new Link(g3, g7);
		new Link(g4, g0);
		new Link(g5, g0);
		new Link(g6, g0);
		Link l = new Link(con1, con2);
		l.setAttribute("class", "dotted");
		new Link(con2, con1);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(g0, con1, con2, g7), Layout.HORIZONTAL, null);
		renderDiagram(d);
	}
	
	@Test
	public void test_35_5_TwoContextLink2() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		Glyph g7 = new Glyph("g7", "", "g7", null, null);	
		
		Context con1 = new Context("con1", HelpMethods.listOf(g1, g2, g3), true, new TextLine("c1"), Layout.DOWN);
		Context con2 = new Context("con2", HelpMethods.listOf(g4, g5, g6), true, new TextLine("c2"), Layout.DOWN);
		
		new Link(con1, g0);
		new Link(con2, g0);
		new Link(con2, g7);
		new Link(con1, g7);
		
		new Link(g1, g7);
		new Link(g2, g7);
		new Link(g3, g7);
		new Link(g4, g0);
		new Link(g5, g0);
		new Link(g6, g0);
		Link l = new Link(con1, con2);
		l.setAttribute("class", "dotted");
		new Link(con2, con1);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList( g0, con1, g7, con2), Layout.RIGHT, null);
		renderDiagram(d);
	}

	@Test
	public void test_35_6_TwoContextLink3() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		Glyph g7 = new Glyph("g7", "", "g7", null, null);	
		
		Context con1 = new Context("con1", HelpMethods.listOf(g1, g2, g3), true, new TextLine("c1"), Layout.RIGHT);
		Context con2 = new Context("con2", HelpMethods.listOf(g4, g5, g6), true, new TextLine("c2"), Layout.RIGHT);
		
		new Link(con1, g0);
		new Link(con2, g0);
		new Link(con2, g7);
		new Link(con1, g7);
		
		new Link(g1, g7);
		new Link(g2, g7);
		new Link(g3, g7);
		new Link(g4, g0);
		new Link(g5, g0);
		new Link(g6, g0);
		Link l = new Link(con1, con2);
		l.setAttribute("class", "dotted");
		new Link(con2, con1);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(con1, g0, g7, con2), Layout.DOWN, null);
		renderDiagram(d);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_35_7_OctopusContext() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		Glyph g7 = new Glyph("g7", "", "g7", null, null);	
		Glyph g8 = new Glyph("g8", "", "g8", null, null);	
		Glyph g9 = new Glyph("g9", "", "g9", null, null);	
		Glyph g10 = new Glyph("g10", "", "g10", null, null);	
		Glyph g11 = new Glyph("g11", "", "g11", null, null);	
		
		Context con1 = new Context("con1", Collections.EMPTY_LIST, true, new TextLine("c2"), null);
		
		new Link(con1, g0);
		new Link(con1, g1);
		new Link(con1, g2);
		new Link(con1, g3);
		new Link(con1, g4);
		new Link(con1, g5);
		new Link(con1, g6);
		new Link(con1, g7);
		new Link(con1, g8);
		new Link(con1, g9);
		new Link(con1, g10);
		new Link(con1, g11);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList( con1, g0, g1, g2, g3, g4, g5, g6, g7, g8, g9, g10, g11), null);
		renderDiagram(d);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_35_8_OctopusStraight() throws Exception {
		Glyph g0 = new Glyph("g0", "", "g0", null, null);
		Glyph g1 = new Glyph("g1", "", "g1", null, null);
		Glyph g2 = new Glyph("g2", "", "g2", null, null);
		Glyph g3 = new Glyph("g3", "", "g3", null, null);
		Glyph g4 = new Glyph("g4", "", "g4", null, null);
		Glyph g5 = new Glyph("g5", "", "g5", null, null);
		Glyph g6 = new Glyph("g6", "", "g6", null, null);
		Glyph g7 = new Glyph("g7", "", "g7", null, null);	
		Glyph g8 = new Glyph("g8", "", "g8", null, null);	
		Glyph g9 = new Glyph("g9", "", "g9", null, null);	
		Glyph g10 = new Glyph("g10", "", "g10", null, null);	
		Glyph g11 = new Glyph("g11", "", "g11", null, null);	
		
		Context con1 = new Context("con1", Collections.EMPTY_LIST, true, new TextLine("c2"), null);
		
		new Link(con1, g0, null, null, null, null, Direction.RIGHT);
		new Link(con1, g1, null, null, null, null, Direction.RIGHT);
		new Link(con1, g2, null, null, null, null, Direction.RIGHT);
		new Link(con1, g3, null, null, null, null, Direction.RIGHT);
		new Link(con1, g4, null, null, null, null, Direction.RIGHT);
		new Link(con1, g5, null, null, null, null, Direction.DOWN);
		new Link(con1, g6, null, null, null, null, Direction.DOWN);
		new Link(con1, g7, null, null, null, null, Direction.DOWN);
		new Link(con1, g8, null, null, null, null, Direction.DOWN);
		new Link(con1, g9, null, null, null, null, Direction.UP);
		new Link(con1, g10, null, null, null, null, Direction.UP);
		new Link(con1, g11, null, null, null, null, Direction.UP);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("D", createList(con1, g0, g1, g2, g3, g4, g5, g6, g7, g8, g9, g10, g11), null);
		renderDiagram(d);
	}

}
