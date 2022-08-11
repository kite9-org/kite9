package com.kite9.server.command.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.testing.TestingHelp;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StreamUtils;

import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.pipeline.command.Command;
import com.kite9.pipeline.command.xml.insert.Delete;
import com.kite9.pipeline.command.xml.insert.InsertUrl;
import com.kite9.pipeline.command.xml.insert.InsertUrlWithChanges;
import com.kite9.pipeline.command.xml.insert.InsertXML;
import com.kite9.pipeline.command.xml.move.ADLMoveCells;
import com.kite9.pipeline.command.xml.move.Move;
import com.kite9.pipeline.command.xml.replace.ReplaceAttr;
import com.kite9.pipeline.command.xml.replace.ReplaceStyle;
import com.kite9.pipeline.command.xml.replace.ReplaceTag;
import com.kite9.pipeline.command.xml.replace.ReplaceTagUrl;
import com.kite9.pipeline.command.xml.replace.ReplaceText;
import com.kite9.pipeline.command.xml.replace.ReplaceText.PreserveChildElements;
import com.kite9.pipeline.command.xml.replace.ReplaceXML;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.XMLCompare;
import com.kite9.server.controllers.CommandController;
import com.kite9.server.update.AbstractUpdateHandler;
import com.kite9.server.update.Update;
import com.kite9.server.uri.URIWrapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
	"kite9.exported-hostname="
})
public class CommandsTest {
	
	private static final String SETUP_XML = "/public/commands/test_command1.adl";
	
	@Autowired
	CommandController commandController;
	
	@LocalServerPort
	protected int port;
	
	String sourceURI;
	
	@BeforeEach
	public void setupUrl() {
		sourceURI = "http://localhost:"+port+SETUP_XML;
		Logger l = (Logger) LoggerFactory.getLogger(AbstractUpdateHandler.class);
		l.setLevel(Level.ALL);
	}
	
	@Test
	public void testReplaceXMLCommand1() throws Exception {
		ReplaceXML replace = new ReplaceXML();
		replace.fragmentId = "The Diagram";
		replace.from= Base64.getEncoder().encodeToString(("<diagram  id=\"The Diagram\">\n" +
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
				"  </diagram>").getBytes());
		replace.to= Base64.getEncoder().encodeToString(("<lets id=\"The Diagram\"><do>some</do>replacing</lets>").getBytes());
		
		testDoAndUndo(replace, "replaceXML");
	}


	@Test
	public void testReplaceTag() throws Exception {
		ReplaceTag replace = new ReplaceTag();
		replace.fragmentId = "link";
		replace.from= Base64.getEncoder().encodeToString(("<link id=\"link\" rank=\"4\"></link>").getBytes());
		replace.to= Base64.getEncoder().encodeToString(("<glyph id=\"The Diagram\" style=\"raw: fish\"></glyph>").getBytes());
		replace.keptAttributes = Arrays.asList("rank", "id", "style");
	
		testDoAndUndo(replace, "replaceTag");
	}
	
	@Test
	public void testReplaceTagUrl() throws Exception {
		ReplaceTagUrl replace = new ReplaceTagUrl();
		replace.fragmentId = "link";
		replace.from= Base64.getEncoder().encodeToString(("<link id=\"link\" rank=\"4\"></link>").getBytes());
		replace.to= sourceURI+"#one";
		replace.keptAttributes = Arrays.asList("rank", "id");
	
		testDoAndUndo(replace, "replaceTagUrl");
	}
	
	@Test
	public void testMoveCommand() throws Exception {
		Move move = new Move();
		move.moveId = "one-label";
		move.to = "two";
		move.from = "one";
		move.toBefore = "two-label";
		
		testDoAndUndo(move, "move");
	}
	
	@Test
	public void testADLMoveCellsCommand() throws Exception {
		ADLMoveCells move = new ADLMoveCells();
		move.fragmentId = "table1";
		move.horiz = true;
		move.from = 1;
		move.push = 3;
		
		sourceURI = sourceURI.replace("command1", "command2");
		
		testDoAndUndo(move, "adlMoveCells");
	}
	
	@Test
	public void testADLMoveCellsCommand2() throws  Exception {
		ADLMoveCells move = new ADLMoveCells();
		move.fragmentId = "table1";
		move.horiz = true;
		move.from = 1;
		move.push = 3;
		move.excludedIds = Collections.singletonList("t3");
		
		sourceURI = sourceURI.replace("command1", "command2");
		
		testDoAndUndo(move, "adlMoveCells2");
	}


	@Test
	public void testReplaceTextCommand1() throws  Exception {
		ReplaceText setText = new ReplaceText();
		setText.to =  "Winner";
		setText.from = "";
		setText.fragmentId="two";
		setText.preserve=PreserveChildElements.AFTER;
		
		testDoAndUndo(setText, "replaceText1");
	}
	
	@Test
	public void testReplaceTextCommand2() throws Exception {
		ReplaceText setText = new ReplaceText();
		setText.to =  "Winner";
		setText.from = "Two";
		setText.fragmentId="two-label";
		setText.preserve=PreserveChildElements.NONE;
		
		testDoAndUndo(setText, "replaceText2");
	}
	
	@Test
	public void testReplaceAttrCommand() throws Exception {
		ReplaceAttr setAttr = new ReplaceAttr();
		setAttr.name =  "name";
		setAttr.to = "value";
		setAttr.fragmentId="two";
	
		testDoAndUndo(setAttr, "replaceAttr");
		
	}
	
	@Test
	public void testReplaceStyleCommand() throws Exception {
		ReplaceStyle setStyle = new ReplaceStyle();
		setStyle.name =  "stroke";
		setStyle.to = "red";
		setStyle.fragmentId="two";
	
		testDoAndUndo(setStyle, "replaceStyle");
		
	}

	@Test
	public void testReplaceStyleCommand2() throws Exception {
		ReplaceStyle setStyle = new ReplaceStyle();
		setStyle.name =  "stroke";
		setStyle.to = "single back() border 1.5em";
		setStyle.fragmentId="two";

		testDoAndUndo(setStyle, "replaceStyle2");

	}

	@Test
	public void testDeleteCommand() throws Exception {
		Delete delete = new Delete();
		delete.fragmentId = "The Diagram";
		delete.beforeId = "two";
		delete.base64Element = Base64.getEncoder().encodeToString(("<glyph id=\"one\">\n" + 
				"      <label id=\"one-label\">One</label>\n" + 
				"    </glyph>").getBytes());
		delete.containedIds = Collections.singletonList("one-label");
		
		testDoAndUndo(delete, "delete");

	}
	
	@Test
	public void testDeleteAllCommand() throws Exception {
		Delete delete = new Delete();
		delete.fragmentId = "The Diagram";
		delete.beforeId = "two";
		delete.base64Element = Base64.getEncoder().encodeToString(("<glyph id=\"one\">\n" + 
				"      <label id=\"one-label\">One</label>\n" + 
				"    </glyph>").getBytes());

		testDoAndUndo(delete, "deleteAll");

	}

	@Test
	public void testInsertXML1Command() throws Exception {
		InsertXML copy = new InsertXML();
		copy.base64Element = Base64.getEncoder().encodeToString(("<some id=\"seven\"><xml>goes</xml>here</some>").getBytes());
		copy.fragmentId="The Diagram";
		copy.beforeId="two";
		testDoAndUndo(copy, "insertXML1");
	} 
	
	@Test
	public void testInsertXML2Command() throws Exception {
		InsertXML copy = new InsertXML();
		copy.base64Element = Base64.getEncoder().encodeToString(("<some id=\"seven\"><xml id=\"ig\">goes</xml>here</some>").getBytes());
		copy.fragmentId="The Diagram";
		copy.beforeId="two";
		copy.containedIds = Collections.singletonList("ig");
		testDoAndUndo(copy, "insertXML2");
	} 
	
	@Test
	public void testInsertUrlCommand() throws Exception {
		String uri = sourceURI+"#one";
		
		InsertUrl copy = new InsertUrl();
		copy.uriStr =  uri;
		copy.fragmentId="The Diagram";
		copy.newId="six";
		
		testDoAndUndo(copy, "insertUrl");

	} 
	
	@Test
	public void testInsertUrl2Command() throws Exception {
		String uri = sourceURI+"#two";
		
		InsertUrl copy = new InsertUrl();
		copy.uriStr =  uri;
		copy.fragmentId="The Diagram";
		copy.newId="six";
		copy.containedIds = Arrays.asList("two");
		copy.beforeId = "link";
		
		testDoAndUndo(copy, "insertUrl2");

	} 
	

	@Test
	public void testInsertUrlLinkCommand() throws Exception {
		String uri = sourceURI+"#link";
		
		InsertUrlWithChanges copy = new InsertUrlWithChanges();
		copy.uriStr =  uri;
		copy.fragmentId="The Diagram";
		copy.newId="six";

		Map<String, String> xpathToValue = new HashMap<>();
		xpathToValue.put("*[local-name()='from']/@reference", "one-label");
		xpathToValue.put("*[local-name()='to']/@reference", "two-label");

		copy.xpathToValue = xpathToValue;

		testDoAndUndo(copy, "insertUrlLink");
	} 
	

	private void testDoAndUndo(Command c, String name) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, Kite9MediaTypes.ADL_XML_VALUE);
		headers.set(HttpHeaders.ACCEPT, Kite9MediaTypes.ADL_XML_VALUE);
		ADLDom out = commandController.applyCommandOnStatic(buildRequestEntity(c, sourceURI, headers), sourceURI);
		String modified = performSaveAndCheck(out, name, "/commands/after_"+name+".xml");
		System.out.println("After command "+name+" do(): \n"+modified+"\n adl: \n"+new String(getBytes(out)));
		String modifiedBase64 = Base64.getEncoder().encodeToString(modified.getBytes());
		ADLDom out2 = commandController.applyCommandOnStatic(buildUndoRequestEntity(c, modifiedBase64, new URI(sourceURI), headers), sourceURI);
		String resource = sourceURI.substring(sourceURI.indexOf("/public"));
		String back = performSaveAndCheck(out2, name+ "-2", "/static"+resource);
		System.out.println("After command "+name+" undo(): \n"+back+"\n adl: \n"+new String(getBytes(out)));
	}

	private byte[] getBytes(ADLDom out) {
		String xml = new XMLHelper().toXML(out.getDocument(), true);
		return xml.getBytes();
	}
	
	
	private RequestEntity<Update> buildRequestEntity(Command c, String uri, HttpHeaders headers) throws URISyntaxException {
		URI javaNetURI = new URI(uri);
		K9URI uri2 = URIWrapper.wrap(javaNetURI);
		Update commands = new Update(Collections.singletonList(c), uri2, Update.Type.NEW);
		return new RequestEntity<>(commands, headers, HttpMethod.POST, javaNetURI);
	}
	
	private RequestEntity<Update> buildUndoRequestEntity(Command c, String base64adl, URI sourceUri, HttpHeaders headers) {
		Update commands = new Update(Collections.singletonList(c), base64adl, Update.Type.UNDO);
		RequestEntity<Update> out = new RequestEntity<>(commands, headers, HttpMethod.POST, sourceUri);
		return out;
	}
	

	public String performSaveAndCheck(ADLDom out, String name, String resource) throws Exception {
		
		System.out.println("Metadata: \n" + out.getMetaData());
		
		byte[] result = getBytes(out);
		TestingHelp.writeOutput(this.getClass(), null, name+".xml", result);
		String expected4 = StreamUtils.copyToString(this.getClass().getResourceAsStream(resource), StandardCharsets.UTF_8);
		String s = new String(result);
		XMLCompare.compareXML(expected4, s);
		return s;
	}
	

}
