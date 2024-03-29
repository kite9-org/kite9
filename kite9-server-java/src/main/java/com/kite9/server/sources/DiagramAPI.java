package com.kite9.server.sources;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.pipeline.ADLBase;

/**
 * Means that the file contents can be understood as ADL.
 * 
 * @author robmoffat
 *
 */
public interface DiagramAPI extends SourceAPI {

	/**
	 * Returns the contents of the file as a renderable pipeline.
	 */
	public ADLBase getCurrentRevisionContent(Authentication authentication, HttpHeaders requestHeaders) throws Exception;
	
}
