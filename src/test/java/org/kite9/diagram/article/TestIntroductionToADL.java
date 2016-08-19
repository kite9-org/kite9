package org.kite9.diagram.article;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.Arrow;
import org.kite9.diagram.xml.Diagram;
import org.kite9.diagram.xml.Glyph;
import org.kite9.diagram.xml.Link;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.diagram.xml.TextLine;

public class TestIntroductionToADL extends AbstractFunctionalTest  {

	@Test
	public void arrows() throws IOException {
		Glyph john = new Glyph("", "John", null, null);
		Glyph ledger = new Glyph("", "Ledger", null, null);
		
		Arrow writes = new Arrow("writes");
		
		new Link(john, writes, null, null, null, null, Direction.RIGHT);
		new Link(ledger, writes, null, null, null, null, Direction.LEFT);
		
		
		
		Diagram d = new Diagram("", createList((XMLElement) john, ledger, writes), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void someGlyphs() throws IOException {
		Glyph john = new Glyph("", "John", null, null);
		Glyph ledger = new Glyph("", "Ledger", null, null);
		Glyph HoAccounts = new Glyph("", "Head of Accounts", null, null);
		
		Diagram d = new Diagram("", createList((Contained) john, ledger, HoAccounts), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void arrowsDirected() throws IOException {
		Glyph john = new Glyph("", "John", null, null);
		Glyph ledger = new Glyph("", "Ledger", null, null);
		
		Arrow writes = new Arrow("writes");
		
		new Link(john, writes, null, null, null, null, Direction.RIGHT);
		new Link(writes, ledger, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		
		
		
		Diagram d = new Diagram("", createList((XMLElement) john, ledger, writes), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void arrowsUndirected() throws IOException {
		Glyph peter = new Glyph("", "Peter", null, null);
		Glyph dan = new Glyph("", "Dan", null, null);
		
		Arrow loves = new Arrow("loves");
		
		new Link(peter, loves,null, null,  null, null, Direction.RIGHT);
		new Link(loves, dan, null, null, null, null, Direction.RIGHT);
		
		
		
		Diagram d = new Diagram("", createList((Contained) peter, loves, dan), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	@Ignore
	/**
	 * @see http://www.kite9.com/content/planarization-no-merges-available-145
	 */
	public void arrowsTwice() throws IOException {
		Glyph peter = new Glyph("", "Peter", null, null);
		Glyph mary = new Glyph("", "Mary", null, null);
		
		Arrow loves = new Arrow("loves");
		Arrow despises = new Arrow("despises");
		
		new Link(peter, loves, null, null, null, null, Direction.RIGHT);
		new Link(loves, mary, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		
		new Link(mary, despises, null, null, null, null, Direction.LEFT);
		new Link(despises, peter, null, null, LinkEndStyle.ARROW, null, null);
		
		
		
		Diagram d = new Diagram("abc", createList(peter, loves, mary, despises), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void labelledArrows() throws IOException {
		Glyph john = new Glyph("", "John", null, null);
		Glyph audi = new Glyph("", "Audi", null, null);
		Glyph felixstowe = new Glyph("", "Felixstowe", null, null);
		
		
		Arrow goes = new Arrow("goes");
		
		new Link(john, goes, null, null, null, null, Direction.RIGHT);
		new Link(goes, felixstowe, null, null, null, new TextLine("to"), Direction.RIGHT);
		new Link(goes, audi,null, null,  null, new TextLine("in his"), Direction.DOWN);
		
		
		
		Diagram d = new Diagram("", createList((Contained) john, felixstowe, audi, goes), null);
		
		renderDiagram(d);
		
	}
	
	@Test
	public void extraContext() throws IOException {
		Glyph database = new Glyph("", "Client Database", null, null);
		Glyph server = new Glyph("", "Server", null, null);
		Glyph user = new Glyph("", "User", null, null);
		
		
		Arrow transaction = new Arrow("queries");
		Arrow distributed = new Arrow("distributed over");
		
		new Link(transaction, user, null, null, null, new TextLine("expects < 3 ms\nresponse time"), Direction.LEFT);
		new Link(transaction, database, null, null, null, new TextLine("with"), Direction.RIGHT);
		new Link(database, distributed, null, null, null, null, Direction.RIGHT);
		new Link(distributed, server, null, null, null, new TextLine("3-6"), Direction.RIGHT);
		
		
		
		Diagram d = new Diagram("", createList((Contained) database, server, user, transaction, distributed), null);
		
		renderDiagram(d);
		
	}
	
	
}
