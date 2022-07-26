package com.kite9.server.adl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StreamUtils;

@ContextConfiguration
public class PrivateGithubIT extends AbstractRestIT {

	public static final String JS = "/github/kite9-org/examples/some.js?v=v0.10";
	public static final String DIR = "/github/kite9-org/examples";


	protected byte[] loadStatic(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		String token = System.getProperty("token");
		headers.set("Authorization", "token "+token);
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	@Test
	@WithMockUser
	public void testExampleJS() throws Exception {
		byte[] js = loadStatic(JS+"?format=js&v=main");
		persistInAFile(js, "testExampleJs", "some.js");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/privategithub/testExampleJs/some.js"), Charset.forName("UTF-8"));
		assertEquals(expected, new String(js));
	}
	
	@Test
	@WithMockUser
	public void testRepo() throws Exception {
		byte[] json = loadStatic(DIR + "?format=adl");
		persistInAFile(json, "testDir", "repo.json");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/privategithub/testDir/repo.json"), Charset.forName("UTF-8"));
		assertEquals(expected, new String(json));
	}
	
}
