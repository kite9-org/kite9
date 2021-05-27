package com.kite9.k9server.command.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kite9.diagram.testing.TestingHelp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StreamUtils;

import com.kite9.k9server.XMLCompare;
import com.kite9.k9server.adl.format.media.Kite9MediaTypes;
import com.kite9.k9server.adl.holder.pipeline.ADLOutput;
import com.kite9.k9server.command.Command;
import com.kite9.k9server.command.CommandException;
import com.kite9.k9server.command.xml.insert.Delete;
import com.kite9.k9server.command.xml.insert.InsertUrl;
import com.kite9.k9server.command.xml.insert.InsertUrlLink;
import com.kite9.k9server.command.xml.insert.InsertXML;
import com.kite9.k9server.command.xml.move.ADLMoveCells;
import com.kite9.k9server.command.xml.move.Move;
import com.kite9.k9server.command.xml.replace.ReplaceAttr;
import com.kite9.k9server.command.xml.replace.ReplaceStyle;
import com.kite9.k9server.command.xml.replace.ReplaceTag;
import com.kite9.k9server.command.xml.replace.ReplaceTagUrl;
import com.kite9.k9server.command.xml.replace.ReplaceText;
import com.kite9.k9server.command.xml.replace.ReplaceText.PreserveChildElements;
import com.kite9.k9server.command.xml.replace.ReplaceXML;
import com.kite9.k9server.controllers.CommandController;
import com.kite9.k9server.update.Update;
import com.kite9.k9server.update.Update.Type;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
	"kite9.exported-hostname="
})
public class CommandsTest {
	
	private static final String SETUP_XML = "/public/commands/test_command1.adl";

	public static final String END_SVG_DOCUMENT = "</svg:svg>";

	public static final String NS = " xmlns=\"http://www.kite9.org/schema/adl\"\n  xmlns:svg='http://www.w3.org/2000/svg' ";

	public static final String START_SVG_DOCUMENT = "<svg:svg xmlns:xlink='http://www.w3.org/1999/xlink' " + NS + ">";
	
	@Autowired
	CommandController commandController;
	
	@LocalServerPort
	protected int port;
	
	String sourceURI;
	
	@BeforeEach
	public void setupUrl() {
		sourceURI = "http://localhost:"+port+SETUP_XML;
	}
	
	@Test
	public void testReplaceXMLCommand1() throws CommandException, Exception {
		ReplaceXML replace = new ReplaceXML();
		replace.fragmentId = "The Diagram";
		replace.from= Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><diagram  id=\"The Diagram\">\n" + 
				"    <glyph id=\"one\">\n" + 
				"      <label id=\"one-label\">One</label>\n" + 
				"    </glyph>\n" + 
				"    <glyph id=\"two\">\n" + 
				"      <label id=\"two-label\">Two</label>\n" + 
				"    </glyph>\n" + 
				"    <link id=\"link\" rank=\"4\">\n" + 
				"      <from reference=\"one\" id=\"link-from\" />\n" + 
				"      <to class=\"arrow\" reference=\"two\" id=\"link-to\" />\n" + 
				"    </link>\n" + 
				"  </diagram></svg:svg>").getBytes());
		replace.to= Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><lets id=\"The Diagram\"><do>some</do>replacing</lets></svg:svg>").getBytes());
		
		testDoAndUndo(replace, "replaceXML");
	}


	@Test
	public void testReplaceTag() throws CommandException, Exception {
		ReplaceTag replace = new ReplaceTag();
		replace.fragmentId = "link";
		replace.from= Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><link id=\"link\" rank=\"4\"></link></svg:svg>").getBytes());
		replace.to= Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><glyph id=\"The Diagram\"></glyph></svg:svg>").getBytes());
		replace.keptAttributes = Arrays.asList("rank", "id");
	
		testDoAndUndo(replace, "replaceTag");
	}
	
	@Test
	public void testReplaceTagUrl() throws CommandException, Exception {
		ReplaceTagUrl replace = new ReplaceTagUrl();
		replace.fragmentId = "link";
		replace.from= Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><link id=\"link\" rank=\"4\"></link></svg:svg>").getBytes());
		replace.to= sourceURI+"#one";
		replace.keptAttributes = Arrays.asList("rank", "id");
	
		testDoAndUndo(replace, "replaceTagUrl");
	}
	
	@Test
	public void testMoveCommand() throws CommandException, Exception {
		Move move = new Move();
		move.moveId = "one-label";
		move.to = "two";
		move.from = "one";
		move.toBefore = "two-label";
		
		testDoAndUndo(move, "move");
	}
	
	@Test
	public void testADLMoveCellsCommand() throws CommandException, Exception {
		ADLMoveCells move = new ADLMoveCells();
		move.fragmentId = "table1";
		move.horiz = true;
		move.from = 1;
		move.push = 3;
		
		sourceURI = sourceURI.replace("command1", "command2");
		
		testDoAndUndo(move, "adlMoveCells");
	}
	
	@Test
	public void testADLMoveCellsCommand2() throws CommandException, Exception {
		ADLMoveCells move = new ADLMoveCells();
		move.fragmentId = "table1";
		move.horiz = true;
		move.from = 1;
		move.push = 3;
		move.excludedIds = Arrays.asList("t3");
		
		sourceURI = sourceURI.replace("command1", "command2");
		
		testDoAndUndo(move, "adlMoveCells2");
	}


	@Test
	public void testReplaceTextCommand1() throws CommandException, Exception {
		ReplaceText setText = new ReplaceText();
		setText.to =  "Winner";
		setText.from = "";
		setText.fragmentId="two";
		setText.preserve=PreserveChildElements.AFTER;
		
		testDoAndUndo(setText, "replaceText1");
	}
	
	@Test
	public void testReplaceTextCommand2() throws CommandException, Exception {
		ReplaceText setText = new ReplaceText();
		setText.to =  "Winner";
		setText.from = "Two";
		setText.fragmentId="two-label";
		setText.preserve=PreserveChildElements.NONE;
		
		testDoAndUndo(setText, "replaceText2");
	}
	
	@Test
	public void testReplaceAttrCommand() throws CommandException, Exception {
		ReplaceAttr setAttr = new ReplaceAttr();
		setAttr.name =  "name";
		setAttr.to = "value";
		setAttr.fragmentId="two";
	
		testDoAndUndo(setAttr, "replaceAttr");
		
	}
	
	@Test
	public void testReplaceStyleCommand() throws CommandException, Exception {
		ReplaceStyle setStyle = new ReplaceStyle();
		setStyle.name =  "stroke";
		setStyle.to = "red";
		setStyle.fragmentId="two";
	
		testDoAndUndo(setStyle, "replaceStyle");
		
	}

	@Test
	public void testDeleteCommand() throws CommandException, Exception {
		Delete delete = new Delete();
		delete.fragmentId = "The Diagram";
		delete.beforeId = "two";
		delete.base64Element = Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><glyph id=\"one\">\n" + 
				"      <label id=\"one-label\">One</label>\n" + 
				"    </glyph></svg:svg>").getBytes());;
		delete.containedIds = Arrays.asList("one-label");
		
		testDoAndUndo(delete, "delete");

	}
	
	@Test
	public void testDeleteAllCommand() throws CommandException, Exception {
		Delete delete = new Delete();
		delete.fragmentId = "The Diagram";
		delete.beforeId = "two";
		delete.base64Element = Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><glyph id=\"one\">\n" + 
				"      <label id=\"one-label\">One</label>\n" + 
				"    </glyph></svg:svg>").getBytes());;
		
		testDoAndUndo(delete, "deleteAll");

	}

	@Test
	public void testInsertXML1Command() throws CommandException, Exception {
		InsertXML copy = new InsertXML();
		copy.base64Element = Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><some id=\"seven\"><xml>goes</xml>here</some></svg:svg>").getBytes());
		copy.fragmentId="The Diagram";
		copy.beforeId="two";
		testDoAndUndo(copy, "insertXML1");
	} 
	
	@Test
	public void testInsertXML2Command() throws CommandException, Exception {
		InsertXML copy = new InsertXML();
		copy.base64Element = Base64.getEncoder().encodeToString(("<svg:svg "+NS+"><some id=\"seven\"><xml id=\"ig\">goes</xml>here</some></svg:svg>").getBytes());
		copy.fragmentId="The Diagram";
		copy.beforeId="two";
		copy.containedIds = Arrays.asList("ig");
		testDoAndUndo(copy, "insertXML2");
	} 
	
	@Test
	public void testInsertUrlCommand() throws CommandException, Exception {
		String uri = sourceURI+"#one";
		
		InsertUrl copy = new InsertUrl();
		copy.uriStr =  uri;
		copy.fragmentId="The Diagram";
		copy.newId="six";
		
		testDoAndUndo(copy, "insertUrl");

	} 
	

	@Test
	public void testInsertUrlLinkCommand() throws CommandException, Exception {
		String uri = sourceURI+"#link";
		
		InsertUrlLink copy = new InsertUrlLink();
		copy.uriStr =  uri;
		copy.fragmentId="The Diagram";
		copy.newId="six";
		copy.fromId = "one-label";
		copy.toId = "two-label";
		
		testDoAndUndo(copy, "insertUrlLink");
	} 
	

	private void testDoAndUndo(Command c, String name) throws Exception, URISyntaxException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(Kite9MediaTypes.ADL_SVG);
		headers.setAccept(Collections.singletonList(Kite9MediaTypes.ADL_SVG));
		ADLOutput<?> out = commandController.applyCommandOnStatic(buildRequestEntity(c, sourceURI, headers), sourceURI);
		String modified = performSaveAndCheck(out, name, "/commands/after_"+name+".xml");
		String modifiedBase64 = Base64.getEncoder().encodeToString(modified.getBytes());
		ADLOutput<?> out2 = commandController.applyCommandOnStatic(buildUndoRequestEntity(c, modifiedBase64, new URI(sourceURI), headers), sourceURI);
		String resource = sourceURI.substring(sourceURI.indexOf("/public"));
		performSaveAndCheck(out2, name+ "-2", "/static"+resource);
	}
	
	
	private RequestEntity<Update> buildRequestEntity(Command c, String uri, HttpHeaders headers) throws URISyntaxException {
		URI uri2 = new URI(uri);
		Update commands = new Update(Collections.singletonList(c), uri2, Type.NEW);
		RequestEntity<Update> out = new RequestEntity<>(commands, headers, HttpMethod.POST, uri2);
		return out;
	}
	
	private RequestEntity<Update> buildUndoRequestEntity(Command c, String base64adl, URI sourceUri, HttpHeaders headers) throws URISyntaxException {
		Update commands = new Update(Collections.singletonList(c), base64adl, Type.UNDO);
		RequestEntity<Update> out = new RequestEntity<>(commands, headers, HttpMethod.POST, sourceUri);
		return out;
	}
	

	public String performSaveAndCheck(ADLOutput<?> out, String name, String resource) throws Exception {
		
		System.out.println(out.getMetaData());
		
		byte[] result = out.getAsBytes();
		TestingHelp.writeOutput(this.getClass(), null, name+".xml", result);
		String expected4 = StreamUtils.copyToString(this.getClass().getResourceAsStream(resource), Charset.forName("UTF-8"));
		String s = new String(result);
		XMLCompare.compareXML(expected4, s);
		return s;
	}
	

}
