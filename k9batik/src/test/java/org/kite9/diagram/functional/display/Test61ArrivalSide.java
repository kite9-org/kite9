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
import org.kite9.diagram.model.style.BorderTraversal;
import org.w3c.dom.Element;

public class Test61ArrivalSide extends AbstractDisplayFunctionalTest {


	@Test
	public void test_61_1_PortPlacement() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		addSixPorts(one);

		Context i1 = new Context("i1", Arrays.asList( one ), true, null, Layout.DOWN);
		addSixPorts(i1);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement(HelpMethods.listOf(i1), null);
		renderDiagram(d);
	}

	private void addSixPorts(Element e) {
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.TOP, "10px"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.TOP, "-10px"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.BOTTOM, "10%"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.BOTTOM, "90%"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.LEFT, "50%"));
		e.appendChild(new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "50%"));
	}

	@Test
	public void test_61_2_SimpleLinkToPort() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "130px");
		one.appendChild(oneSocket);

		Glyph two = new Glyph("Stereo", "Two", null, null);
		two.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket twoSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.LEFT, "10px");
		two.appendChild(twoSocket);
		Link l4 = new Link(oneSocket, twoSocket);
		//Link l3 = new Link(one, twoSocket);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement("dia", HelpMethods.listOf(two, one), Layout.DOWN, null);
		renderDiagram(d);
	}

	@Test
	public void test_61_3_MultipleLinksToPort() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "30px");
		one.appendChild(oneSocket);

		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph("Stereo", "Three", null, null);
		Glyph four = new Glyph("Stereo", "Four", null, null);

		Link l4 = new Link(oneSocket, two);
		Link l3 = new Link(oneSocket, three, null, null, null, null, Direction.RIGHT);
		Link l5 = new Link(oneSocket, four);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement( HelpMethods.listOf(two, one, three, four), null);
		renderDiagram(d);
	}

	@Test
	public void test_61_4_ComplexArrivalSides() throws Exception {

	}

	@Test
	public void test_61_5_OffsetPortsAndMiddles() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "30px");
		one.appendChild(oneSocket);
		BasicSocket twoSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "130px");
		one.appendChild(twoSocket);
		BasicSocket threeSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.LEFT, "75%");
		one.appendChild(threeSocket);

		Glyph two = new Glyph("Stereo", "Two", null, null);
		Glyph three = new Glyph("Stereo", "Three", null, null);
		Glyph four = new Glyph("Stereo", "Four", null, null);

		Link l4 = new Link(oneSocket, two);
		Link l3 = new Link(three, twoSocket);
		Link l5 = new Link(four, threeSocket);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement( HelpMethods.listOf(two, one, three, four), null);
		renderDiagram(d);
	}

	@Test
	public void test_61_6_DirectedPorts1() throws Exception {

	}

	@Test
	public void test_61_7_DirectedPortSideContradiction() throws Exception {
		Glyph one = new Glyph("Stereo", "One", null, null);
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "50%");
		one.appendChild(oneSocket);

		Glyph two = new Glyph("Stereo", "Two", null, null);
		ContradictingLink l4 = new ContradictingLink(oneSocket, two, null, null, null, null, Direction.LEFT);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement("dia", HelpMethods.listOf(two, one), Layout.RIGHT, null);
		renderDiagram(d);
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
