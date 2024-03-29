package com.kite9.server.sources;

import org.springframework.security.core.Authentication;

import com.kite9.server.update.Update;

public interface SourceAPIFactory {

	/**
	 * Returns the api for working with/editing some content or directory info.
	 * @param a 
	 */
	public SourceAPI createAPI(Update u, Authentication a) throws Exception;
	
}
