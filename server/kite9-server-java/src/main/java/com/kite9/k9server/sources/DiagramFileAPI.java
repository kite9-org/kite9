package com.kite9.k9server.sources;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import com.kite9.k9server.adl.holder.meta.MetaReadWrite;
import com.kite9.k9server.adl.holder.pipeline.ADLBase;

/**
 * Means that the file contents can be understood as ADL.
 * 
 * @author robmoffat
 *
 */
public interface DiagramFileAPI extends FileAPI {

	/**
	 * Returns the contents of the file as a renderable pipeline.
	 */
	public ADLBase getCurrentRevisionContent(Authentication authentication, HttpHeaders requestHeaders) throws Exception;
	
	/**
	 * Callback function to set api-specific metadata on the ADL diagram.
	 */
	public void addMeta(MetaReadWrite adl);
	
}
