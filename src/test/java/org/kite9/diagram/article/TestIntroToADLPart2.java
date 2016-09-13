package org.kite9.diagram.article;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Key;
import org.kite9.diagram.adl.KeyHelper;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;

public class TestIntroToADLPart2 extends AbstractFunctionalTest {

	@Test
	public void roverBiscuits() throws IOException {
		Glyph rover = new Glyph("", "John", null, null);
		Glyph english = new Glyph("", "English", null, null);
		Glyph biscuits = new Glyph("", "Tea", null, null);
		
		Arrow isa = new Arrow("is");
		Arrow likes = new Arrow("like");
		
		
		new Link(rover, isa, null, null, null, null, Direction.UP);
		new Link(isa, english, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		
		
		new Link(likes, english, null, null, null, new TextLine("all"), Direction.LEFT);
		new Link(likes, biscuits, null, null, LinkEndStyle.ARROW, null, null);
		
		
		DiagramXMLElement d = new DiagramXMLElement("", createList(rover, english, isa, likes, biscuits), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void peopleCompex() throws IOException {
		Glyph emile = new Glyph("", "Emile", null, null);
		Glyph john = new Glyph("", "John", null, null);
		Glyph ellen = new Glyph("", "Ellen", null, null);
		Glyph male = new Glyph("", "Male", null, null);
		Glyph female = new Glyph("", "Female", null, null);
		Glyph english = new Glyph("", "English", null, null);
		Glyph french = new Glyph("", "French", null, null);
		
		Arrow emile_is_1 = new Arrow("is1", "is");
		Arrow emile_is_2 = new Arrow("is2", "is");
		Arrow john_is_1 = new Arrow("is3", "is");
		Arrow john_is_2 = new Arrow("is4", "is");
		Arrow ellen_is_1 = new Arrow("is5", "is");
		Arrow ellen_is_2 = new Arrow("is6", "is");
		
		new Link(emile, emile_is_1, null, null, null, null, Direction.UP);
		new Link(emile_is_1, french, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(emile, emile_is_2, null, null,  null, null, Direction.UP);
		new Link(emile_is_2, male, null, null, LinkEndStyle.ARROW, null, Direction.UP);

		new Link(john, john_is_1, null, null, null, null, Direction.UP);
		new Link(john_is_1, english, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(john, john_is_2,null, null,  null, null, Direction.UP);
		new Link(john_is_2, male, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		
		
		new Link(ellen, ellen_is_1, null, null, null, null, Direction.UP);
		new Link(ellen_is_1, english, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		new Link(ellen, ellen_is_2,null, null,  null, null, Direction.UP);
		new Link(ellen_is_2, female, null, null, LinkEndStyle.ARROW, null, Direction.UP);
	
		DiagramXMLElement d = new DiagramXMLElement("", createList(john, ellen, emile, english, french, male, female, john_is_1, john_is_2, emile_is_1, emile_is_2, ellen_is_1, ellen_is_2), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void peopleContext1() throws IOException {
		Glyph emile = new Glyph("", "Emile", null, null);
		Context french = new Context("French people", createList(emile), true, new TextLine("French people"), null);

		Glyph john = new Glyph("", "John", null, null);
		Glyph ellen = new Glyph("", "Ellen", null, null);

		Context english = new Context("English people", createList(john, ellen), true, new TextLine("English people"), null);
	
		DiagramXMLElement d = new DiagramXMLElement("abc", createList(english, french), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void peopleContext2() throws IOException {
		Glyph emile = new Glyph("", "Emile", null, null);
		Context male1 = new Context("Male", createList(emile), true, new TextLine("Male"), null);
		Context french = new Context("French people", createList(male1), true, new TextLine("French people"), null);

		Glyph john = new Glyph("", "John", null, null);
		Context male2 = new Context("Male", createList(john), true, new TextLine("Male") , null);
		Glyph ellen = new Glyph("", "Ellen", null, null);
		Context female1 = new Context("Female", createList(ellen), true, new TextLine("Female"), null);

		Context english = new Context("English people", createList(male2, female1), true, new TextLine("English people"), null);
	
		DiagramXMLElement d = new DiagramXMLElement("abc", createList(english, french), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void systemDiagram() throws IOException {
		Glyph john = new Glyph("john","", "John", null, null);
		Context c2 = new Context("c2", createList(john), true, new TextLine("Ohio Office"), Layout.UP);

		Glyph report = new Glyph("report", "", "Report", null, null);
		Glyph accounts = new Glyph("ad", "", "Accounts Department", null, null);
		Glyph sales = new Glyph("sd", "", "Sales Department", null, null);
		
		Arrow sends = new Arrow("sends", "sends");
		
		
		new Link(sends, john,null, null,  null, new TextLine("monthly"), Direction.LEFT);
		new Link(sends, accounts,null, null,  LinkEndStyle.ARROW, null, Direction.RIGHT);
		
		
		new Link(sends, report,null, null,  null, null, Direction.UP);

		Context c1 = new Context("c1", createList(accounts, sales), true, new TextLine("Head Office"), Layout.UP);
		
		DiagramXMLElement d = new DiagramXMLElement("", createList(c1, sends, report, c2), null);
		
		renderDiagram(d);
		
	}
	
	
	@Test
	public void peopleKey() throws IOException {
		KeyHelper helper = new KeyHelper();
		Symbol male = helper.createSymbol("Male");
		Symbol female = helper.createSymbol("Female");
		Symbol english = helper.createSymbol("English");
		Symbol french = helper.createSymbol("French");
		
		Glyph emile = new Glyph("", "Emile", null, createList(male, french));
		Glyph john = new Glyph("", "John", null, createList(male, english));
		Glyph ellen = new Glyph("", "Ellen", null, createList(female, english));
		
		Key k = new Key("Explanation of Symbols", null, new ArrayList<Symbol>(helper.getUsedSymbols()));
		
		DiagramXMLElement d = new DiagramXMLElement("", createList(john, ellen, emile), k);
		
		renderDiagram(d);
		
	}
	
}
