package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.visualization.display.style.sheets.Designer2012Stylesheet;

public class Test1BasicLinkage extends AbstractFunctionalTest {

	@Override
	protected boolean checkDiagramSize() {
		return true;
	}
	
	@Test
	public void test_1_1_GlyphFinal() throws IOException {
		Contained one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Diagram d = new Diagram("The Diagram", createList(one));

		renderDiagram(d);
	}

	@Test
	public void test_1_2_GlyphInContainerFinal() throws IOException {
		Contained one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Contained con = new Context("Context", createList(one), true, null, null);
		Diagram d = new Diagram("The Diagram", createList(con));
		renderDiagram(d);
	}

	@Test
	public void test_1_3_TwoGlyphsFinal() throws IOException {
		Contained one = new Glyph("Stereo", "One", null, null);
		Contained two = new Glyph("Stereo", "Two", null, null);
		Diagram d = new Diagram("The Diagram", createList(one, two));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_4_GlyphFinalDesignerStylesheet() throws IOException {
		Contained one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Diagram d = new Diagram("The Diagram", createList(one));

		renderDiagram(d);
	}

}
