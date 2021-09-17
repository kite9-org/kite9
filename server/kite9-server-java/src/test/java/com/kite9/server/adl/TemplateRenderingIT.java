package com.kite9.server.adl;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.kite9.diagram.logging.Kite9Log.Destination;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;

import com.kite9.server.XMLCompare;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;

public class TemplateRenderingIT extends AbstractRestIT {

	@Autowired
	ResourcePatternResolver resolver;
	
	@Test
	public void testAllADLFiles() throws Exception {
		Kite9LogImpl.setLogging(Destination.OFF);
		Resource[] resources = resolver.getResources("classpath:/static/public/templates/**/*.adl");
		
		for (Resource resource : resources) {
			String name = resource.getURI().toString();
			System.out.println("Loading: "+name);
			String localUri = name.substring(name.indexOf("/public"));
			testExampleSVG(localUri);
		}
		
	}
	
	protected byte[] loadStaticSVG(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.parseMediaType(Kite9MediaTypes.EDITABLE_SVG_VALUE)));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	
	public void testExampleSVG(String name) throws Exception {
		byte[] svg = loadStaticSVG("/public/templates/risk-first/minimal.adl?format=svg");
		persistInAFile(svg, "testExampleSVG", name+".svg");
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream("/rendering/public/testExampleSVG/"+name+".svg"), Charset.forName("UTF-8"));
		XMLCompare.compareXML(new String(svg), expected);
	}
	
}
