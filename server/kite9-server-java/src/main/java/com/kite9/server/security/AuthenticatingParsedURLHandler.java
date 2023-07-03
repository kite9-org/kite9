package com.kite9.server.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.batik.util.ParsedURLData;
import org.apache.batik.util.ParsedURLDefaultProtocolHandler;
import org.springframework.http.HttpHeaders;

import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.web.URIRewriter;

/**
 * This allows onward-forwarding requests to maintain the identity of the original request, 
 * assuming they go to the same server.
 * 
 * @author robmoffat
 *
 */
public class AuthenticatingParsedURLHandler extends ParsedURLDefaultProtocolHandler {


    public AuthenticatingParsedURLHandler(String protocol) {
		super(protocol);
	}

	class AuthParsedURLData extends ParsedURLData {

	    protected String cookie;
	    protected String authentication;
		
		public AuthParsedURLData() {
			super();
		}

		public AuthParsedURLData(URL url, String cookie, String authentication) {
			super(url);
			this.cookie = cookie;
			this.authentication = authentication;
		}

		@SuppressWarnings("rawtypes")
		protected InputStream openStreamInternal(String userAgent, Iterator mimeTypes, Iterator encodingTypes)
				throws IOException {
			if (stream != null)
				return stream;

			hasBeenOpened = true;

			URL url = null;
			try {
				url = buildURL();
			} catch (MalformedURLException mue) {
				throw new IOException("Unable to make sense of URL for connection");
			}

			if (url == null)
				return null;

			URLConnection urlC = url.openConnection();
			if (urlC instanceof HttpURLConnection) {
				if (userAgent != null)
					urlC.setRequestProperty(HTTP_USER_AGENT_HEADER, userAgent);

				if (mimeTypes != null) {
					String acceptHeader = "";
					while (mimeTypes.hasNext()) {
						acceptHeader += mimeTypes.next();
						if (mimeTypes.hasNext())
							acceptHeader += ",";
					}
					urlC.setRequestProperty(HTTP_ACCEPT_HEADER, acceptHeader);
				}
				
				/* Added by Rob */
				if (cookie != null) {
					urlC.setRequestProperty(HttpHeaders.COOKIE, cookie);
				}
				
				if (authentication != null) {
					urlC.setRequestProperty(HttpHeaders.AUTHORIZATION, authentication);
				}
				/* Done added by rob */
 
				if (encodingTypes != null) {
					String encodingHeader = "";
					while (encodingTypes.hasNext()) {
						encodingHeader += encodingTypes.next();
						if (encodingTypes.hasNext())
							encodingHeader += ",";
					}
					urlC.setRequestProperty(HTTP_ACCEPT_ENCODING_HEADER, encodingHeader);
				}

				contentType = urlC.getContentType();
				contentEncoding = urlC.getContentEncoding();
				postConnectionURL = urlC.getURL();
			}

			try {
				return (stream = urlC.getInputStream());
			} catch (IOException e) {
				if (urlC instanceof HttpURLConnection) {
					// bug 49889: if available, return the error stream
					// (allow interpretation of content in the HTTP error response)
					stream = ((HttpURLConnection) urlC).getErrorStream();
					if (stream == null) {
						throw e;
					}
					return stream;
				} else {
					throw e;
				}
			}

		}
	}

	/**
     * Subclasses can override these method to construct alternate 
     * subclasses of ParsedURLData.
     */
    protected ParsedURLData constructParsedURLData() {
        return new AuthParsedURLData();
    }

    /**
     * Subclasses can override these method to construct alternate 
     * subclasses of ParsedURLData.
     * @param url the java.net.URL class we reference.
     */
    protected ParsedURLData constructParsedURLData(URL url) {
    	K9URI currentURI = URIRewriter.getCompleteCurrentRequestURI();
    	String auth = null;
    	String cookie = null;
    	if (url.getHost().equals(currentURI.getHost())) {
    		HttpServletRequest request = URIRewriter.getCurrentRequest();
    		auth = request != null ? request.getHeader(HttpHeaders.AUTHORIZATION) : null;
    		cookie = request != null ? request.getHeader(HttpHeaders.COOKIE) : null;
    		
    	}
        return new AuthParsedURLData(url, cookie, auth);
    }
}
