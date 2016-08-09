package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.xml.Context;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.diagram.xml.XMLElement;

public class Test1BasicLinkage extends AbstractFunctionalTest {

	@Override
	protected boolean checkDiagramSize() {
		return true;
	}
	
	@Test
	public void test_1_1_GlyphFinal() throws IOException {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Diagram d = new Diagram("The Diagram", createList(one));

		renderDiagram(d);
	}

	@Test
	public void test_1_2_GlyphInContainerFinal() throws IOException {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		XMLElement con = new Context("Context", createList(one), true, null, null);
		Diagram d = new Diagram("The Diagram", createList(con));
		renderDiagram(d);
	}

	@Test
	public void test_1_3_TwoGlyphsFinal() throws IOException {
		XMLElement one = new Glyph("Stereo", "One", null, null);
		XMLElement two = new Glyph("Stereo", "Two", null, null);
		Diagram d = new Diagram("The Diagram", createList(one, two));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_4_GlyphFinalDesignerStylesheet() throws IOException {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Diagram d = new Diagram("The Diagram", createList(one));

		renderDiagram(d);
	}

}
