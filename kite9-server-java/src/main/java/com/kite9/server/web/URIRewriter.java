package com.kite9.server.web;

import java.net.URI;
import java.net.URISyntaxException;

import jakarta.servlet.http.HttpServletRequest;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.uri.URIWrapper;

/**
 * Some functions for returning the URI for a given request.
 * 
 * @author robmoffat
 *
 */
public final class URIRewriter {

	public static boolean localPublicContent(URI uri) throws URISyntaxException {
		String ourHost = getCompleteCurrentRequestURI().getHost();
		return (ourHost == null) || (uri.getHost() == null) || ourHost.equals(uri.getHost());
	}

	public static boolean localPublicContent(K9URI uri) {
		String ourHost = getCompleteCurrentRequestURI().getHost();
		return (ourHost == null) || (uri.getHost() == null) || ourHost.equals(uri.getHost());
	}

	public static K9URI getCompleteCurrentRequestURI() {
		HttpServletRequest req = getCurrentRequest();

		try {
			if (req == null) {
				return URIWrapper.wrap(new URI("http://localhost/unknown"));
			} else {
				String qs = req.getQueryString();
				qs = qs == null ? "" : "?" + qs;
				String main = req.getRequestURL().toString();

				return URIWrapper.wrap(new URI(main + qs));
			}
		} catch (URISyntaxException e) {
			throw new Kite9XMLProcessingException("Couldn't determine URI of current request", e);
		}
	}

	public static HttpServletRequest getCurrentRequest() {
		try {
			RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
			if (attrs == null) {
				return null;
			}
			return ((ServletRequestAttributes) attrs).getRequest();
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't determine current request", e);
		}
	}

	public static K9URI resolve(String url) throws URISyntaxException {
		K9URI currentRequestURI = getCompleteCurrentRequestURI();
		if (currentRequestURI != null) {
			return currentRequestURI.resolve(url);
		} else {
			return URIWrapper.wrap(new URI(url));
		}
	}

}
