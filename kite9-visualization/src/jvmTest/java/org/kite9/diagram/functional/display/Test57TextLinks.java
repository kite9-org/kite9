package org.kite9.diagram.functional.display;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.functional.TestingEngine.Checks;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.common.HelpMethods;

public class Test57TextLinks extends AbstractDisplayFunctionalTest {
	
	protected Checks checks() {
		Checks out = new Checks();
		out.checkMidConnection = false;
		out.everythingStraight = false;
		return out;
	}
	
	@Test
	public void test_57_1_TextLineLink1() throws Exception {
		TextLine tl = new TextLine("linker", "some row");
		Glyph one = new Glyph("Stereo", "One", HelpMethods.listOf(tl), null);
		LinkBody a = new LinkBody("meets");
		new Link(a, tl, null, null, null, null, Direction.RIGHT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, a));

		renderDiagram(d);
	}
	
	@Test
	public void test_57_2_TextLineLink2() throws Exception {
		TextLine tl = new TextLine("linker", "some row");
		TextLine tl2 = new TextLine("linker2", "some other row");
		Glyph one = new Glyph("bigboy", "Stereo", "One", HelpMethods.listOf(tl, tl2), null);
		LinkBody a = new LinkBody("a");
		LinkBody b = new LinkBody("b");
		new Link(a, tl, null, null, null, null, Direction.RIGHT);
		new Link(b, tl2, null, null, null, null, Direction.LEFT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, a, b));

		renderDiagram(d);
	}
	

	@Test
	public void test_57_3_ExpandingArrow1() throws Exception {
		TextLine tl = new TextLine("linker", "some row blah blah blah");
		Glyph one = new Glyph("Stereo", "One", HelpMethods.listOf(tl), null);
		Glyph two = new Glyph("Stereo", "Two", null, null);
		LinkBody a = new LinkBody("a");
		a.setAttribute("style","	--kite9-sizing: maximize;");
		new Link(a, one, null, null, null, null, Direction.UP);
		new Link(one, two, null, null, null, null, Direction.LEFT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, a));

		renderDiagram(d);
	}
	
	/**
	 * Unlike 57_3, this one joins to the text line.
	 */
	@Test
	public void test_57_4_ExpandingArrow2() throws Exception {
		TextLine tl = new TextLine("linker", "some row blah blah blah");
		Glyph one = new Glyph("one", "Stereo", "One", HelpMethods.listOf(tl), null);
		Glyph two = new Glyph("two","Stereo", "Two", null, null);
		LinkBody a = new LinkBody("a", "a");
		a.setAttribute("style","	--kite9-sizing: maximize;");
		new Link(a, tl, null, null, null, null, Direction.UP);
		new Link(one, two, null, null, null, null, Direction.LEFT);
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two, a));

		renderDiagram(d);
	}
	
	Symbol s1, s4;
	TextLine tls;
	Glyph one, two, three;
	
	public void initTestDocument() {
		super.initTestDocument();
		s1 = new Symbol("Some text", 'a', SymbolShape.CIRCLE);
		s1.setID("s1");

		tls = new TextLine("Here is line 1");

		s4 = new Symbol("Some text", 'q', SymbolShape.DIAMOND);
		s4.setID("s4");
		one = new Glyph("from", "Stereo", "One",
				createList(
						tls),
				createList(s4));
		
		two = new Glyph("two", "two", null, null);
	}

}
