package org.kite9.diagram.functional.display;

import java.net.URL;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;

public class Test58ScriptTag extends AbstractDisplayFunctionalTest {

	@Override
	public String getDesignerStylesheetReference() {
		URL u = this.getClass().getResource("/stylesheets/designer-with-script.css");
		return "<svg:defs><svg:style type=\"text/css\"> @import url(\""+u+"\");</svg:style></svg:defs>";
	}

	
	@Test
	//@Ignore("Scripting hasn't been finalized yet")
	public void test_58_1_SimpleGlyphPlusScript() throws Exception {
		Glyph one = new Glyph("", "Rob's Glyph", null, null);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one), null);
		renderDiagram(d);
	}
}
