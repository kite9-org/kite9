package com.kite9.k9server.adl.holder.pipeline;

import java.net.URI;

import org.springframework.http.HttpHeaders;

public interface XMLBase {

	URI getUri();

	/**
	 * Returns the HTTPHeaders that were responsible for loading this 
	 * ADL.  Useful for passing around credentials.
	 */
	HttpHeaders getRequestHeaders();

	/**
	 * Returns the XML.
	 */
	String getXMLString();

}