package com.kite9.server.sources;

import java.io.InputStream;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.domain.RestEntity;

public interface SourceAPI {
	
	/**
	 * Instead of returning actual content, returns an entity representation of it, for directory listings and so on.
	 */
	public RestEntity getEntityRepresentation(Authentication a) throws Exception;

	enum SourceType {
		FILE, DIRECTORY
	}
	
	/**
	 * Determines whether this is a file or a directory we are looking at.
	 */
	public SourceType getSourceType(Authentication a) throws Exception;
	
	/**
	 * Return bytes for current file (if source type is file)
	 */
	public InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception;

	/**
	 * Returns the underlying media type required for storing the file, 
	 * as dictated by the file's extension.  
	 * Returns null for directories / anything unknown
	 */
	public K9MediaType getMediaType();
	
	/**
	 * Returns the https://kite9.org URI of the resource being 
	 * accessed.  All query parameters are removed.
	 */
	public K9URI getKite9ResourceURI();
}
