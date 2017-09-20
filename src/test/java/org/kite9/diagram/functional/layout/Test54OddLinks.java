package org.kite9.diagram.functional.layout;

import org.junit.Before;
import org.junit.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TextLineWithSymbols;
import org.kite9.framework.xml.DiagramKite9XMLElement;

public class Test54OddLinks extends AbstractLayoutFunctionalTest {

	Symbol s1, s2, s3, s4;
	TextLine line2, line3;
	TextLineWithSymbols tls;
	Glyph one, two, three;
	
	public void initTestDocument() {
		super.initTestDocument();
		s1 = new Symbol("Some text", 'a', SymbolShape.CIRCLE);
		s2 = new Symbol("Some text", 'A', SymbolShape.DIAMOND);
		s3 = new Symbol("Some text", 'A', SymbolShape.HEXAGON);

		tls = new TextLineWithSymbols("Here is line 1",
				createList(s1, s2, s3));
		
		line2 = new TextLine("Here is line 2");
		line3 = new TextLine("Here is line 3");
		s4 = new Symbol("Some text", 'q', SymbolShape.DIAMOND);
		one = new Glyph("from", "Stereo", "One",
				createList(
						tls,
						line2, 
						line3),
				createList(s4));
		
		two = new Glyph("two", "two", null, null);
		two = new Glyph("three", "three", null, null);
	}
	
	@Test
	public void test_54_1_LinkToTextLine() throws Exception {
		new Link(tls, two);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_54_2_LinkToSymbols() throws Exception {
		new Link(s1, two);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two), null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_54_3_LinkTwoSymbols() throws Exception {
		new Link(s1, s4);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(one, two), null);
		renderDiagram(d);
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Override
	protected boolean checkNoHops() {
		return false;
	}
	
	
}
