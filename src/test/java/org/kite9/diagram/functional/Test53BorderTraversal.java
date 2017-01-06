package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.HelpMethods;

public class Test53BorderTraversal extends AbstractFunctionalTest {


	@Test
	public void test_53_1_ContainerToContainerRight() throws IOException {
		Glyph g1 = new Glyph("one", "","one", null, null);
		Glyph g2 = new Glyph("two", "","two ", null, null);
		Glyph g3 = new Glyph("three", "","three ", null, null);
		Glyph g6 = new Glyph("six", "","six ", null, null);
		Context c5 = new Context("five", listOf(g6), true, null, null);
		c5.setStyle("traversal: always, none, none, none; ");
		
		new Link(g2, g6);

		renderDiagram(new DiagramXMLElement("diagram", Arrays.asList(g1, c5, g2, g3), Layout.RIGHT, null));
	}
	
	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}
}
