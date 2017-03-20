package org.kite9.diagram.functional.display;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.xml.DiagramKite9XMLElement;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.LinkEndStyle;

public class Test21InvisibleContexts extends AbstractLayoutFunctionalTest {

	int id = 0;
	
	
	List<Kite9XMLElement> createGlyphs(int count) {
		List<Kite9XMLElement> out = new ArrayList<Kite9XMLElement>(count);
		for (int i = 0; i < count; i++) {
			id ++;
			Glyph g = new Glyph("id"+id, "bob", "id"+id, null, null);
			out.add(g);
		}
		return out;
	}
	
	
	@Test
	public void test_21_1_3x3NoLinks() throws Exception {
		List<Kite9XMLElement> row1 = createGlyphs(3);
		List<Kite9XMLElement> row2 = createGlyphs(3);
		List<Kite9XMLElement> row3 = createGlyphs(3);
		
		Context c1 = new Context("ctx1", row1, false, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, false, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, false, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((Kite9XMLElement) c1, c2, c3), true, null, Layout.DOWN);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", createList((Kite9XMLElement) cc), null);
		renderDiagram(d);
		
	}
	
	@Test
	public void test_21_2_Nx3NoLinks() throws Exception {
		List<Kite9XMLElement> row1 = createGlyphs(8);
		List<Kite9XMLElement> row2 = createGlyphs(7);
		List<Kite9XMLElement> row3 = createGlyphs(5);
		
		Context c1 = new Context("ctx1", row1, false, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, false, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, false, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((Kite9XMLElement) c1, c2, c3), true, null, Layout.DOWN);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", createList((Kite9XMLElement) cc), null);
		renderDiagram(d);
		
	}
	
	@Test
	public void test_21_3_Nx3SomeLinksWithDirectedContext() throws Exception {
		List<Kite9XMLElement> row1 = createGlyphs(8);
		List<Kite9XMLElement> row2 = createGlyphs(7);
		List<Kite9XMLElement> row3 = createGlyphs(15);
		
		Context c1 = new Context("ctx1", row1, true, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, true, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, true, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((Kite9XMLElement) c1, c2, c3), true, null, Layout.DOWN);
		
//		new Link(c1, c2, null, null, null, null, Direction.DOWN);
//		new Link(c2, c3, null, null, null, null, Direction.DOWN);
		
		
		new Link((Kite9XMLElement)c1.getChildNodes().item(2), (Kite9XMLElement) c2.getChildNodes().item(3), null, null, null, null, null);
//		new Link((Connected) c1.getContents().get(4), (Connected) c2.getContents().get(3), null, null, null);
		new Link((Kite9XMLElement) c1.getChildNodes().item(4), (Kite9XMLElement) c3.getChildNodes().item(4), null, null, null, null, null);
//		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", createList((Kite9XMLElement) cc), Layout.DOWN, null);
		renderDiagram(d);
		
	}
	
	@Test
	public void test_21_4_Nx3SomeLinksWithLinkedContexts() throws Exception {
		List<Kite9XMLElement> row1 = createGlyphs(8);
		List<Kite9XMLElement> row2 = createGlyphs(7);
		List<Kite9XMLElement> row3 = createGlyphs(15);
		
		Context c1 = new Context("ctx1", row1, true, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, true, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, true, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((Kite9XMLElement) c1, c2, c3), true, null, null);
		
		new Link(c1, c2, null, null, null, null, Direction.DOWN);
		new Link(c2, c3, null, null, null, null, Direction.DOWN);
		
		
		new Link((Kite9XMLElement) c1.getChildNodes().item(2), (Kite9XMLElement) c2.getChildNodes().item(3), null, null, null, null, null);
//		new Link((Connected) c1.getContents().get(4), (Connected) c2.getContents().get(3), null, null, null);
		new Link((Kite9XMLElement) c1.getChildNodes().item(4), (Kite9XMLElement) c3.getChildNodes().item(4), null, null, null, null, null);
//		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("d", createList((Kite9XMLElement) cc), Layout.DOWN, null);
		renderDiagram(d);
		
	}
	
	@Test
	public void test_21_5_hiddenContext() throws Exception {
		Glyph rs = new Glyph("ridley_scott", "Director", "Ridley Scott", null, null);
		Arrow directed = new Arrow("directed");
		
		new Link(directed, rs);

		Glyph bladerunner = new Glyph("film", "Bladerunner", null, null);
		Glyph glad = new Glyph("film", "Gladiator", null, null);
		Glyph thelma = new Glyph("film", "Thelma & Louise", null, null);

		new TurnLink(directed, bladerunner, null, null, LinkEndStyle.ARROW, null);
		new TurnLink(directed, glad, null, null, LinkEndStyle.ARROW, null);
		new TurnLink(directed, thelma, null, null, LinkEndStyle.ARROW, null);

		Context hidden = new Context(listOf(bladerunner, glad, thelma), false, null, Layout.RIGHT);
		
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement("my_diagram", listOf(rs, directed, hidden), null);
		renderDiagram(d1);
	
}
}
