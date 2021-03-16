package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.functional.TestingEngine.ElementsMissingException;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.common.HelpMethods;

public class Test34EmptyArrows extends AbstractDisplayFunctionalTest  {

	@Test
	public void test_34_1_3WayPointArrow() throws Exception {
		LinkBody a = new LinkBody("a", null);
		Glyph ga = new Glyph("g1","The 1", null, null);
		Glyph gb = new Glyph("g2","The 2", null, null);
		Glyph gc = new Glyph("g3","The 3", null, null);
		new Link("a-2", a, gb);
		new Link("a-1", a, ga);
		new Link("a-3", a, gc);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(a, ga, gb, gc), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_2_4WayPointArrow() throws Exception {
		LinkBody a = new LinkBody("a", "a");
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		Glyph gd = new Glyph("gd","The d", null, null);
		new Link(a, gb);
		new Link(a, ga);
		new Link(a, gc);
		new Link(a, gd);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(a, ga, gb, gc, gd), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_3_5WayPointArrow() throws Exception {
		LinkBody a = new LinkBody("middle", "");
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		Glyph gd = new Glyph("gd","The d", null, null);
		Glyph ge = new Glyph("ge","The e", null, null);
		new TurnLink(a, gb);
		new TurnLink(a, ga);
		new TurnLink(a, gc);
		new TurnLink(a, gd);
		new TurnLink(a, ge);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(a, ga, gb, gc, gd, ge), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_4_3WayNonPointArrow() throws Exception {
		LinkBody a = new LinkBody("middle", "middle");
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		new Link(a, gb);
		new Link(a, ga, null, null, null, null, Direction.RIGHT);
		new Link(a, gc, null, null, null, null, Direction.RIGHT);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(a, ga, gb, gc), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_5_3WayPointArrowWithBuffers() throws Exception {
		LinkBody a = new LinkBody("a", null);
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		new Link(a, gb);
		new Link(a, ga);
		new Link(a, gc);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(a, ga, gb, gc), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_6_3WayPointArrowWithTerms() throws Exception {
		LinkBody a = new LinkBody("a", null);
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		new Link(a, gb, null, null, "ARROW", null, Direction.UP);
		new Link(a, ga,null, null, "ARROW", null, Direction.DOWN);
		new Link(a, gc,null, null, "ARROW", null);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement(HelpMethods.listOf(a, ga, gb, gc), null);
		renderDiagram(d);
	}
	
	@Test(expected=ElementsMissingException.class)
	public void test_34_7_1GlyphsOneEdge() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		new TurnLink(one, one);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one));

		renderDiagram(d);
	}
}
