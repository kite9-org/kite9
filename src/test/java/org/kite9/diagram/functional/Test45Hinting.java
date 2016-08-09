package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.HintMap;
import org.kite9.diagram.adl.PositionableDiagramElement;
import org.kite9.diagram.common.hints.PositioningHints;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.diagram.xml.Link;
import org.kite9.framework.common.HelpMethods;

public class Test45Hinting extends AbstractFunctionalTest {


	private void checkInVerticalLine(Diagram d2) {
		DiagramElement up = getById("up", d2);
		DiagramElement mid = getById("mid", d2);
		DiagramElement down = getById("down", d2);
		DiagramAssert.assertInDirection(up, mid, Direction.DOWN);
		DiagramAssert.assertInDirection(mid, down, Direction.DOWN);
	}
	
	private void checkInOrdinalHorizontalLine(Diagram d2) {
		DiagramElement up = getById("up", d2);
		DiagramElement mid = getById("mid", d2);
		DiagramElement down = getById("down", d2);
		DiagramElement next = getById("next", d2);
		DiagramAssert.assertInDirection(next, down, Direction.RIGHT);
		DiagramAssert.assertInDirection(down, mid, Direction.RIGHT);
		DiagramAssert.assertInDirection(mid, up, Direction.RIGHT);
	}
	
	private void checkInHorizontalLine(Diagram d2) {
		Contained prev = null;
		for (Contained c : d2.getContents()) {
			if (prev != null) {
				DiagramAssert.assertInDirection(prev, c, Direction.RIGHT);
			} 
			prev = c;
		}
	}
	
	private Diagram create8Glyphs() {
		List<Contained> created = new ArrayList<Contained>();
		for (int i = 0; i < 8; i++) {
			created.add(new Glyph("glyph"+i, "", "G"+i, null, null));
		}
		
		return new Diagram("d", created, Layout.HORIZONTAL, null);
	}

	private Diagram create4Glyphs(boolean addLinks, boolean addHints) {
		Glyph up = new Glyph("up", null, "UP", null, null);
		Glyph mid = new Glyph("mid", null, "MID", null, null);
		Glyph down = new Glyph("down", null, "DOWN", null, null);
		
		up.setPositioningHints(new HintMap());
		mid.setPositioningHints(new HintMap());
		down.setPositioningHints(new HintMap());
		
		if (addHints) {
			PositioningHints.planFill(up.getPositioningHints(), 0f, .5f, 0f, .25f);
			PositioningHints.planFill(mid.getPositioningHints(), 0f, .5f, .26f, .5f);
			PositioningHints.planFill(down.getPositioningHints(), 0f, .5f, .51f, .99f);
		}
		
		Glyph next = new Glyph("next", null, "NEXT", null, null);
		Diagram d= new Diagram(HelpMethods.listOf(next, down, mid, up), null);
		
		if (addLinks) {
			new Link(up, next, null, null, null, null, Direction.RIGHT);
			new Link(mid, next, null, null, null, null, Direction.RIGHT);
			new Link(down, next, null, null, null, null, Direction.RIGHT);
		}
		return d;
	}
	
	@Test
	public void test_45_1_UpDownPlanarizationHinting() throws IOException {
		Diagram d = create4Glyphs(true, true);
		Diagram d2 = renderDiagram(d);
		checkInVerticalLine(d2);
	}
	
	@Test
	public void test_45_2_UpDownPlanarizationHinting2() throws IOException {
		Diagram d= create4Glyphs(false, true);
		Diagram d2 = renderDiagram(d);
		checkInHorizontalLine(d2);
	}
	
	@Test
	public void test_45_3_OrdinalOrder() throws IOException {
		Diagram d= create4Glyphs(false, false);
		List<Contained> ordered1 = d.getContents();
		Diagram d2 = renderDiagram(d);
		checkInOrdinalHorizontalLine(d2);
		List<Contained> ordered2 = getInOrder(new ArrayList<Contained>(d2.getContents()));
		Assert.assertEquals(ordered1.toString(), ordered2.toString());
	}
	
	@Test
	public void test_45_4_OrdinalOrderOverridesPositionHinting() throws IOException {
		// check that diagram orders glyphs in the collection order
		Diagram d = create8Glyphs();
		Diagram d2 = renderDiagram(d);
		checkInHorizontalLine(d2);
		
		// change the collection order, and check that it is preserved
		Collections.swap(d2.getContents(), 0, 5);
		Collections.swap(d2.getContents(), 3, 1);
		Collections.swap(d2.getContents(), 6, 2);
		
		List<Contained> ordered2 = new ArrayList<Contained>(d2.getContents());
		Diagram d3 = renderDiagram(d2);
		List<Contained> ordered3 = getInOrder(new ArrayList<Contained>(d3.getContents()));
		Assert.assertEquals(ordered2.toString(), ordered3.toString());
	}
	
	@Test 
	public void test_45_5_HintingChangesOrdinal() throws IOException {
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		Glyph c = new Glyph("c", "", "c", null, null);
		
		Diagram d = new Diagram("d", HelpMethods.listOf(a, b, c), Layout.VERTICAL, null);
		new Link(a, b);
		new Link(a, c);
		Diagram d2 = renderDiagram(d);
		System.out.println(d2.getContents());
		
		// make sure the ordinal order has changed to reflect the layout.
		Assert.assertEquals(HelpMethods.listOf(b, a, c).toString(), d2.getContents().toString());
	}
	
	private List<Contained> getInOrder(List<Contained> contents) {
		Collections.sort(contents,
				new Comparator<Contained>() {

			@Override
			public int compare(Contained arg0, Contained arg1) {
				return ((Double)getXPos(arg0)).compareTo(getXPos(arg1));
			}

			private double getXPos(Contained arg0) {
				return ((RectangleRenderingInformation) ((PositionableDiagramElement)arg0).getRenderingInformation()).getPosition().x();
			}
		});
		
		return contents;
	}
}
