package org.kite9.diagram.functional.display;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;

public class Test1ArrowGlyphContainer extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_1_1_GlyphFinal() throws IOException {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one));

		renderDiagram(d);
	}

	@Test
	public void test_1_2_GlyphInContainerFinal() throws IOException {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		XMLElement con = new Context("Context", createList(one), true, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}

	@Test
	public void test_1_3_TwoGlyphsFinal() throws IOException {
		XMLElement one = new Glyph("Stereo", "One", null, null);
		XMLElement two = new Glyph("Stereo", "Two", null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_4_GlyphFinalDesignerStylesheet() throws IOException {
		XMLElement one = new Glyph("one", "Stereo", "Rob's Glyph", null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one));

		renderDiagram(d);
	}
	
	@Test
	public void test_1_5_TwoArrowsFinal() throws IOException {
		XMLElement one = new Arrow("One");
		XMLElement two = new Arrow("Two");
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one, two));
		renderDiagram(d);
	}

	@Test
	public void test_1_6_TwoGlyphsInContainerFinal() throws IOException {
		XMLElement one = new Glyph("Stereo", "Rob's Glyph", null, null);
		XMLElement two = new Glyph("Stereo", "Two", null, null);
		XMLElement con = new Context("Context", createList(one, two), true, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_7_TwoArrowsInContainerFinal() throws IOException {
		XMLElement one = new Arrow("One");
		XMLElement two = new Arrow("Two");
		XMLElement con = new Context("Context", createList(one, two), true, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con));
		renderDiagram(d);
	}
	
	@Test
	public void test_1_8_EmptyGlyph() throws IOException {
		XMLElement one = new Glyph(null, null, null, null);
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(one));

		renderDiagram(d);
	}
}
