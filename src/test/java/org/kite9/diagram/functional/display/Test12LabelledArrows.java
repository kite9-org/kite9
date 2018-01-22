package org.kite9.diagram.functional.display;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.NotAddressed;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.KeyHelper;
import org.kite9.diagram.adl.TextLabel;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.adl.TextLineWithSymbols;
import org.kite9.diagram.adl.TurnLink;
import org.kite9.diagram.functional.layout.TestingEngine.LayoutErrorException;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.xml.DiagramKite9XMLElement;


public class Test12LabelledArrows extends AbstractDisplayFunctionalTest {

	@Test
	public void test_12_1_LabelledLeftRight() throws Exception {
		
//		Glyph a = new Glyph("g1", "", "aasdsad", null, null);
		Glyph b = new Glyph("g2", "", "bsadsad", null, null);
		
		Arrow i1 = new Arrow("arrow1", "i1asdas ");
		
		TextLabel from = new TextLabel("from");
		from.setID("fromLabel");
		TextLabel toLabel = new TextLabel("to dsdsfds f ds f");
		toLabel.setID("toLabel");
		
//		new Link(i1, a, null, null, null, from, Direction.LEFT);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, toLabel, Direction.DOWN);
						
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(b, i1), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_2_LabelledUpDown() throws Exception {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		Arrow i1 = new Arrow("i1", "i1");
		
		new Link(i1, a, null, null, null, new TextLabel("from"), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLabel("to"), Direction.DOWN);
						
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a, b, i1), null);
		renderDiagram(d);
	}
	
	@NotAddressed("See technical_debt/getLength()")
	@Test(expected=LayoutErrorException.class)
	public void test_12_3_SymbolLabels() throws Exception {
		KeyHelper kh = new KeyHelper();
		
		
		Glyph a = new Glyph("stereo", "a", null, createList(kh.createSymbol("bob", 'b', SymbolShape.CIRCLE)));
		Glyph b = new Glyph("", "Something\nWicked", createList(new TextLineWithSymbols("some line of data", createList(kh.createSymbol("bizbox")))), createList(kh.createSymbol("terv", 'b', SymbolShape.HEXAGON)));
		
		Arrow i1 = new Arrow("i1");

		
		new Link(i1, a, null, new TextLineWithSymbols("lines", createList(kh.createSymbol("Trevor"))), null, null, Direction.LEFT);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		
		Symbol s1 = kh.createSymbol("bob");
		Symbol s2 = kh.createSymbol("jeff");
		
		
//		TextLineWithSymbols clabel = new TextLineWithSymbols("Container Label", createList(s1, s2));
//		
//		Context con = new Context("c1",createList( a, b, i1), true, clabel, null);
//				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( a,b,i1), null);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_12_4_VeryLongLabels() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		Glyph b = new Glyph("", "b", null, null);
		
		Arrow i1 = new Arrow("i1");
		
		new Link(i1, a, null, null, null, new TextLabel("from the wild side"), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLabel("to the safe side"), Direction.DOWN);
		
		Context con = new Context("c1",createList(a, b, i1), true, new TextLabel("Container Label, oh the old container"), null);
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( con), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_5_MultiLineLongLabels() throws Exception {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		Arrow i1 = new Arrow("i1", "i1");
		
		new Link(i1, a, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog"), Direction.UP);
		new Link(i1, b, null, null, LinkEndStyle.ARROW, new TextLabel("to the safe side"), Direction.DOWN);
		
		Context con = new Context("c1",createList(a, b, i1), true, new TextLabel("Container Label\n oh the old container\nhas a very long and tedious label"), null);
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(con), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_6_GlyphMultipleLabels() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Arrow i1 = new Arrow("i1");
		Arrow i2 = new Arrow("i2");
		Arrow i3 = new Arrow("i3");
		
		new Link(i1, a, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog"), Direction.UP);
		new Link(i2, a, null, null, LinkEndStyle.ARROW, null /* new LabelTextLine("to the safe side A") */, Direction.UP);
		new Link(i3, a, null, null, LinkEndStyle.ARROW, null /* new LabelTextLine("to the safe side B") */, Direction.UP);
		
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a, i1, i2, i3), null);
		renderDiagram(d);
	}
	
	@Test
	public void test_12_7_LabelsInside() throws Exception {
		
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		
		
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		new TurnLink(i1, a, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog 1"), null);
		new TurnLink(i1, b, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog 2"), null);
		new TurnLink(i2, a, null, null, LinkEndStyle.ARROW, new TextLabel("to the safe side A"), null);
		new TurnLink(i2, b, null, null, LinkEndStyle.ARROW, new TextLabel("to the safe side B"), null);
		
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList( a, i1, i2, b), null);
		renderDiagram(d);
	}

	@Test
	public void test_12_8_TestLabelledBothEnds() throws Exception {
		Glyph a = new Glyph("glyph","", "a", null, null);
		Arrow i1 = new Arrow("arrow", "i1");
		Link l = new Link(i1, a);
		l.setDrawDirection(Direction.RIGHT);
		l.setFromLabel(new TextLabel("arrow-hello"));
		l.setToLabel(new TextLabel("glyph-gopher"));

		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a, i1), null);
		renderDiagram(d);

	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_12_9_ChainOfLabels() throws Exception {
				
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		Context c = new Context("c1", Collections.EMPTY_LIST, true, new TextLabel("Big C"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(i1, i2), true, new TextLabel("Arrow Holder"), null);
		
		new Link(c, i1, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog 1"), Direction.RIGHT);
		new Link(c, i2, null, null, null, new TextLabel("from the wild side\ngoing east on the highway\nwith a frog 2"), Direction.RIGHT);
		
		new Link(i1, i2, null, null, null, null, Direction.DOWN);
		
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(c, c2), null);
		renderDiagram(d);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_12_10_BlankLabels() throws Exception {
		Arrow i1 = new Arrow("i1", "i1");
		Arrow i2 = new Arrow("i2", "i2");
		
		Context c = new Context("c1", Collections.EMPTY_LIST, true, new TextLabel("Big C"), null);
		Context c2 = new Context("c2", HelpMethods.listOf(i1, i2), true, new TextLabel(""), null);
		
		new Link(c, i1, null, null, null, new TextLabel(null), Direction.RIGHT);
		new Link(c, i2, null, null, null, new TextLabel("  "), Direction.RIGHT);
		
		new Link(i1, i2, null, null, null, null, Direction.DOWN);
		
				
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(c, c2), new Key(null,"", new ArrayList<Symbol>()));
		renderDiagram(d);
		
	}
	
}
