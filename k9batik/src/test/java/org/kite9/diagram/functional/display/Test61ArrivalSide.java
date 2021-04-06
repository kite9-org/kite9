package org.kite9.diagram.functional.display;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.*;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.common.HelpMethods;
import org.w3c.dom.Element;

public class Test61ArrivalSide extends AbstractDisplayFunctionalTest {


	@Test
	public void test_61_1_PortPlacement() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		addEightPorts(one);

		Context i1 = new Context("i1", Arrays.asList( one ), true, null, Layout.DOWN);
		addEightPorts(i1);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement(HelpMethods.listOf(i1), null);
		renderDiagram(d);
	}

	private void addEightPorts(Element e) {
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.TOP, "10px"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.TOP, "-10px"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.BOTTOM, "10%"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.BOTTOM, "90%"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.LEFT, "50%"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "50%"));
	}

	@SuppressWarnings("unchecked")
	@Test
	@Ignore
    public void test_61_1_LinkedDifferentDirection() throws Exception {
    	Context i1 = new Context("i1", Collections.EMPTY_LIST, true, null, Layout.DOWN);
    	Context i2 = new Context("i2", Collections.EMPTY_LIST, true, null, Layout.DOWN);
    	Context i3 = new Context("i3", Collections.EMPTY_LIST, true, null, Layout.DOWN);
    	Context i4 = new Context("i4", Collections.EMPTY_LIST, true, null, Layout.DOWN);
    	
    	Context outer = new Context("outer", HelpMethods.listOf(i1, i2, i3, i4), true, null, null);
    	Link l1 = new Link(i2, i3, "ARROW", null, "ARROW", null, null);
    	Link l2 = new Link(i1, i2, null, null, null, null, Direction.RIGHT);
    	Link l3 = new Link(i2, i3, null, null, null, null, Direction.RIGHT);
    	Link l4 = new Link(i3, i4, null, null, null, null, Direction.RIGHT);
    	
    	
    	AbstractMutableXMLElement fromT = l1.getProperty("from");
    	fromT.setAttribute("style", "--kite9-arrival-side: up; ");
    	
    	AbstractMutableXMLElement toT = l1.getProperty("to");
    	toT.setAttribute("style", "--kite9-arrival-side: down; ");
    	
    	DiagramKite9XMLElement d= new DiagramKite9XMLElement(HelpMethods.listOf(outer), null);
    	renderDiagram(d);
    }
    

}
