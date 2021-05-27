package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;

import java.io.OutputStream;

/**
 * Used for Hateoas, JSON.
 *
 */
public class EntityFormat implements DiagramFormat {
	
	private String extension;
	private MediaType[] mediaTypes;

	public EntityFormat(String extension, MediaType[] mediaTypes) {
		super();
		this.extension = extension;
		this.mediaTypes = mediaTypes;
	}
	
	@Override
	public MediaType[] getMediaTypes() {
		return mediaTypes;
	}

	@Override
	public void handleWrite(ADLDom input, OutputStream baos, Kite9SVGTranscoder t) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getExtension() {
		return extension;
	}

}
