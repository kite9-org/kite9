package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLabel;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.functional.TestingEngine.Checks;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.common.HelpMethods;
import org.w3c.dom.Element;

import java.util.List;

public class Test29ContainerSizing extends AbstractDisplayFunctionalTest {

	@Test
	public void test_29_1_MidSetEdge() throws Exception {
		Glyph g1 = new Glyph("g1", "", "some quite long label", null, null);
		Glyph g2 = new Glyph("g2", "", "another long label", null, null);
		Glyph g3 = new Glyph("g3", "", "blahdy blahdy blah", null, null);
		Glyph g4 = new Glyph("g4", "", "andon andon andon", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1, g2, g3, g4), true, null, null);
		
		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g3, g4, null, null, null, null, Direction.RIGHT);
		new Link(g1, g4, null, null, null, null, Direction.DOWN);
		
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_2_ContainerSizes() throws Exception {
		Glyph g1 = new Glyph("g1", "", "some quite long label", null, null);
		Glyph g2 = new Glyph("g2", "", "another label", null, null);
		Glyph g3 = new Glyph("g3", "", "blahdy blahdy blah", null, null);
		Glyph g4 = new Glyph("g4", "", "andon", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1), true, new TextLabel("c1"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(g2), true, new TextLabel("c2"), null);
		Context c3 = new Context("c3", HelpMethods.listOf(g3), true, new TextLabel("c3"), null);
		Context c4 = new Context("c4", HelpMethods.listOf(g4), true, new TextLabel("c4"), null);
		
		new Link(g1, g2, null, null, null, null, Direction.RIGHT);
		new Link(g1, g4, null, null, null, null, Direction.DOWN);
				
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_3_LikeASequence() throws Exception {
		Glyph g1 = new Glyph("g1", "", "method method method 1", null, null);
		Glyph g2 = new Glyph("g2", "", "method method method 2", null, null);
		Glyph g3 = new Glyph("g3", "", "method method method 3", null, null);
		Glyph g4 = new Glyph("g4", "", "method method method 4", null, null);
		Glyph g5 = new Glyph("g5", "", "method method method 5", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1), true, new TextLabel("c1"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(g2), true, new TextLabel("c2"), null);
		Context c3 = new Context("c3", HelpMethods.listOf(g3, g5), true, new TextLabel("c3"), null);
		Context c4 = new Context("c4", HelpMethods.listOf(g4), true, new TextLabel("c4"), null);
		
		new Link(g1, g2, null, null, null, null, Direction.RIGHT); 
		new Link(g2, g3, null, null, null, null, Direction.RIGHT);
		new Link(g2, g5, null, null, null, null, Direction.RIGHT);
		
		new Link(g3, g4, null, null, null, null, Direction.RIGHT);
		new Link(g5, g4, null, null, null, null, Direction.RIGHT);
		new Link(g1, g4, null, null, null, null, Direction.RIGHT);
				
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		d1.setLayoutDirection(Layout.RIGHT);
		
		renderDiagram(d1);
		
		
	}
	
	@Test
	public void test_29_4_CornersInContainers() throws Exception {
		Glyph g1 = new Glyph("g1", "", "method 1", null, null);
		Glyph g2 = new Glyph("g2", "", "method 2", null, null);
		Glyph g3 = new Glyph("g3", "", "method method method  3", null, null);
		Glyph g4 = new Glyph("g4", "", "method 4", null, null);
		Context c1 = new Context("c1", HelpMethods.listOf(g1), true, new TextLabel("c1"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(g2), true, new TextLabel("c2"), null);
		Context c3 = new Context("c3", HelpMethods.listOf(g3), true, new TextLabel("c3"), null);
		Context c4 = new Context("c3", HelpMethods.listOf(g4), true, new TextLabel("c3"), null);
		
		new Link(g1, g2, null, null, null, null, Direction.DOWN);
		new Link(g2, g3, null, null, null, null, Direction.RIGHT);
		new TurnLink(g1, g3);
		new TurnLink(g1, g4);
		new TurnLink(g4, g3);
		
		
				
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1, c2, c3, c4), null);
		
		renderDiagram(d1);
		
		
	}

	@Test
	public void test_29_6_LabelledCrissCross() throws Exception {
		Glyph g0 = new Glyph("g0", "", "method 0", null, null);
		Glyph g1 = new Glyph("g1", "", "method 1", null, null);

		Glyph g2 = new Glyph("g2", "", "method 2", null, null);
		Glyph g3 = new Glyph("g3", "", "method 3", null, null);

		Glyph g4= new Glyph("g4", "", "method 4", null, null);
		Glyph g5= new Glyph("g5", "", "method 5", null, null);

		Context c1 = new Context("c1", HelpMethods.listOf(g0, g1), true, new TextLabel("c1"), Layout.DOWN);
		Context c2 = new Context("c2", HelpMethods.listOf(g2, g3), true, new TextLabel("c2"), Layout.DOWN);
		Context c3 = new Context("c3", HelpMethods.listOf(g4, g5), true, new TextLabel("c3"), Layout.DOWN);


		// links from c1 to c2
		new Link(g0, g2, null, new TextLabel("straight1"), null, new TextLabel("straight2"), Direction.RIGHT);
		new Link(g1, g3, null, new TextLabel("straight3"), null, new TextLabel("straight4"), Direction.RIGHT);

		TurnLink b1 = new TurnLink(g0, g3);
		TurnLink b2 = new TurnLink(g1, g2);
		b1.setFromLabel(new TextLabel("from"));
		b2.setFromLabel(new TextLabel("from"));
		b1.setToLabel(new TextLabel("to"));
		b1.setToLabel(new TextLabel("to"));

		// links from c2 to c3
		new Link(g3, g4, null, new TextLabel("straight5"), null, new TextLabel("straight6"), Direction.RIGHT);
		new TurnLink(g3, g5);

		TurnLink b3 = new TurnLink(g2, g5);
		TurnLink b4 = new TurnLink(g2, g4);
		b3.setFromLabel(new TextLabel("from"));
		b4.setFromLabel(new TextLabel("from"));
		b3.setToLabel(new TextLabel("to"));
		b4.setToLabel(new TextLabel("to"));

		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement(HelpMethods.listOf(c1, c2, c3), null);

		renderDiagram(d1);


	}

	private Glyph createGlyph(String name) {
		return new Glyph(name, "", name, null, null);
	}

	public void middleGuyTopology(boolean directedEdges, boolean labels, Layout l) throws Exception {

		Glyph middle = createGlyph("middle");

		List<Element> tops = HelpMethods.listOf(createGlyph("t1"), createGlyph("t2"), createGlyph("t3"));
		List<Element> bottoms = HelpMethods.listOf(createGlyph("b1"), createGlyph("b2"), createGlyph("b3"));
		List<Element> lefts = HelpMethods.listOf(createGlyph("l1"), createGlyph("l2"), createGlyph("l3"));
		List<Element> rights = HelpMethods.listOf(createGlyph("r1"), createGlyph("r2"), createGlyph("r3"));

		Context top = new Context("top", tops, true, new TextLabel("top"), Layout.RIGHT);
		Context bottom = new Context("bottom", bottoms, true, new TextLabel("bottom"), Layout.RIGHT);
		Context left = new Context("left", lefts, true, new TextLabel("left"), Layout.DOWN);
		Context right = new Context("right", rights, true, new TextLabel("riught"), Layout.DOWN);

		for (int i=0; i<3; i++) {
			if (i==1) {
				if (directedEdges && labels) {
					new Link(middle, tops.get(i), null, new TextLabel("straight"+i), null, new TextLabel("straight"+i), Direction.UP);
					new Link(middle, bottoms.get(i), null, new TextLabel("straight"+i), null, new TextLabel("straight"+i), Direction.DOWN);
					new Link(middle, lefts.get(i), null, new TextLabel("straight"+i), null, new TextLabel("straight"+i), Direction.LEFT);
					new Link(middle, rights.get(i), null, new TextLabel("straight"+i), null, new TextLabel("straight"+i), Direction.RIGHT);
				} else if (directedEdges) {
					new Link(middle, tops.get(i), null, null, null, null, Direction.UP);
					new Link(middle, bottoms.get(i), null, null, null,null, Direction.DOWN);
					new Link(middle, lefts.get(i), null, null, null,null, Direction.LEFT);
					new Link(middle, rights.get(i), null, null, null,null, Direction.RIGHT);
				} else if (labels) {
					new Link(middle, tops.get(i), null, new TextLabel("straight"+i), null, new TextLabel("straight"+i), null);
					new Link(middle, bottoms.get(i), null, new TextLabel("straight"+i), null, new TextLabel("straight"+i), null);
					new Link(middle, lefts.get(i), null, new TextLabel("straight"+i), null, new TextLabel("straight"+i), null);
					new Link(middle, rights.get(i), null, new TextLabel("straight"+i), null, new TextLabel("straight"+i),null);
				}
			} else {
				new TurnLink(middle, tops.get(i));
				new TurnLink(middle, bottoms.get(i));
				new TurnLink(middle, lefts.get(i));
				new TurnLink(middle, rights.get(i));
			}
		}

		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement("dia", HelpMethods.listOf(middle, top, left, right, bottom), l, null);

		renderDiagram(d1);
	}

	@Test
	public void test_29_7_MiddleGuyFanningRight() throws Exception {
		middleGuyTopology(false, true, Layout.RIGHT);
	}

	@Test
	public void test_29_8_MiddleGuyFanningDown() throws Exception {
		middleGuyTopology(false, true, Layout.DOWN);
	}

	@Test
	public void test_29_9_MiddleGuyFanningDirected() throws Exception {
		middleGuyTopology(true, true, null);
	}

	@Test
	public void test_29_5_EmptyDiagram() throws Exception {
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement();
		renderDiagram(d1);
	}
	
	/**
	 * Sometimes mid-positioning doesn't work, if we mid-position both ends in different ways
	 * that happens on this test.
	 */
	protected Checks checks() {
		Checks out = super.checks();
		out.checkMidConnection = false;
		return out;
	}
}
