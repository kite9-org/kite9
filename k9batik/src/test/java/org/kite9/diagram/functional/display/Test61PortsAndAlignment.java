package org.kite9.diagram.functional.display;

import java.util.Arrays;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.*;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.common.HelpMethods;
import org.kite9.diagram.model.position.Turn;
import org.kite9.diagram.model.style.BorderTraversal;
import org.w3c.dom.Element;

public class Test61PortsAndAlignment extends AbstractDisplayFunctionalTest {


	@Test
	public void test_61_1_PortPlacement() throws Exception {
		Glyph one = createGlyph("One");
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
		Glyph one = createGlyph("One");
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "130px");
		one.appendChild(oneSocket);

		Glyph two = createGlyph("Two");
		two.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket twoSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.LEFT, "10px");
		two.appendChild(twoSocket);
		Link l4 = new Link(oneSocket, twoSocket);
		//Link l3 = new Link(one, twoSocket);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement("dia", HelpMethods.listOf(two, one), Layout.DOWN, null);
		renderDiagram(d);
	}

	@Test
	public void test_61_3_MultipleUndirectedLinksToPort() throws Exception {
		Glyph one = createGlyph("One");
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "30px");
		one.appendChild(oneSocket);

		Glyph two = createGlyph("Two");
		Glyph three = createGlyph("Three");
		Link l4 = new Link(oneSocket, two);
		Link l3 = new Link(oneSocket, three, null, null, null, null, null);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement( HelpMethods.listOf(one, two, three), null);
		renderDiagram(d);
	}

	@Test
	public void test_61_8_MultipleUndirectedLinksToPort2() throws Exception {
		Glyph one = createGlyph("One");
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "30px");
		one.appendChild(oneSocket);

		Glyph two = createGlyph("Two");
		Glyph three = createGlyph("Three");
		Glyph four = createGlyph("Four");
		Glyph five = createGlyph("Five");

		Context i1 = new Context("i1", Arrays.asList( two, three, four, five ), true, null, Layout.DOWN);

		Link l4 = new Link(oneSocket, two);
		Link l3 = new Link(oneSocket, three, null, null, null, null, null);
		Link l5 = new Link(oneSocket, four);
		Link l6 = new Link(one, five);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement( HelpMethods.listOf(one, two, three), null);
		renderDiagram(d);
	}

	@Test
	public void test61_8_PortFanningWithLabels() throws Exception{

	}

	@Test
	public void test_61_4_ComplexArrivalSides() throws Exception {

	}

	@Test
	public void test_61_5_OffsetPortsAndMiddles() throws Exception {
		Glyph one = createGlyph("One");
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "30px");
		one.appendChild(oneSocket);
		BasicSocket twoSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "130px");
		one.appendChild(twoSocket);
		BasicSocket threeSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.LEFT, "75%");
		one.appendChild(threeSocket);

		Glyph two = createGlyph("Two");
		Glyph three = createGlyph("Three");
		Glyph four = createGlyph("Four");

		Link l4 = new Link(oneSocket, two);
		Link l3 = new Link(three, twoSocket);
		Link l5 = new Link(four, threeSocket);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement( HelpMethods.listOf(two, one, three, four), null);
		renderDiagram(d);
	}

	@Test
	public void test_61_6_Alignments() throws Exception {
		String alignmentStyle = CSSConstants.CONNECTION_ALIGN_LEFT_PROPERTY+ ": -5px; "+
				CSSConstants.CONNECTION_ALIGN_RIGHT_PROPERTY+": -5px; "+
				CSSConstants.CONNECTION_ALIGN_BOTTOM_PROPERTY+": 10px; "+
				CSSConstants.LINK_INSET+": 0px; ";
		Glyph one = createStyledGlyph("One", alignmentStyle);
		Glyph two = createStyledGlyph("Two", alignmentStyle);
		Glyph three = createStyledGlyph("Three", alignmentStyle);
		Glyph four = createStyledGlyph("Four", alignmentStyle);

		Link l4 = new Link(one, two);
		Link l3 = new Link(one, three, null, null, null, null, Direction.RIGHT);
		Link l5 = new Link(one, four);
		TurnLink l6 = new TurnLink(two, four);

		DiagramKite9XMLElement d= new DiagramKite9XMLElement("dia", HelpMethods.listOf(two, one, three, four), null);
		renderDiagram(d);
	}

	@NotNull
	private Glyph createGlyph(String one) {
		return new Glyph("Stereo", one, null, null);
	}

	private Glyph createStyledGlyph(String name, String style) {
		Glyph g = createGlyph(name);
		g.setAttribute("style", style);
		return g;
	}


	@Test
	public void test_61_7_DirectedPortSideContradiction() throws Exception {
		Glyph one = createGlyph("One");
		one.setAttribute("style", CSSConstants.TRAVERSAL_PROPERTY+": "+ BorderTraversal.PREVENT+";");
		BasicSocket oneSocket = new BasicSocket(BasicSocket.createID(), BasicSocket.TESTING_DOCUMENT, CSSConstants.RIGHT, "50%");
		one.appendChild(oneSocket);

		Glyph two = createGlyph("Two");
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
