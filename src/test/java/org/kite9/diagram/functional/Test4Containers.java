package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.Contained;

public class Test4Containers extends AbstractFunctionalTest {

	@Test
	public void test_4_1_ContainerNestingFinal() throws IOException {
		Contained one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Contained con1 = new Context("b1", createList(one), true, null, null);
		Contained con2 = new Context("b2", createList(con1), true, null, null);
		Contained con3 = new Context("b3", createList(con2), true, null, null);

		Diagram d = new Diagram("The Diagram", createList(con3), null);
		renderDiagram(d);
	}

	@Test
	public void test_4_2_HierarchicalContainers() throws IOException {
		Contained one = new Glyph("Stereo", "one", null, null);
		Contained two = new Glyph("Stereo", "two", null, null);
		Contained con1 = new Context("b1", createList(one), true, null, null);
		Contained con2 = new Context("b2", createList(two), true, null, null);
		Contained con3 = new Context("b3", createList(con1, con2), true, null, null);

		Diagram d = new Diagram("The Diagram", createList(con3), null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_4_3_ContainerContentOrdering() throws IOException {
		Glyph one = new Glyph("top", "Stereo", "top", null, null);
		Glyph two = new Glyph("middle", "Stereo", "middle", null, null);
		Glyph three = new Glyph("bottom", "Stereo", "bottom", null, null);
		
		Contained con1 = new Context("b1", createList((Contained) one, two, three), true, null, Layout.DOWN);
		
		Glyph four = new Glyph("left", "Stereo", "left", null, null);
		Glyph five = new Glyph("middle2", "Stereo", "middle2", null, null);
		Glyph six = new Glyph("right", "Stereo", "right", null, null);
		
		
		Contained con2 = new Context("b2", createList((Contained) four, five, six), true, null, Layout.RIGHT);
	
		Contained con3 = new Context("b3", createList(con1, con2), true, null, Layout.UP);

		Diagram d = new Diagram("The Diagram", createList(con3), null);
		renderDiagram(d);
	}

	// top-to-bottom and left-to-right ordering

	// container labels

	// invisible containers
}
