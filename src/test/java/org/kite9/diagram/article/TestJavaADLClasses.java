package org.kite9.diagram.article;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.adl.Arrow;
import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.LinkEndStyle;
import org.kite9.diagram.adl.TextLine;
import org.kite9.diagram.functional.AbstractFunctionalTest;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.visualization.display.java2d.style.sheets.BasicStylesheet;
import org.kite9.diagram.visualization.display.java2d.style.sheets.CGWhiteStylesheet;
import org.kite9.framework.Kite9Item;

public class TestJavaADLClasses extends AbstractFunctionalTest {

	@Test
	public void javaArchitecture1() throws IOException {
		Diagram d = architecture();

		renderDiagramNoWM(d, new CGWhiteStylesheet());
	}

	@Kite9Item
	public Diagram architecture() {
		Glyph diagram = new Glyph("xml", "A Diagram Definition", null, null);
		Glyph response = new Glyph("zip file", "Response", createList(new TextLine("PNG Image (or) "), new TextLine(
				"PDF Image"), new TextLine("Client-side image map")), null);

		Arrow sends = new Arrow("sends");
		Arrow receives = new Arrow("receives");

		Glyph client = new Glyph("client side", "Kite9 Java Tool", createList(new TextLine("(or another tool)")), null);

		Glyph diagramServer = new Glyph("web-app", "Kite9 Diagram Server", null, null);

		Context transport = new Context("Over the wire", createList((Contained) diagram, sends, receives, response),
				true, new TextLine("HTTP Over Internet"), Layout.DOWN);
		Context yourside = new Context("yours", createList((Contained) client), true, new TextLine(
				"Your Server / PC"), null);
		Context ourside = new Context("ours", createList((Contained) diagramServer), true, new TextLine(
				"Kite9 Servers"), null);

		Diagram d = new Diagram("Arch", createList((Contained) yourside, transport, ourside), null);

		// sends
		new Link(client, sends, null, null, null, null, Direction.RIGHT);
		new Link(sends, diagramServer, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(sends, diagram);

		// receives
		new Link(diagramServer, receives, null, null, null, null, Direction.LEFT);
		new Link(receives, client, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(receives, response);
		return d;
	}

	/**
	 * @see http://www.kite9.com/content/planarization-no-merges-available-145
	 */
	@Test
	@Ignore
	public void javaArchitecture2() throws IOException {
		Glyph diagram = new Glyph("xml", "A Diagram Definition", null, null);
		Glyph response = new Glyph("zip file", "Response", createList(new TextLine("PNG Image (or) "), new TextLine(
				"PDF Image"), new TextLine("Client-side image map")), null);

		Arrow sends = new Arrow("sends");
		Arrow receives = new Arrow("receives");

		Glyph objects = new Glyph("java objects", "ADL Model", null, null);
		Glyph xstream = new Glyph("", "XStream", null, null);
		Arrow converts = new Arrow("converts");

		Glyph client = new Glyph("client side", "Kite9 Java Tool", null, null);

		Glyph diagramServer = new Glyph("web-app", "Kite9 Diagram Server", null, null);

		Context transport = new Context("Over the wire", createList((Contained) diagram, sends, receives, response),
				true, new TextLine("HTTP Over Internet"), null);
		
		Context yourside = new Context("yours", createList((Contained) converts, client, xstream, objects), true,
				new TextLine("Your Server / PC"), null);
		Context ourside = new Context("ours", createList((Contained) diagramServer), true, new TextLine(
				"Kite9 Servers"), null);

		Diagram d = new Diagram("Arch", createList((Contained) yourside, transport, ourside), null);

		// converts

		new Link(converts, client);
		new Link(converts, xstream, null, null, null, new TextLine("using"), null);
		new Link(converts, objects, null, null, null, new TextLine("from"), null);
		new Link(converts, diagram, null, null, null, new TextLine("to"), null);

		// sends
		new Link(sends, diagramServer, null, null, LinkEndStyle.ARROW, null, Direction.RIGHT);
		new Link(sends, diagram);

		// receives
		new Link(diagramServer, receives, null, null, null, null, Direction.LEFT);
		new Link(receives, client, null, null, LinkEndStyle.ARROW, null, Direction.LEFT);
		new Link(receives, response);

		renderDiagramNoWM(d, new BasicStylesheet());
	}

}
