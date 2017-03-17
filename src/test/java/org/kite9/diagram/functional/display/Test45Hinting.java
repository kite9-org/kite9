package org.kite9.diagram.functional.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.DiagramAssert;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.xml.DiagramXMLElement;
import org.kite9.framework.xml.XMLElement;

import junit.framework.Assert;


@Ignore
public class Test45Hinting extends AbstractLayoutFunctionalTest {


	private void checkInVerticalLine(DiagramXMLElement d2) {
		DiagramElement up = getById("up", d2);
		DiagramElement mid = getById("mid", d2);
		DiagramElement down = getById("down", d2);
		DiagramAssert.assertInDirection(up, mid, Direction.DOWN);
		DiagramAssert.assertInDirection(mid, down, Direction.DOWN);
	}
	
	private void checkInOrdinalHorizontalLine(DiagramXMLElement d2) {
		DiagramElement up = getById("up", d2);
		DiagramElement mid = getById("mid", d2);
		DiagramElement down = getById("down", d2);
		DiagramElement next = getById("next", d2);
		DiagramAssert.assertInDirection(next, down, Direction.RIGHT);
		DiagramAssert.assertInDirection(down, mid, Direction.RIGHT);
		DiagramAssert.assertInDirection(mid, up, Direction.RIGHT);
	}
	
	private void checkInHorizontalLine(DiagramXMLElement d2) {
		XMLElement prev = null;
		for (XMLElement c : d2) {
			if (prev != null) {
				DiagramAssert.assertInDirection(prev.getDiagramElement(), c.getDiagramElement(), Direction.RIGHT);
			} 
			prev = c;
		}
	}
	
	private DiagramXMLElement create8Glyphs() {
		List<XMLElement> created = new ArrayList<XMLElement>();
		for (int i = 0; i < 8; i++) {
			created.add(new Glyph("glyph"+i, "", "G"+i, null, null));
		}
		
		return new DiagramXMLElement("d", created, Layout.HORIZONTAL, null);
	}

	private DiagramXMLElement create4Glyphs(boolean addLinks, boolean addHints) {
		Glyph up = new Glyph("up", null, "UP", null, null);
		Glyph mid = new Glyph("mid", null, "MID", null, null);
		Glyph down = new Glyph("down", null, "DOWN", null, null);
		
//		up.setPositioningHints(new HintMap());
//		mid.setPositioningHints(new HintMap());
//		down.setPositioningHints(new HintMap());
//		
//		if (addHints) {
//			PositioningHints.planFill(up.getPositioningHints(), 0f, .5f, 0f, .25f);
//			PositioningHints.planFill(mid.getPositioningHints(), 0f, .5f, .26f, .5f);
//			PositioningHints.planFill(down.getPositioningHints(), 0f, .5f, .51f, .99f);
//		}
		
		Glyph next = new Glyph("next", null, "NEXT", null, null);
		DiagramXMLElement d= new DiagramXMLElement(HelpMethods.listOf(next, down, mid, up), null);
		
		if (addLinks) {
			new Link(up, next, null, null, null, null, Direction.RIGHT);
			new Link(mid, next, null, null, null, null, Direction.RIGHT);
			new Link(down, next, null, null, null, null, Direction.RIGHT);
		}
		return d;
	}
	
	@Test
	public void test_45_1_UpDownPlanarizationHinting() throws Exception {
		DiagramXMLElement d = create4Glyphs(true, true);
		DiagramXMLElement d2 = renderDiagram(d);
		checkInVerticalLine(d2);
	}
	
	@Test
	public void test_45_2_UpDownPlanarizationHinting2() throws Exception {
		DiagramXMLElement d= create4Glyphs(false, true);
		DiagramXMLElement d2 = renderDiagram(d);
		checkInHorizontalLine(d2);
	}
	
	@Test
	public void test_45_3_OrdinalOrder() throws Exception {
		DiagramXMLElement d= create4Glyphs(false, false);
		List<DiagramElement> ordered1 = d.getDiagramElement().getContents();
		DiagramXMLElement d2 = renderDiagram(d);
		checkInOrdinalHorizontalLine(d2);
		List<DiagramElement> ordered2 = getInOrder(new ArrayList<DiagramElement>(d2.getDiagramElement().getContents()));
		Assert.assertEquals(ordered1.toString(), ordered2.toString());
	}
	
//	@Test
//	public void test_45_4_OrdinalOrderOverridesPositionHinting() throws Exception {
//		// check that diagram orders glyphs in the collection order
//		DiagramXMLElement d = create8Glyphs();
//		DiagramXMLElement d2 = renderDiagram(d);
//		checkInHorizontalLine(d2);
//		
//		// change the collection order, and check that it is preserved
//		Collections.swap(d2, 0, 5);
//		Collections.swap(d2, 3, 1);
//		Collections.swap(d2, 6, 2);
//		
//		List<DiagramElement> ordered2 = new ArrayList<DiagramElement>(d2.getDiagramElement().getContents());
//		DiagramXMLElement d3 = renderDiagram(d2);
//		List<DiagramElement> ordered3 = getInOrder(new ArrayList<DiagramElement>(d3.getContents()));
//		Assert.assertEquals(ordered2.toString(), ordered3.toString());
//	}
//	
//	@Test 
//	public void test_45_5_HintingChangesOrdinal() throws Exception {
//		Glyph a = new Glyph("a", "", "a", null, null);
//		Glyph b = new Glyph("b", "", "b", null, null);
//		Glyph c = new Glyph("c", "", "c", null, null);
//		
//		DiagramXMLElement d = new DiagramXMLElement("d", HelpMethods.listOf(a, b, c), Layout.VERTICAL, null);
//		new Link(a, b);
//		new Link(a, c);
//		DiagramXMLElement d2 = renderDiagram(d);
//		System.out.println(d2.getContents());
//		
//		// make sure the ordinal order has changed to reflect the layout.
//		Assert.assertEquals(HelpMethods.listOf(b, a, c).toString(), d2.getContents().toString());
//	}
	
	private List<DiagramElement> getInOrder(List<DiagramElement> contents) {
		Collections.sort(contents,
				new Comparator<DiagramElement>() {

			@Override
			public int compare(DiagramElement arg0, DiagramElement arg1) {
				return ((Double)getXPos(arg0)).compareTo(getXPos(arg1));
			}

			private double getXPos(DiagramElement arg0) {
				return ((RectangleRenderingInformation) arg0.getRenderingInformation()).getPosition().x();
			}
		});
		
		return contents;
	}
}
