package com.kite9.server.adl;

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

	protected byte[] loadStaticAdl(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	@Test
	public void testExampleADL() throws Exception {
		byte[] html = loadStaticAdl(MINIMAL + "?format=adl&v=testing");
		persistInAFile(html, "testExampleADl", "diagram.adl");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/github/testExampleADL/diagram.html"), Charset.forName("UTF-8"));
		XMLCompare.compareXML(expected, new String(html));
	}
}
