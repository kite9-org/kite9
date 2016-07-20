package org.kite9.diagram.article;

import java.io.IOException;

import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.functional.HopLink;
import org.kite9.diagram.functional.NotAddressed;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.visualization.display.style.sheets.Designer2012Stylesheet;

public class TestJavaHelloWorld extends AbstractFunctionalTest {
	
	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

	@Test
	@NotAddressed("We've seen unnecessary overlaps - depends on order connections are added")
	public void javaArchitecture() throws IOException {
		Glyph javaCode = new Glyph("java", "Your Java Project", null, null);
		Glyph kite9Lib = new Glyph("jar", "Kite9 Java Tool", null, null);
		Glyph diagram = new Glyph("xml", "A Diagram Definition", null, null);
		Glyph response = new Glyph("image", "The Diagram", 
				createList(
						new TextLine("PNG / PDF supported")), null);
		
		Arrow scans = new Arrow("scans");
		Arrow sends = new Arrow("sends");
		Arrow receives = new Arrow("receives");
		
		Context client = new Context("client side", createList((Contained) javaCode, kite9Lib, scans), true, new TextLine("Your PC (Client)"), null);
		
		Glyph project = new Glyph(null, "Project Definitions", null, null);
		Glyph diagramServer = new Glyph("web-app", "Kite9 Diagram Server", null, null);
		
		Context server = new Context("server side", createList((Contained) project, diagramServer), true, new TextLine("Kite9 Servers"), null);
		
		Diagram d = new Diagram("Arch", createList((Contained) client, server, diagram, sends, receives, response), null);
		
		
		// scans
		new Link(kite9Lib, scans, null, null, null, null, Direction.UP);
		new Link(scans, javaCode, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		
		// sends
		new Link(kite9Lib, sends, null, null, null, null, Direction.RIGHT);
		new Link(sends, server, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new HopLink(sends, diagram, null, null, null, null, Direction.DOWN);
		
		// receives
		new HopLink(server, receives, null, null, null, null, Direction.LEFT);
		new Link(receives, kite9Lib, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(receives, response);
		
		new Link(diagramServer, project, null, null, null, new TextLine("many"), Direction.UP);
		
		
		
		renderDiagram(d);
	}
	
	@Test
	public void javaArchitectureBroken() throws IOException {
		Glyph javaCode = new Glyph("java", "Your Java Project", null, null);
		Glyph kite9Lib = new Glyph("jar", "Kite9 Java Tool", null, null);
		Glyph diagram = new Glyph("xml", "A Diagram Definition", null, null);
		Glyph response = new Glyph("image", "The Diagram", 
				createList(
						new TextLine("PNG / PDF supported")), null);
		
		Arrow scans = new Arrow("scans");
		Arrow sends = new Arrow("sends");
		Arrow receives = new Arrow("receives");
		
		Context client = new Context("client side", createList((Contained) javaCode, kite9Lib, scans), true, new TextLine("Your PC (Client)"), null);
		
		Glyph project = new Glyph(null, "Project Definitions", null, null);
		Glyph diagramServer = new Glyph("web-app", "Kite9 Diagram Server", null, null);
		
		Context server = new Context("server side", createList((Contained) project, diagramServer), true, new TextLine("Kite9 Servers"), null);
		
		Context internet = new Context("internet", createList((Contained) diagram, sends, receives, response), false, null, null);
		
		Diagram d = new Diagram("Arch", createList((Contained) client, server, internet), null);
		
		
		// scans
		new Link(kite9Lib, scans, null, null, null, null, Direction.UP);
		new Link(scans, javaCode, null, null, LinkEndStyle.ARROW, null, Direction.UP);
		
		// sends
		new Link(kite9Lib, sends, null, null, null, null, Direction.RIGHT);
		new Link(sends, server, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(sends, diagram, null, null, null, null, Direction.UP);
		
		// receives
		new Link(server, receives, null, null, null, null, Direction.LEFT);
		new Link(receives, kite9Lib, null, null, null, null, Direction.LEFT);
		
		
		renderDiagram(d);
	}
	
	@Test
	public void javaArchitectureBroken2() throws IOException {
		Glyph kite9Lib = new Glyph("jar", "Kite9 Java Tool", null, null);
		Glyph diagram = new Glyph("xml", "A Diagram Definition", null, null);
		Glyph response = new Glyph("image", "The Diagram", 
				createList(
						new TextLine("PNG / PDF supported")), null);
		
		Arrow sends = new Arrow("sends");
		Arrow receives = new Arrow("receives");
		
		Context client = new Context("client side", createList((Contained) kite9Lib), true, new TextLine("Your PC (Client)"), null);
		
		
		Context server = new Context("server side", null, true, new TextLine("Kite9 Servers"), null);
		
		Diagram d = new Diagram("Arch", createList((Contained) client, server, diagram, sends, receives, response), null);
		
		
		// sends
		new HopLink(kite9Lib, sends, null, null, null, null, Direction.RIGHT);
		new HopLink(sends, server, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new HopLink(sends, diagram, null, null, null, null, Direction.UP);
		
		// receives
		new HopLink(server, receives, null, null, null, null, Direction.LEFT);
		new HopLink(receives, kite9Lib, null, null, null, null, Direction.LEFT);
		new HopLink(receives, response, null, null, null, null, Direction.DOWN);
		
		
		renderDiagram(d);
	}
	
}
