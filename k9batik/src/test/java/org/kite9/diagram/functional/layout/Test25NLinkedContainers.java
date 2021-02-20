package org.kite9.diagram.functional.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.GraphConstructionTools;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9Log.Destination;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Test25NLinkedContainers extends AbstractLayoutFunctionalTest {

	
	
	public void testGrid(int glyphs, int containers, Layout withinContainer, Layout overallL, Direction d) throws Exception {
		Random r = new Random(101);
		List<Kite9XMLElement> contents = new ArrayList<Kite9XMLElement>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", glyphs, containers, contents, withinContainer);
		
		for (int i = 0; i < glyphs; i++) {
			for (int j = 0; j < containers-1; j++) {
				new Link(out[j][i],out[j+1][i], null, null, LinkEndStyle.ARROW, null, d);
			}
		}
		
		for (Kite9XMLElement c : contents) {
			shuffleElements(r, c);
		}
		
		Context overall = new Context("co", contents, true, null, overallL);
		List<Kite9XMLElement> out2 = new ArrayList<Kite9XMLElement>();
		out2.add(overall);
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));
	}

	private void shuffleElements(Random r, Kite9XMLElement c) {
		NodeList l = c.getChildNodes();
		List<Node> items = new ArrayList<>(l.getLength());
		for (int i = 0; i < l.getLength(); i++) {
			items.add(l.item(i));
			c.removeChild(l.item(i));
		}
		
		Collections.shuffle(items, r);
		
		for (Node node : items) {
			c.appendChild(node);
		}
	}
	
	@Test
	public void test_25_1_6Glyphs3LinkedContainersHVNull() throws Exception {
		testGrid(6, 3, Layout.HORIZONTAL, Layout.VERTICAL, null);
	}
	

	@Test
	public void test_25_2_6Glyphs3LinkedContainersVHNull() throws Exception {
		testGrid(6, 3, Layout.VERTICAL, Layout.HORIZONTAL, null);
	}
	
	@Test
	public void test_25_3_3Glyphs3LinkedContainersHVDown() throws Exception {
		testGrid(3, 3, Layout.HORIZONTAL, Layout.VERTICAL, Direction.DOWN);
	}

	@Test
	public void test_25_4_6Glyphs3LinkedContainersVHRight() throws Exception {
		testGrid(6, 3, Layout.VERTICAL, Layout.HORIZONTAL, Direction.RIGHT);
	}
	
	@Test
	public void test_25_5_TwoFourLinkedContainersV() throws Exception {
		testGrid(2, 3, Layout.HORIZONTAL, null, null);		
	}
	
	@Test
	public void test_25_6_TwoSevenLinkedContainers() throws Exception {
		testGrid(2, 15, Layout.HORIZONTAL, null, null);		
	}
	
	@Test
	public void test_25_7_10Glyphs3LinkedContainersHVNull() throws Exception {
		testGrid(10, 3, Layout.HORIZONTAL, Layout.VERTICAL, null);
	}
	
	@Test
	public void test_25_8_6Glyphs3LinkedContainersNUllNUllRight() throws Exception {
		testGrid(6, 3, null, null, Direction.RIGHT);
	}
	
	@Test
	public void test_25_9_TwoFourLinkedContainersH() throws Exception {
		testGrid(2, 3, Layout.VERTICAL, null, null);		
	}
	
	@Test
	public void test_25_10_30Glyphs3LinkedContainersPartialRightHV() throws Exception {
		Kite9LogImpl.setLogging(Destination.OFF);
		Random r = new Random(101);
		int n = 30;
		List<Kite9XMLElement> contents = new ArrayList<Kite9XMLElement>();
		Glyph[][] out = GraphConstructionTools.createXContainers("g", n, 3, contents, Layout.HORIZONTAL);
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 2; j++) {
				new Link(out[j][i],out[j+1][i], null, null, LinkEndStyle.ARROW, null, null);
			}
		}
		
		((Context)contents.get(0)).setLayoutDirection(Layout.RIGHT);
		
		for (Kite9XMLElement c : contents) {
			shuffleElements(r, c);
		}
		
		Context overall = new Context("co", contents, true, null, Layout.VERTICAL);
		List<Kite9XMLElement> out2 = new ArrayList<Kite9XMLElement>();
		out2.add(overall);
		
		renderDiagram(new DiagramKite9XMLElement(out2, null));
	}
}
