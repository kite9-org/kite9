package com.kite9.server.sources;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.pipeline.ADLDom;

public interface ModifiableDiagramAPI extends ModifiableAPI, DiagramAPI  {
	
	/**
	 * Commits a revision when the content is an ADL Diagram,
	 */
	void commitRevision(String message, Authentication by, ADLDom data);

}
