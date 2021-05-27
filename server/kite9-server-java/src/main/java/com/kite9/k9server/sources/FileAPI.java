package com.kite9.k9server.sources;

import java.io.InputStream;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

/**
 * Source is a file.
 * 
 * @author robmoffat
 *
 */
public interface FileAPI extends SourceAPI {
	
	/**
	 * Return bytes for current file.
	 */
	public InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception;

	/**
	 * Returns the underlying media type required for storing the file, 
	 * as dictated by the file's extension.
	 */
	public MediaType getMediaType();
	
}
