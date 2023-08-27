package com.kite9.server.adl;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.kite9.diagram.logging.Kite9Log.Destination;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.server.XMLCompare;

public class TemplateRenderingIT extends AbstractRestIT {

	static ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	static class NameProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			Resource[] resources = resolver.getResources("classpath:/static/public/templates/**/*.adl");
			return Stream.of(resources)
				.map(n -> Arguments.of(n));
		}
		
	}
	
	protected byte[] loadStaticSVG(String page) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.parseMediaType(Kite9MediaTypes.EDITABLE_SVG_VALUE)));
		HttpEntity<Void> ent = new HttpEntity<>(headers);
		ResponseEntity<byte[]> back = getRestTemplate().exchange(new URI(getUrlBase()+page), HttpMethod.GET, ent, byte[].class);
		return back.getBody();
	}
	
	
	@ParameterizedTest
	@ArgumentsSource(NameProvider.class)
	public void testExampleSVG(Resource r) throws Exception {
		Kite9LogImpl.setLogging(Destination.OFF);
		String name = r.getURI().toString();
		name = name.substring(name.indexOf("/public"));
		System.out.println("Loading: "+name);

		byte[] svg = loadStaticSVG(name+"?format=svg");
		persistInAFile(svg, "testExampleSVG", name+".svg");
		String resourceName = "/rendering/public/testExampleSVG"+name+".svg";
		String expected = StreamUtils.copyToString(this.getClass().getResourceAsStream(resourceName), Charset.forName("UTF-8"));
		if (!StringUtils.hasLength(expected)) {
			Assertions.fail();
		}
		XMLCompare.compareXML(new String(svg), expected);
	}
	
}
