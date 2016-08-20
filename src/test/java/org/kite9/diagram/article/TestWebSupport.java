package org.kite9.diagram.article;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.diagram.xml.LinkEndStyle;
import org.kite9.framework.common.HelpMethods;

public class TestWebSupport extends AbstractFunctionalTest {

	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Test
	public void testShowArchitecture() throws IOException {
		Glyph yourPage = new Glyph("html", "Your Page", 
				HelpMethods.createList(
						new TextLine("HTML Content of the page"),
						new TextLine("<IMG> tag within the page")
				), null);
		
		Glyph xml = new Glyph("xml", "Your Diagram", 
				HelpMethods.createList(
						new TextLine("XML Diagram file")
				), null);
		
		Context yourHost = new Context(HelpMethods.listOf(yourPage, xml), true, new TextLine("yourhost.com"), null);
		
		
		Glyph png = new Glyph("png", "Your Diagram", 
				HelpMethods.createList(
						new TextLine("Diagram Image")
				), null);
		
		Glyph map = new Glyph("map", "Your Diagram Map", 
				HelpMethods.createList(
						new TextLine("Client Side Image Map")
				), null);
		
		Glyph server = new Glyph("server", "Kite9 Diagram Engine", null, null);
		
		Context kite9Host = new Context(HelpMethods.listOf(png, map, server), true, new TextLine("server.kite9.org"), null);
		
		new Link(yourPage, server, null, new TextLine("<img src=\"http://server.kite.org... />"), LinkEndStyle.ARROW, null);
		new Link(server, xml, null, new TextLine("loads"), LinkEndStyle.ARROW, null);
		new Link(server, png, null, new TextLine("creates"), LinkEndStyle.ARROW, null);
		new Link(server, map, null, new TextLine("creates"), LinkEndStyle.ARROW, null);
		
		DiagramXMLElement d = new DiagramXMLElement(HelpMethods.listOf(kite9Host, yourHost), null);
		renderDiagramNoWM(d);
	}
}
