package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.HelpMethods;

public class Test29ContainerSizing extends AbstractFunctionalTest {

	@Test
	public void test_29_1_MidSetEdge() throws IOException {
		Glyph g1 = new Glyph("g1", "", "some quite long label", null, null);
		Glyph g2 = new Glyph("g2", "", "another long label", null, null);
		Glyph g3 = new Glyph("g3", "", "blahdy blahdy blah", null, null);
		Glyph g4 = new Glyph("g4", "", "andon andon andon", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1, g2, g3, g4), true, null, null);
		
		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g3, g4, null, null, null, null, Direction.RIGHT);
		new Link(g1, g4, null, null, null, null, Direction.DOWN);
		
		
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(c1), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_2_ContainerSizes() throws IOException {
		Glyph g1 = new Glyph("g1", "", "some quite long label", null, null);
		Glyph g2 = new Glyph("g2", "", "another label", null, null);
		Glyph g3 = new Glyph("g3", "", "blahdy blahdy blah", null, null);
		Glyph g4 = new Glyph("g4", "", "andon", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1), true, new TextLine("c1"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(g2), true, new TextLine("c2"), null);
		Context c3 = new Context("c3", HelpMethods.listOf(g3), true, new TextLine("c3"), null);
		Context c4 = new Context("c4", HelpMethods.listOf(g4), true, new TextLine("c4"), null);
		
		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g1, g4, null, null, null, null, Direction.DOWN);
				
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_3_LikeASequence() throws IOException {
		Glyph g1 = new Glyph("g1", "", "method 1", null, null);
		Glyph g2 = new Glyph("g2", "", "method 2", null, null);
		Glyph g3 = new Glyph("g3", "", "method 3", null, null);
		Glyph g4 = new Glyph("g4", "", "method 4", null, null);
		Glyph g5 = new Glyph("g5", "", "method 5", null, null);
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
				
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		d1.setLayoutDirection(Layout.RIGHT);
		
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_4_CornersInContainers() throws IOException {
		Glyph g1 = new Glyph("g1", "", "method 1", null, null);
		Glyph g2 = new Glyph("g2", "", "method 2", null, null);
		Glyph g3 = new Glyph("g3", "", "method 3", null, null);
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
		
		
				
		DiagramXMLElement d1 = new DiagramXMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_5_EmptyDiagram() throws IOException {
		DiagramXMLElement d1 = new DiagramXMLElement();
		renderDiagram(d1);
	}
	
}
