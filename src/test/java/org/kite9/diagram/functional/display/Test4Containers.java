package org.kite9.diagram.functional.display;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLabel;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.LabelPlacement;

public class Test4Containers extends AbstractDisplayFunctionalTest {

	@Test
	public void test_4_1_ContainerNestingFinal() throws Exception {
		Glyph one = new Glyph("Stereo", "Rob's Glyph", null, null);
		Context con1 = new Context("b1", createList(one), true, null, null);
		Context con2 = new Context("b2", createList(con1), true, null, null);
		Context con3 = new Context("b3", createList(con2), true, null, null);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con3), null);
		renderDiagram(d);
	}

	@Test
	public void test_4_2_HierarchicalContainers() throws Exception {
		Glyph one = new Glyph("Stereo", "one", null, null);
		Glyph two = new Glyph("Stereo", "two", null, null);
		Context con1 = new Context("b1", createList(one), true, null, null);
		Context con2 = new Context("b2", createList(two), true, null, null);
		Context con3 = new Context("b3", createList(con1, con2), true, null, null);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con3), null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_4_3_ContainerContentOrdering() throws Exception {
		Glyph one = new Glyph("top", "Stereo", "top", null, null);
		Glyph two = new Glyph("middle", "Stereo", "middle", null, null);
		Glyph three = new Glyph("bottom", "Stereo", "bottom", null, null);
		
		Context con1 = new Context("b1", createList(one, two, three), true, null, Layout.DOWN);
		
		Glyph four = new Glyph("left", "Stereo", "left", null, null);
		Glyph five = new Glyph("middle2", "Stereo", "middle2", null, null);
		Glyph six = new Glyph("right", "Stereo", "right", null, null);
		
		
		Context con2 = new Context("b2", createList(four, five, six), true, null, Layout.RIGHT);
	
		Context con3 = new Context("b3", createList(con1, con2), true, null, Layout.UP);

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con3), null);
		renderDiagram(d);
	}

	@Test
	public void test_4_4_LabelledContainers() throws Exception {
		Glyph one = new Glyph("Stereo", "one", null, null);
		Glyph two = new Glyph("Stereo", "two", null, null);
		Glyph three = new Glyph("Stereo", "some annoyingly long glyph 3", null, null);
		Glyph four = new Glyph("Stereo", "some annoyingly long glyph 4", null, null);
		Glyph five = new Glyph("Stereo", "5", null, null);

		Context con1 = new Context("b1", 
			createList(one, 
					new TextLabel("1. Top Left", LabelPlacement.TOP_LEFT),
					new TextLabel("2. Top Right", LabelPlacement.TOP_RIGHT),
					new TextLabel("3. Bottom Left", LabelPlacement.BOTTOM_LEFT),
					new TextLabel("4. Bottom Right", LabelPlacement.BOTTOM_RIGHT)), true, null, null);
		
		TextLabel top = new TextLabel("5. Top", LabelPlacement.TOP);
		TextLabel right = new TextLabel("6. Right", LabelPlacement.RIGHT);
		TextLabel tall = new TextLabel("7. Tall", LabelPlacement.RIGHT);
		TextLabel down = new TextLabel("8. Bottom", LabelPlacement.BOTTOM);
		
		top.setAttribute("style", "kite9-horizontal-sizing: maximize; kite9-label-placement: top;");
		right.setAttribute("style", "kite9-horizontal-sizing: maximize; kite9-label-placement: right;");
		
		tall.setAttribute("style", "kite9-vertical-sizing: maximize; kite9-label-placement: right;");
		down.setAttribute("style", "kite9-horizontal-sizing: minimize; kite9-label-placement: bottom;");
		
		Context con2 = new Context("b2", 
				createList(two, 
						top,
						right,
						tall,
						down), true, null, null);

		
		Context con3 = new Context("b3", 
				createList(three, new TextLabel("9. Top \n1", LabelPlacement.TOP),
						new TextLabel("10. Top 2", LabelPlacement.TOP),
						new TextLabel("11. Left 1", LabelPlacement.LEFT),
						new TextLabel("12. Left \n2", LabelPlacement.LEFT)), true, null, null);
		
		TextLabel topl = new TextLabel("13. Top \n1", LabelPlacement.LEFT);
		topl.setAttribute("style", "kite9-vertical-sizing: maximize; kite9-horizontal-sizing: minimize; kite9-label-placement: top;");
		
		Context con4 = new Context("b4", 
				createList(four, topl), true, null, null);
		
		con4.setAttribute("style", "kite9-sizing: minimize;");
		
		TextLabel left = new TextLabel("14. Left Left \n1", LabelPlacement.LEFT);
		left.setAttribute("style", "kite9-vertical-sizing: maximize; kite9-horizontal-sizing: minimize; kite9-label-placement: left;");
		
		
		Context con5 = new Context("b5", 
				createList(five, left), true, null, null);
		
		con5.setAttribute("style", "kite9-sizing: minimize;");
		
		new Link(one, two);
		new Link(three, four);
		new Link(four, five);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(
				con1, 
				con2, 
				con3, 
				con4,
				con5
				), null);
		
		renderDiagram(d);
	}
	
	@Test
	public void test_4_5_TurnsInsideContainers() throws Exception {
		Glyph one = new Glyph("one", "Stereo", "one", null, null);
		Glyph two = new Glyph("two", "Stereo", "two", null, null);
		Glyph three = new Glyph("three", "Stereo", "three", null, null);
		Glyph four = new Glyph("four", "Stereo", "four", null, null);
		Glyph five = new Glyph("five", "Stereo", "five", null, null);
		
		Context con1 = new Context("b1", createList(one), true, null, Layout.RIGHT);
		Context con2 = new Context("b2", createList(two), true, null, Layout.RIGHT);

		new Link(one, three, null,null,null,null,Direction.DOWN);
		new Link(two, five, null,null,null,null,Direction.DOWN);
		new Link(three, four, null,null,null,null,Direction.RIGHT);
		new Link(four, five, null,null,null,null,Direction.RIGHT);
		new TurnLink(one, four);
		new TurnLink(two, four);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con1, con2, three, four, five), null);
		renderDiagram(d);
	}
	
	@Test
	@Ignore("Should be a test about where corners go")
	public void test_4_6_PushOutRectangularization() throws Exception {
		Glyph one = new Glyph("one", "Stereo", "one", null, null);
		Glyph two = new Glyph("two", "Stereo", "two", null, null);
		
		Context con1 = new Context("b1", createList(one), true, null, Layout.RIGHT);
		con1.setAttribute("style", "kite9-sizing: minimize; kite9-horizontal-sizing: minimize; kite9-label-placement: top;");
		
		new Link(one, two, null,null,null,null,Direction.DOWN);
	
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con1, two), null);
		renderDiagram(d);
	}
	

}
