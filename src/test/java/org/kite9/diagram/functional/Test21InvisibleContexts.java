package org.kite9.diagram.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.functional.layout.AbstractLayoutFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.diagram.xml.XMLElement;

public class Test21InvisibleContexts extends AbstractLayoutFunctionalTest {

	int id = 0;
	
	
	List<XMLElement> createGlyphs(int count) {
		List<XMLElement> out = new ArrayList<XMLElement>(count);
		for (int i = 0; i < count; i++) {
			id ++;
			Glyph g = new Glyph("id"+id, "bob", "id"+id, null, null);
			List<XMLElement> textLines = new ArrayList<>();
			for (int j = 0; j < i; j++) {
				g.appendChild(new TextLine("Some text"));
			}
			out.add(g);
		}
		return out;
	}
	
	
	@Test
	public void test_21_1_3x3NoLinks() throws IOException {
		List<XMLElement> row1 = createGlyphs(3);
		List<XMLElement> row2 = createGlyphs(3);
		List<XMLElement> row3 = createGlyphs(3);
		
		Context c1 = new Context("ctx1", row1, false, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, false, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, false, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((XMLElement) c1, c2, c3), true, null, Layout.DOWN);
		
		DiagramXMLElement d = new DiagramXMLElement("d", createList((XMLElement) cc), null);
		renderDiagram(d);
		
	}
	
	@Test
	public void test_21_2_Nx3NoLinks() throws IOException {
		List<XMLElement> row1 = createGlyphs(8);
		List<XMLElement> row2 = createGlyphs(7);
		List<XMLElement> row3 = createGlyphs(5);
		
		Context c1 = new Context("ctx1", row1, false, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, false, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, false, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((XMLElement) c1, c2, c3), true, null, Layout.DOWN);
		
		DiagramXMLElement d = new DiagramXMLElement("d", createList((XMLElement) cc), null);
		renderDiagram(d);
		
	}
	
	@Test
	public void test_21_3_Nx3SomeLinksWithDirectedContext() throws IOException {
		List<XMLElement> row1 = createGlyphs(8);
		List<XMLElement> row2 = createGlyphs(7);
		List<XMLElement> row3 = createGlyphs(15);
		
		Context c1 = new Context("ctx1", row1, true, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, true, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, true, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((XMLElement) c1, c2, c3), true, null, Layout.DOWN);
		
//		new Link(c1, c2, null, null, null, null, Direction.DOWN);
//		new Link(c2, c3, null, null, null, null, Direction.DOWN);
		
		
		new Link((XMLElement)c1.getChildNodes().item(2), (XMLElement) c2.getChildNodes().item(3), null, null, null, null, null);
//		new Link((Connected) c1.getContents().get(4), (Connected) c2.getContents().get(3), null, null, null);
		new Link((XMLElement) c1.getChildNodes().item(4), (XMLElement) c3.getChildNodes().item(4), null, null, null, null, null);
//		
		DiagramXMLElement d = new DiagramXMLElement("d", createList((XMLElement) cc), Layout.DOWN, null);
		renderDiagram(d);
		
	}
	
	@Test
	public void test_21_4_Nx3SomeLinksWithLinkedContexts() throws IOException {
		List<XMLElement> row1 = createGlyphs(8);
		List<XMLElement> row2 = createGlyphs(7);
		List<XMLElement> row3 = createGlyphs(15);
		
		Context c1 = new Context("ctx1", row1, true, null, Layout.RIGHT);
		Context c2 = new Context("ctx2", row2, true, null, Layout.RIGHT);
		Context c3 = new Context("ctx3", row3, true, null, Layout.RIGHT);
		Context cc = new Context("ctxn", createList((XMLElement) c1, c2, c3), true, null, null);
		
		new Link(c1, c2, null, null, null, null, Direction.DOWN);
		new Link(c2, c3, null, null, null, null, Direction.DOWN);
		
		
		new Link((XMLElement) c1.getChildNodes().item(2), (XMLElement) c2.getChildNodes().item(3), null, null, null, null, null);
//		new Link((Connected) c1.getContents().get(4), (Connected) c2.getContents().get(3), null, null, null);
		new Link((XMLElement) c1.getChildNodes().item(4), (XMLElement) c3.getChildNodes().item(4), null, null, null, null, null);
//		
		DiagramXMLElement d = new DiagramXMLElement("d", createList((XMLElement) cc), Layout.DOWN, null);
		renderDiagram(d);
		
	}
	
	@Test
	public void test_21_5_hiddenContext() throws IOException {
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
		
		DiagramXMLElement d1 = new DiagramXMLElement("my_diagram", listOf(rs, directed, hidden), null);
		renderDiagram(d1);
	
}
}
