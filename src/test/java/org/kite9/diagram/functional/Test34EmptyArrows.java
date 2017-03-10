package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.functional.TestingEngine.Checks;
import org.kite9.diagram.functional.TestingEngine.ElementsMissingException;
import org.kite9.diagram.functional.layout.AbstractLayoutFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.display.complete.ADLBasicCompleteDisplayer;
import org.kite9.diagram.visualization.format.png.BufferedImageRenderer;
import org.kite9.diagram.visualization.pipeline.full.BufferedImageProcessingPipeline;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.common.HelpMethods;

public class Test34EmptyArrows extends AbstractLayoutFunctionalTest  {

	@Test
	public void test_34_1_3WayPointArrow() throws IOException {
		Arrow a = new Arrow("a", null);
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		new Link(a, gb);
		new Link(a, ga);
		new Link(a, gc);
		
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(a, ga, gb, gc), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_2_4WayPointArrow() throws IOException {
		Arrow a = new Arrow("a", null);
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		Glyph gd = new Glyph("gd","The d", null, null);
		new Link(a, gb);
		new Link(a, ga);
		new Link(a, gc);
		new Link(a, gd);
		
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(a, ga, gb, gc, gd), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_3_5WayPointArrow() throws IOException {
		Arrow a = new Arrow("middle", "");
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		Glyph gd = new Glyph("gd","The d", null, null);
		Glyph ge = new Glyph("gd","The d", null, null);
		new TurnLink(a, gb);
		new TurnLink(a, ga);
		new TurnLink(a, gc);
		new TurnLink(a, gd);
		new TurnLink(a, ge);
		
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(a, ga, gb, gc, gd, ge), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_4_3WayNonPointArrow() throws IOException {
		Arrow a = new Arrow("middle", "");
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		new Link(a, gb);
		new Link(a, ga, null, null, null, null, Direction.RIGHT);
		new Link(a, gc, null, null, null, null, Direction.RIGHT);
		
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(a, ga, gb, gc), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_34_5_3WayPointArrowWithBuffers() throws IOException {
		Arrow a = new Arrow("a", null);
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		new Link(a, gb);
		new Link(a, ga);
		new Link(a, gc);
		
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(a, ga, gb, gc), null);
		TestingEngine te = new TestingEngine(getZipName(), false) {

			@Override
			public BufferedImageProcessingPipeline getPipeline(Class<?> test, String subtest, boolean watermark) {
				return new BufferedImageProcessingPipeline(new ADLBasicCompleteDisplayer(watermark, true), new BufferedImageRenderer());
			}
			
			
		};
		
		te.renderDiagram(d, true, new Checks());
	}
	
	@Test
	public void test_34_6_3WayPointArrowWithTerms() throws IOException {
		Arrow a = new Arrow("a", null);
		Glyph ga = new Glyph("ga","The a", null, null);
		Glyph gb = new Glyph("gb","The b", null, null);
		Glyph gc = new Glyph("gc","The c", null, null);
		new Link(a, gb, null, null, "ARROW", null, Direction.UP);
		new Link(a, ga,null, null, "ARROW", null, Direction.DOWN);
		new Link(a, gc,null, null, "ARROW", null);
		
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(a, ga, gb, gc), null);
		renderDiagram(d);
	}
	
	@Test(expected=ElementsMissingException.class)
	public void test_34_7_1GlyphsOneEdge() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		new TurnLink(one, one);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one));

		renderDiagram(d);
	}
}
