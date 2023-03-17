package org.kite9.diagram.functional.display;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kite9.diagram.AbstractDisplayFunctionalTest;
import org.kite9.diagram.adl.LinkBody;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramKite9XMLElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.adl.TextLabel;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.model.position.Direction;


public class Test13Key extends AbstractDisplayFunctionalTest {

	@Test
	public void test_13_1_SimpleKey() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("key", "some bold text" ,"blah",  null, Key.TESTING_DOCUMENT);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_2_TextKey() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("some bold text" , "some regular text that goes underneath.\nThis can sit on multiple lines.", null);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_3_With1Symbol() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("some bold text" , null, createList(new Symbol("Some information", 'S', SymbolShape.CIRCLE)));
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_4_With2Symbols() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("some text" , null, 
				createLongSymbolList(2)
			);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_5_WithLotsOfSymbols() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("some bold text" , null, 
				createLongSymbolList(10)
			);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	private Symbol[] getOptions() {
		return  new Symbol[] { 
			new Symbol("Some information", 'S', SymbolShape.CIRCLE),
			new Symbol("Partridge", 'P', SymbolShape.CIRCLE),
			new Symbol("Gemstone Beta", 'G', SymbolShape.HEXAGON),
			new Symbol("Multiline and very complicated\ndescription of the symbol\nitself", 'P', SymbolShape.DIAMOND),
			new Symbol("Par\ntwo lines", 'V', SymbolShape.CIRCLE) };
	}

		
	private List<Symbol> createLongSymbolList(int size) {
		int next = 0;
		Symbol[] options = getOptions();
		ArrayList<Symbol> out = new ArrayList<Symbol>(size);
		for (int i = 0; i < size; i++) {
			out.add(options[next++]);
			next = next % options.length;
		}
		return out;
	}
	
	private List<Symbol> createNarrowSymbolList(int size) {
		int next = 0;
		Symbol[] options = getOptions();
		ArrayList<Symbol> out = new ArrayList<Symbol>(size);
		while( out.size() < size) {
			Symbol symbol = options[next++];
			if (symbol.getText().length()<20) {
				out.add(symbol);
			}
			next = next % options.length;
		}
		return out;
	}
	
	@Test
	public void test_13_6_WithLotsOfWidth() throws Exception {
		
		Glyph a = new Glyph("", "fairly tediously long arrow", null, null);
		LinkBody b = new LinkBody("Gordon bennett this is a very long arrow");
		new Link(a, b, null, null, null, null, Direction.RIGHT);
		
		
		Key k = new Key("some bold text" , null, 
				createLongSymbolList(7)
			);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a, b), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_7_WithLotsOfLabelWidth() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		LinkBody b = new LinkBody("b");
		new Link(a, b, null, null, null, null, Direction.RIGHT);
		
		
		Key k = new Key("Here is a very long item of bold text which takes up a lot of width" , "shamu", 
				createLongSymbolList(7)
			);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a, b), k);
		renderDiagram(d);
	}
	
	
	@Test
	public void test_13_8_separateGlyphsWithKey() throws Exception {
		Symbol s = new Symbol("Brother of Tony Scott", 'T', SymbolShape.CIRCLE);
		Glyph hf = new Glyph("HF", "Actor","Harrison Ford", null, null);
		Glyph rs = new Glyph("RS", "Director", "Ridley Scott", createList(new TextLine("Directed: Thelma & Louise"), new TextLine("Directed: Gladiator")),
				createList(s));
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement("my_diagram", listOf(hf, rs), new Key("Big Movies", "", createList(s)));
		renderDiagram(d1);
	}
	
	@Test
	public void test_13_9_WonkyContextLabel() throws Exception {
			Glyph hf = new Glyph("harrison_ford","Actor","Harrison Ford", null, null);
			Glyph rs = new Glyph("ridley_scott", "Director", "Ridley Scott", null, null);
			LinkBody ww = new LinkBody("worked_with", "worked with");
			
			new Link(ww, hf, null, null, null, null, Direction.RIGHT);
			new Link(ww, rs);

			Context bladerunner = new Context("bladerunner", listOf(hf, rs, ww), true, new TextLabel("Bladerunner"), null);

			
			DiagramKite9XMLElement d1 = new DiagramKite9XMLElement("my_diagram", listOf(bladerunner), null);
			renderDiagram(d1);
		
	}
	
	@Test
	public void test_13_10_separateGlyphsWithKey2() throws Exception {
		Glyph a = new Glyph("a", "", "a", null, null);
		Glyph b = new Glyph("b", "", "b", null, null);
		Glyph c = new Glyph("c", "", "c", createList(new TextLine("some\nstuff"), new TextLine("here")), null);
		Glyph d = new Glyph("d", "", "d", null, null);
		Glyph e = new Glyph("e", "", "e", null, null);
		new Link(c, d, null, null, null, null, Direction.UP);
		new Link(c, e);
		
		Key key = new Key("Big Movies", "some text\nhere blah blah bah", null);
		key.setID("key");
		Context ctx = new Context("ctx", listOf(a, b, c), true, null, null);
		ctx.appendChild(key);
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement("my_diagram", listOf(ctx, d, e), null);
		renderDiagram(d1);
	}
	
	@Test
	public void test_13_11_AllTextAndWith1Symbol() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("some bold text" , "some body text\nspanning two lines", createList(new Symbol("Some information", 'S', SymbolShape.CIRCLE)));
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_12_TwoColumns() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("some bold text taking lots of space dsjhksdjfhjkdskj ds" , null , createNarrowSymbolList(7));
		k.setAttribute("style", "--kite9-direction: right; ");
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_13_ThreeColumns() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("some bold text taking lots of space dsjhksdjfhjkdskj d dcdshf kdskjfhjdsf jkhdskjf skdjhfjkds j s" , null , createNarrowSymbolList(7));
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_14_FourColumns() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		
		Key k = new Key("some bold text taking lots of space dsjhksdjfhjkdskj d dcdshf kdskjfhjdsf jkhdskjf skdjhfjkds j s" , null , createNarrowSymbolList(7));
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(a), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_15_InvisibleContext() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		Context c1 = new Context("c1", listOf(a), false, null, null);
		

		Key k = new Key("mon cle" , null , null);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(c1), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_16_InvisibleContext() throws Exception {
		
		Glyph a = new Glyph("", "a", null, null);
		Glyph b = new Glyph("", "b", null, null);
		Context c1 = new Context("c1", listOf(a), false, null, null);
		

		Key k = new Key("mon cle" , null , null);
		
		new Link(a, b, null, null, null, null, Direction.RIGHT);
		
		DiagramKite9XMLElement d = new DiagramKite9XMLElement("The Diagram", createList(c1, b), k);
		renderDiagram(d);
	}
	
	@Test
	public void test_13_17_separateGlyphsWithKey3() throws Exception {
		Glyph c = new Glyph("c", "", "c", null, null);
		Glyph e = new Glyph("e", "", "e", null, null);
		new Link(c, e, null, null, null, null, Direction.DOWN);
		Context ctx = new Context("ctx", listOf( c), true, new TextLabel("ssdvd ds fsdfs ds dsf dsf dsf ds"), null);
		DiagramKite9XMLElement d1 = new DiagramKite9XMLElement("my_diagram", listOf(ctx, e), null);
		renderDiagram(d1);
	}
}
