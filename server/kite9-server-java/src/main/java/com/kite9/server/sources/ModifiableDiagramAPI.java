package com.kite9.server.sources;

import org.springframework.security.core.Authentication;

import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;

public interface ModifiableDiagramAPI extends ModifiableAPI, DiagramFileAPI  {
	
	/**
	 * Commits a revision when the content is an ADL Diagram,
	 */
	public void commitRevision(String message, Authentication by, ADLDom data);

}
