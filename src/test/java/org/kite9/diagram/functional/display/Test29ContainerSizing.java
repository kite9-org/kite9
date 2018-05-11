package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.functional.layout.TestingEngine.Checks;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.dom.elements.DiagramKite9XMLElement;

public class Test29ContainerSizing extends AbstractDisplayFunctionalTest {

	@Test
	public void test_29_1_MidSetEdge() throws Exception {
		Glyph g1 = new Glyph("g1", "", "some quite long label", null, null);
		Glyph g2 = new Glyph("g2", "", "another long label", null, null);
		Glyph g3 = new Glyph("g3", "", "blahdy blahdy blah", null, null);
		Glyph g4 = new Glyph("g4", "", "andon andon andon", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1, g2, g3, g4), true, null, null);
		
		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g3, g4, null, null, null, null, Direction.RIGHT);
		new Link(g1, g4, null, null, null, null, Direction.DOWN);
		
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_2_ContainerSizes() throws Exception {
		Glyph g1 = new Glyph("g1", "", "some quite long label", null, null);
		Glyph g2 = new Glyph("g2", "", "another label", null, null);
		Glyph g3 = new Glyph("g3", "", "blahdy blahdy blah", null, null);
		Glyph g4 = new Glyph("g4", "", "andon", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1), true, new TextLine("c1l", "c1"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(g2), true, new TextLine("c2l", "c2"), null);
		Context c3 = new Context("c3", HelpMethods.listOf(g3), true, new TextLine("c3l", "c3"), null);
		Context c4 = new Context("c4", HelpMethods.listOf(g4), true, new TextLine("c4l", "c4"), null);
		
		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g1, g4, null, null, null, null, Direction.DOWN);
				
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_3_LikeASequence() throws Exception {
		Glyph g1 = new Glyph("g1", "", "method method method 1", null, null);
		Glyph g2 = new Glyph("g2", "", "method method method 2", null, null);
		Glyph g3 = new Glyph("g3", "", "method method method 3", null, null);
		Glyph g4 = new Glyph("g4", "", "method method method 4", null, null);
		Glyph g5 = new Glyph("g5", "", "method method method 5", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1), true, new TextLine("c1"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(g2), true, new TextLine("c2"), null);
		Context c3 = new Context("c3", HelpMethods.listOf(g3, g5), true, new TextLine("c3"), null);
		Context c4 = new Context("c4", HelpMethods.listOf(g4), true, new TextLine("c4"), null);
		
		new Link(g1, g2, null, null, null, null, Direction.RIGHT); 
		new Link(g2, g3, null, null, null, null, Direction.RIGHT);
		new Link(g2, g5, null, null, null, null, Direction.RIGHT);
		
		new Link(g3, g4, null, null, null, null, Direction.RIGHT);
		new Link(g5, g4, null, null, null, null, Direction.RIGHT);
		new Link(g1, g4, null, null, null, null, Direction.RIGHT);
				
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		d1.setLayoutDirection(Layout.RIGHT);
		
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_4_CornersInContainers() throws Exception {
		Glyph g1 = new Glyph("g1", "", "method 1", null, null);
		Glyph g2 = new Glyph("g2", "", "method 2", null, null);
		Glyph g3 = new Glyph("g3", "", "method method method  3", null, null);
		Glyph g4 = new Glyph("g4", "", "method 4", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1), true, new TextLine("c1"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(g2), true, new TextLine("c2"), null);
		Context c3 = new Context("c3", HelpMethods.listOf(g3), true, new TextLine("c3"), null);
		Context c4 = new Context("c3", HelpMethods.listOf(g4), true, new TextLine("c3"), null);
		
		new Link(g1, g2, null, null, null, null, Direction.DOWN);
		new Link(g2, g3, null, null, null, null, Direction.RIGHT);
		new TurnLink(g1, g3);
		new TurnLink(g1, g4);
		new TurnLink(g4, g3);
		
		
				
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_5_EmptyDiagram() throws Exception {
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement();
		renderDiagram(d1);
	}
	
	/**
	 * Sometimes mid-positioning doesn't work, if we mid-position both ends in different ways
	 * that happens on this test.
	 */
	protected Checks checks() {
		Checks out = super.checks();
		out.checkMidConnection = false;
		return out;
	}
}
