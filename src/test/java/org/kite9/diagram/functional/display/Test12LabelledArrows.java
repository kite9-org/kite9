package org.kite9.diagram.functional.display;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.KeyHelper;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.functional.AbstractLayoutFunctionalTest;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.framework.common.HelpMethods;


public class Test12LabelledArrows extends AbstractLayoutFunctionalTest {

	@Test
	public void test_12_1_LabelledLeftRight() throws IOException {
		
		Glyph a = new Glyph("", "a", null, null);
		Glyph b = new Glyph("", "b", null, null);
		
		Arrow i1 = new Arrow("i1");
		
		new Link(i1, a, null, null, null, new TextLine("from"), Direction.LEFT);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLine("to"), Direction.RIGHT);
						
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(a, b, i1), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_2_LabelledUpDown() throws IOException {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		Arrow i1 = new Arrow("i1", "i1");
		
		new Link(i1, a, null, null, null, new TextLine("from"), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLine("to"), Direction.DOWN);
						
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(a, b, i1), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_3_SymbolLabels() throws IOException {
		KeyHelper kh = new KeyHelper();
		
		
		Glyph a = new Glyph("stereo", "a", null, createList(kh.createSymbol("bob", 'b', SymbolShape.CIRCLE)));
		Glyph b = new Glyph("", "b", createList(new TextLine("some line of data", createList(kh.createSymbol("bizbox")))), createList(kh.createSymbol("terv", 'b', SymbolShape.HEXAGON)));
		
		Arrow i1 = new Arrow("i1");

		
		new Link(i1, a, null, new TextLine("lines", createList(kh.createSymbol("Trevor"))), null, null, Direction.LEFT);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		
		Symbol s1 = kh.createSymbol("bob");
		Symbol s2 = kh.createSymbol("jeff");
		
		
		TextLine clabel = new TextLine("Container Label", createList(s1, s2));
		
		Context con = new Context("c1",createList( a, b, i1), true, clabel, null);
				
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList( con), null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_12_4_VeryLongLabels() throws IOException {
		
		Glyph a = new Glyph("", "a", null, null);
		Glyph b = new Glyph("", "b", null, null);
		
		Arrow i1 = new Arrow("i1");
		
		new Link(i1, a, null, null, null, new TextLine("from the wild side"), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLine("to the safe side"), Direction.DOWN);
		
		Context con = new Context("c1",createList(a, b, i1), true, new TextLine("Container Label, oh the old container"), null);
				
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList( con), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_5_MultiLineLongLabels() throws IOException {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		Arrow i1 = new Arrow("i1", "i1");
		
		new Link(i1, a, null, null, null, new TextLine("from the wild side\ngoing east on the highway\nwith a frog"), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLine("to the safe side"), Direction.DOWN);
		
		Context con = new Context("c1",createList(a, b, i1), true, new TextLine("Container Label\n oh the old container\nhas a very long and tedious label"), null);
				
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(con), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_6_GlyphMultipleLabels() throws IOException {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Arrow i1 = new Arrow("i1");
		Arrow i2 = new Arrow("i2");
		Arrow i3 = new Arrow("i3");
		
		new Link(i1, a, null, null, null, new TextLine("from the wild side\ngoing east on the highway\nwith a frog"), Direction.UP);
		new Link(i2, a, null, null, LinkEndStyle.ARROW, null /* new TextLine("to the safe side A") */, Direction.UP);
		new Link(i3, a, null, null, LinkEndStyle.ARROW, null /* new TextLine("to the safe side B") */, Direction.UP);
		
				
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(a, i1, i2, i3), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_7_LabelsInside() throws IOException {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		new TurnLink(i1, a, null, null, null, new TextLine("from the wild side\ngoing east on the highway\nwith a frog 1"), null);
		new TurnLink(i1, b, null, null, null, new TextLine("from the wild side\ngoing east on the highway\nwith a frog 2"), null);
		new TurnLink(i2, a, null, null, LinkEndStyle.ARROW, new TextLine("to the safe side A"), null);
		new TurnLink(i2, b, null, null, LinkEndStyle.ARROW, new TextLine("to the safe side B"), null);
		
				
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList( a, i1, i2, b), null);
		renderDiagram(d);
	}

	@Test
	public void test_12_8_TestLabelledBothEnds() throws IOException {
		Glyph a = new Glyph("", "a", null, null);
		Arrow i1 = new Arrow("i1");
		Link l = new Link(i1, a);
		l.setDrawDirection(Direction.RIGHT);
		l.setFromLabel(new TextLine("hello"));
		l.setToLabel(new TextLine("gopher"));

		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(a, i1), null);
		renderDiagram(d);

	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_12_9_ChainOfLabels() throws IOException {
				
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		Context c = new Context("c1", Collections.EMPTY_LIST, true, new TextLine("Big C"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(i1, i2), true, new TextLine("Arrow Holder"), null);
		
		new Link(c, i1, null, null, null, new TextLine("from the wild side\ngoing east on the highway\nwith a frog 1"), Direction.RIGHT);
		new Link(c, i2, null, null, null, new TextLine("from the wild side\ngoing east on the highway\nwith a frog 2"), Direction.RIGHT);
		
		new Link(i1, i2, null, null, null, null, Direction.DOWN);
		
				
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(c, c2), null);
		renderDiagram(d);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_12_10_BlankLabels() throws IOException {
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		Context c = new Context("c1", Collections.EMPTY_LIST, true, new TextLine("Big C"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(i1, i2), true, new TextLine(""), null);
		
		new Link(c, i1, null, null, null, new TextLine(null), Direction.RIGHT);
		new Link(c, i2, null, null, null, new TextLine("  "), Direction.RIGHT);
		
		new Link(i1, i2, null, null, null, null, Direction.DOWN);
		
				
		DiagramXMLElement d = new DiagramXMLElement("The Diagram", createList(c, c2), new Key(null,"", new ArrayList<Symbol>()));
		renderDiagram(d);
		
	}

	@Test
	public void test_12_11_PushingRect() throws IOException {
		generate("label_push_error.xml");
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}


	
}
