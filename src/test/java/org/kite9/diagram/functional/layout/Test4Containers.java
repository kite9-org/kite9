package org.kite9.diagram.functional.layout;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.functional.AbstractLayoutFunctionalTest;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;

public class Test4Containers extends AbstractLayoutFunctionalTest {

	@Test
	public void test_4_1_ContainerNestingFinal() throws Exception {
		Glyph one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Context con1 = new Context("b1", createList(one), true, null, null);
		Context con2 = new Context("b2", createList(con1), true, null, null);
		Context con3 = new Context("b3", createList(con2), true, null, null);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con3), null);
		renderDiagram(d);
	}

	@Test
	public void test_4_2_HierarchicalContainers() throws Exception {
		Glyph one = new Glyph("Stereo", "one", null, null);
		Glyph two = new Glyph("Stereo", "two", null, null);
		Context con1 = new Context("b1", createList(one), true, null, null);
		Context con2 = new Context("b2", createList(two), true, null, null);
		Context con3 = new Context("b3", createList(con1, con2), true, null, null);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con3), null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_4_3_ContainerContentOrdering() throws Exception {
		Glyph one = new Glyph("top", "Stereo", "top", null, null);
		Glyph two = new Glyph("middle", "Stereo", "middle", null, null);
		Glyph three = new Glyph("bottom", "Stereo", "bottom", null, null);
		
		Context con1 = new Context("b1", createList(one, two, three), true, null, Layout.DOWN);
		
		Glyph four = new Glyph("left", "Stereo", "left", null, null);
		Glyph five = new Glyph("middle2", "Stereo", "middle2", null, null);
		Glyph six = new Glyph("right", "Stereo", "right", null, null);
		
		
		Context con2 = new Context("b2", createList(four, five, six), true, null, Layout.RIGHT);
	
		Context con3 = new Context("b3", createList(con1, con2), true, null, Layout.UP);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con3), null);
		renderDiagram(d);
	}

	@Test
	public void test_4_4_LabelledContainers() throws Exception {
		Glyph one = new Glyph("Stereo", "one", null, null);
		Context con1 = new Context("b1", createList(one), true, new TextLine("Here is my label"), null);

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con1), null);
		renderDiagram(d);
	}
	
	// top-to-bottom and left-to-right ordering

	// invisible containers
}
