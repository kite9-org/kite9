package com.kite9.k9server.adl.format.media;

/**
 * Handles sending a certain file format to the output stream for http
 * responses.
 * 
 * @author robmoffat
 */
public interface Format {
	
	/**
	 * A canonical media type, used by Kite9 server consistently.
	 * Used for caching.
	 */
	public default String getFormatIdentifier() {
		return getMediaTypes()[0].toString();
	}

	public MediaType[] getMediaTypes();

	/**
	 * In order to use format=xxx in the URL, we need to give each format an extension.
	 */
	public String getExtension();
	
	
}
