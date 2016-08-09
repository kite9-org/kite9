package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.Context;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.diagram.xml.Link;
import org.kite9.framework.common.HelpMethods;

@Ignore
public class Test41OrthogonalisationEdgeCases extends AbstractFunctionalTest {

	
	@Test
	public void test_41_1_UnnecessaryBend() throws IOException {
		Glyph top = new Glyph("top","", "top", null, null);
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b","",  "b", null, null);
		Glyph c = new Glyph("c","",  "c", null, null);
		
		Context ctx = new Context("middle", HelpMethods.listOf(a, b, c), true, null, Layout.HORIZONTAL);
		
		Glyph bottom = new Glyph("bottom", "", "bottom", null, null);
		
		Diagram d = new Diagram("d", HelpMethods.listOf(top, ctx, bottom), Layout.DOWN, null);
	
		new TurnLink(top, a);
		new Link(top, b);
		new TurnLink(top, c);
		new TurnLink(bottom, a);
		new TurnLink(bottom, b);
		
		renderDiagram(d);
	}

	@Override
	protected boolean checkEverythingStraight() {
		return true;
	}
	
	
}
