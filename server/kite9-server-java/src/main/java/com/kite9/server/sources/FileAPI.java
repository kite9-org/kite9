package com.kite9.server.sources;

import java.io.InputStream;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.K9MediaType;

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
	InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception;

	/**
	 * Returns the underlying media type required for storing the file, 
	 * as dictated by the file's extension.
	 */
	K9MediaType getMediaType();
	
}
