package com.kite9.server.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.springframework.http.HttpHeaders;

import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.web.URIRewriter;

/**
 * Adds authorization to the resolver process, inspired by Florent Georges'
 * implementation.
 */
public class AuthenticatingXSLTURIResolver implements URIResolver {

	private URIResolver original;

	public AuthenticatingXSLTURIResolver(URIResolver original) {
		this.original = original;
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		// Get the resolved URI
		URI absolute = null;
		try {
			if (base == null) {
				absolute = new URI(href);
			} else {
				URI base_uri = new URI(base);
				absolute = base_uri.resolve(href);
			}
		} catch (URISyntaxException ex) {
			throw new TransformerException(ex);
		}
		
		// Set proxy if it is an HTTP resource, or delegate
		if (absolute.getScheme().startsWith("http")) {
			return resolve(absolute);
		} else {
			return original.resolve(href, base);
		}
	}

	private Source resolve(URI uri) throws TransformerException {
		// open the connection
		HttpURLConnection conn = null;
		try {
			URL url = uri.toURL();
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
		} catch (MalformedURLException ex) {
			throw new TransformerException(ex);
		} catch (IOException ex) {
			throw new TransformerException(ex);
		}
		
		K9URI currentURI = URIRewriter.getCompleteCurrentRequestURI();
    	String auth = null;
    	String cookie = null;
    	if (uri.getHost().equals(currentURI.getHost())) {
    		HttpServletRequest request = URIRewriter.getCurrentRequest();
    		auth = request != null ? request.getHeader(HttpHeaders.AUTHORIZATION) : null;
    		cookie = request != null ? request.getHeader(HttpHeaders.COOKIE) : null;
    	}
		
		// actually send the request
		try {
			conn.connect();
			if (auth != null) {
				conn.setRequestProperty(HttpHeaders.AUTHORIZATION, auth);
			}
			if (cookie != null) {
				conn.setRequestProperty(HttpHeaders.COOKIE, cookie);
			}
		} catch (IOException ex) {
			throw new TransformerException(ex);
		}
		// disconnect
		conn.disconnect();
		// get the response stream
		try {
			InputStream stream = conn.getInputStream();
			return new StreamSource(stream);
		} catch (IOException ex) {
			throw new TransformerException(ex);
		}
	}

}
