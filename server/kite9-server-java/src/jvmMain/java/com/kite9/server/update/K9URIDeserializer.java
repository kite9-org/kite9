package com.kite9.server.update;

import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.uri.URIWrapper;

public class K9URIDeserializer extends JsonDeserializer<K9URI>{

	@Override
	public K9URI deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		try {
			String s = p.getText();
			URI u = new URI(s);
			return URIWrapper.wrap(u);
		} catch (Exception e) {
			throw JsonMappingException.from(ctxt, "Couldn't create K9URI", e);
		}
	}

}
