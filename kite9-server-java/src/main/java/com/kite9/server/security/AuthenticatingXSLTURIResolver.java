package com.kite9.server.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.batik.util.ParsedURL;

/**
 * Adds authentication to the resolver process by piggybacking on the 
 * {@link AuthenticatingParsedURLHandler} functionality.
 */
public class AuthenticatingXSLTURIResolver implements URIResolver {
	
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		ParsedURL pUrl;
		
		try {
			if (base == null) {
				pUrl = new ParsedURL(href);
			} else {			
				URI base_uri = new URI(base);
				URI absolute = base_uri.resolve(href);
				pUrl = new ParsedURL(absolute.toString());
			}
		} catch (URISyntaxException e) {
			throw new TransformerException("Bad URI Syntax: "+href+" / "+base, e);
		}
		
		
		try {
			InputStream stream = pUrl.openStream();
			StreamSource out = new StreamSource(stream, pUrl.toString());
			return out;
		} catch (IOException e) {
			throw new TransformerException("Couldn't resolve: "+pUrl, e);
		}
	}
}