package org.kite9.diagram.functional.display;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.*;
import org.kite9.diagram.common.HelpMethods;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Collections;

public class Test62PortContradictions extends AbstractDisplayFunctionalTest {


	@Test
	public void test_62_1_ComplexArrivalSides() throws Exception {
		Glyph one = createGlyph("One");
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.LEFT, "50%");
		one.appendChild(oneSocket);

		Glyph two = createGlyph("Two");
		Glyph three = createGlyph("Three");

		new ContradictingLink(oneSocket, two, null, null, null, null, Direction.DOWN);
		new ContradictingLink(oneSocket, three, null, null, null, null, Direction.UP);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement( HelpMethods.listOf(one, two, three), null);
		renderDiagram(d);
	}



	@NotNull
	private Glyph createGlyph(String one) {
		return new Glyph("Stereo", one, null, null);
	}

	@Test
	public void test_62_2_DirectedPortSideContradiction() throws Exception {
		Glyph one = createGlyph("One");
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "50%");
		one.appendChild(oneSocket);

		Glyph two = createGlyph("Two");
		ContradictingLink l4 = new ContradictingLink(oneSocket, two, null, null, null, null, Direction.LEFT);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement("dia", HelpMethods.listOf(two, one), Layout.RIGHT, null);
		renderDiagram(d);
	}

}
