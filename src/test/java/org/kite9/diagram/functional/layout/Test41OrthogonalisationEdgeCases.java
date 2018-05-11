package org.kite9.diagram.functional.layout;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.dom.elements.DiagramKite9XMLElement;

@Ignore
public class Test41OrthogonalisationEdgeCases extends AbstractLayoutFunctionalTest {

	
	@Test
	public void test_41_1_UnnecessaryBend() throws Exception {
		Glyph top = new Glyph("top","", "top", null, null);
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b","",  "b", null, null);
		Glyph c = new Glyph("c","",  "c", null, null);
		
		Context ctx = new Context("middle", HelpMethods.listOf(a, b, c), true, null, Layout.HORIZONTAL);
		
		Glyph bottom = new Glyph("bottom", "", "bottom", null, null);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", HelpMethods.listOf(top, ctx, bottom), Layout.DOWN, null);
	
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
