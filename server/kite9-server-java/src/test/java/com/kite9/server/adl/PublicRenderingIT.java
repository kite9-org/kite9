package com.kite9.server.adl;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.kite9.server.adl.format.MediaTypeHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import com.kite9.server.XMLCompare;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;

public class PublicRenderingIT extends AbstractRestIT {
	
	public static final String MINIMAL = "/public/templates/risk-first/examples/minimal.adl";

	protected byte[] loadStaticHtml(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.TEXT_HTML));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	protected byte[] loadStaticPNG(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.IMAGE_PNG));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	protected byte[] loadStaticSVG(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.parseMediaType(Kite9MediaTypes.SVG_VALUE)));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	protected byte[] loadStaticESVG(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.parseMediaType(Kite9MediaTypes.EDITABLE_SVG_VALUE)));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	
	protected byte[] loadStaticADL(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.parseMediaType(Kite9MediaTypes.ADL_SVG_VALUE)));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	@Test
	public void testExampleHTML() throws Exception {
		byte[] html = loadStaticHtml(MINIMAL + "?format=html");
		persistInAFile(html, "testExampleHTML", "diagram.html");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/public/testExampleHTML/diagram.html"), Charset.forName("UTF-8"));
		XMLCompare.compareXML(expected, new String(html));
	}
	
	@Test
	public void testExampleADL() throws Exception {
		byte[] svg = loadStaticADL(MINIMAL);
		persistInAFile(svg, "testExampleADL", "diagram.adl");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/static/"+MINIMAL), Charset.forName("UTF-8"));
		XMLCompare.compareXML(new String(svg), expected);
	}
	
	@Test
	public void testExampleSVG() throws Exception {
		byte[] svg = loadStaticSVG(MINIMAL+"?format=svg");
		persistInAFile(svg, "testExampleSVG", "diagram.svg");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/public/testExampleSVG/diagram.svg"), Charset.forName("UTF-8"));
		XMLCompare.compareXML(new String(svg), expected);
	}
	
	@Test
	public void testExampleESVG() throws Exception {
		byte[] svg = loadStaticESVG(MINIMAL + "?format=esvg");
		persistInAFile(svg, "testExampleESVG", "diagram.svg");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/public/testExampleESVG/diagram.svg"), Charset.forName("UTF-8"));
		XMLCompare.compareXML(new String(svg), expected);
	}
	
	@Test
	public void testExamplePNG() throws Exception {
		byte[] png = loadStaticPNG(MINIMAL + "?width=500&format=png");
		persistInAFile(png, "testExamplePNG", "diagram.png");
		byte[] expected = StreamUtils.copyToByteArray(this.getClass().getResourceAsStream("/rendering/public/testExamplePNG/diagram.png"));
		Assertions.assertEquals(expected.length, png.length);
	}
}
