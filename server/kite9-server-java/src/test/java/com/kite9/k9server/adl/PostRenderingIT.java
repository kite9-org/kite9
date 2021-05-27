package com.kite9.k9server.adl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kite9.diagram.common.StreamHelp;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.kite9.k9server.XMLCompare;
import com.kite9.k9server.adl.format.media.Kite9MediaTypes;


/**
 * Tests that basic Kite9 ADL rendering functionality is working.
 * 
 * This is an open endpoint with no security.
 * 
 * @author robmoffat
 *
 */
public class PostRenderingIT extends AbstractRestIT {
	
	protected byte[] withBytesInFormat(MediaType output, String xml) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(output));
		headers.setContentType( Kite9MediaTypes.ADL_SVG);
		HttpEntity<byte[]> postBody = new HttpEntity<byte[]>(xml.getBytes(), headers);
		
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+"/api/renderer"), HttpMethod.POST, postBody, byte[].class);
		return back.getBody();
	}
	
	@Test
	public void testHTMLRender() throws Exception {
		byte[] back = withBytesInFormat(MediaType.TEXT_HTML, createDiagramXML1());
		persistInAFile(back, "testHTMLRender", "diagram.html");
		XMLCompare.compareXML(StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/post/testHTMLRender/diagram.html"), Charset.forName("UTF-8")), new String(back));
	}

	
	@Test
	public void testADLAndSVGRender() throws Exception {
		byte[] back = withBytesInFormat(Kite9MediaTypes.ADL_SVG, createDiagramXML1());
		persistInAFile(back, "testADLRender", "diagram.adl");
		XMLCompare.compareXML(StreamUtils.copyToString(this.getClass().getResourceAsStream("/static/public/templates/risk-first/minimal.adl"), Charset.forName("UTF-8")), new String(back));
	}
	
	@Test
	public void testSVGRender() throws Exception {
		byte[] back = withBytesInFormat(Kite9MediaTypes.SVG, createDiagramXML1());
		persistInAFile(back, "testSVGRender", "diagram.svg");
		XMLCompare.compareXML(StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/post/testSVGRender/diagram.svg"), Charset.forName("UTF-8")), new String(back));

		// do different diagram
		back = withBytesInFormat(Kite9MediaTypes.SVG, createDiagramXML2());
		persistInAFile(back, "testSVGRender", "diagram-2.svg");
		XMLCompare.compareXML(StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/post/testSVGRender/diagram-2.svg"), Charset.forName("UTF-8")), new String(back));
	}
	
	@Test
	public void testPNGRender() throws Exception {
		byte[] back = withBytesInFormat(MediaType.IMAGE_PNG, createDiagramXML1());
		persistInAFile(back, "testPNGRender", "diagram.png");
		byte[] expected = StreamUtils.copyToByteArray(this.getClass().getResourceAsStream("/rendering/post/testPNGRender/diagram.png"));
		Assertions.assertEquals(expected.length, back.length);
		
		back = withBytesInFormat(MediaType.IMAGE_PNG, createDiagramXML2());
		persistInAFile(back, "testPNGRender", "diagram-2.png");
		expected = StreamUtils.copyToByteArray(this.getClass().getResourceAsStream("/rendering/post/testPNGRender/diagram-2.png"));
		Assertions.assertEquals(expected.length, back.length);
	}
	
	@Test
	public void testESVGRender() throws Exception {
		byte[] back = withBytesInFormat(Kite9MediaTypes.ESVG, createDiagramXML1());
		persistInAFile(back, "testESVGRender", "diagram.svg");
		XMLCompare.compareXML(StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/post/testESVGRender/diagram.svg"), Charset.forName("UTF-8")), new String(back));
	}

	public Document parseBytesToXML(byte[] back) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		dbf.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbf.setExpandEntityReferences(false);
		DocumentBuilder db= dbf.newDocumentBuilder();
		Document d = db.parse(
			new InputSource(new ByteArrayInputStream(back)));
		return d;
	}
	
	public String createDiagramXML1() throws IOException {
		StringWriter sw = new StringWriter();
		StreamHelp.streamCopy(new InputStreamReader(this.getClass().getResourceAsStream("/static/public/templates/risk-first/minimal.adl")), sw, true);
		String theDiagram = sw.toString();
		return theDiagram;
	}
	
	public String createDiagramXML2() throws IOException {
		StringWriter sw = new StringWriter();
		StreamHelp.streamCopy(new InputStreamReader(this.getClass().getResourceAsStream("/static/public/templates/risk-first/votes.adl")), sw, true);
		String theDiagram = sw.toString();
		return theDiagram;
	}
}
