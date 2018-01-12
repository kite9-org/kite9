package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.xml.DiagramKite9XMLElement;

public class Test57TextLinks extends AbstractDisplayFunctionalTest {
	
	@Test
	public void test_57_1_TextLineLink1() throws Exception {
		TextLine tl = new TextLine("linker", "some row");
		Glyph one = new Glyph("Stereo", "One", HelpMethods.listOf(tl), null);
		Arrow a = new Arrow("meets");
		new Link(a, tl, null, null, null, null, Direction.RIGHT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, a));

		renderDiagram(d);
	}
	
	@Test
	public void test_57_2_TextLineLink2() throws Exception {
		TextLine tl = new TextLine("linker", "some row");
		TextLine tl2 = new TextLine("linker2", "some other row");
		Glyph one = new Glyph("Stereo", "One", HelpMethods.listOf(tl, tl2), null);
		Arrow a = new Arrow("a");
		Arrow b = new Arrow("b");
		new Link(a, tl, null, null, null, null, Direction.LEFT);
		new Link(b, tl2, null, null, null, null, Direction.LEFT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, a, b));

		renderDiagram(d);
	}

}
