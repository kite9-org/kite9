package com.kite9.server.adl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import com.kite9.server.XMLCompare;

public class GithubIT extends AbstractRestIT {

	public static final String MINIMAL = "/github/kite9-org/kite9/templates/risk-first/examples/minimal.adl";
	public static final String JS = "/github/kite9-org/kite9/client/behaviours/actionable/actionable.js";
	public static final String DIR = "/github/kite9-org/kite9/client/behaviours/rest";


	protected byte[] loadStatic(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
//		headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	@Test
	public void testExampleJS() throws Exception {
		byte[] js = loadStatic(JS+"?format=js&v=master");
		persistInAFile(js, "testExampleJs", "actionable.js");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/github/testExampleJs/actionable.js"), Charset.forName("UTF-8"));
		assertEquals(expected, new String(js));
	}
	
	@Test
	public void testExampleADL() throws Exception {
		byte[] adl = loadStatic(MINIMAL + "?format=adl&v=master");
		persistInAFile(adl, "testExampleADL", "diagram.adl");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/github/testExampleADL/diagram.adl"), Charset.forName("UTF-8"));
		XMLCompare.compareXML(expected, new String(adl));
	}
	
	@Test
	public void testDir() throws Exception {
		byte[] json = loadStatic(DIR + "?format=json");
		persistInAFile(json, "testDir", "dir.json");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/github/testDir/dir.json"), Charset.forName("UTF-8"));
		assertEquals(expected, new String(json));
	}
	
}
