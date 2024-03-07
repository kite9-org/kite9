package com.kite9.server.sources;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;

public interface ModifiableDiagramAPI extends ModifiableAPI, DiagramAPI  {
	
	/**
	 * Commits a revision when the content is an ADL Diagram.
	 */
	default void commitRevision(String message, Authentication by, ADLDom data) throws Exception {
		checkUserCanWrite(by);
		DiagramWriteFormat df = getWriteFormat();
		if (df != null) {
			ADLOutput out = data.process(getUnderlyingResourceURI(by), df);
			commitRevisionAsBytes(message, by, out.getAsBytes());
		}
	}

	public DiagramWriteFormat getWriteFormat();
}
