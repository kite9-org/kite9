package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;

import java.io.OutputStream;
import java.util.List;

/**
 * Used for Hateoas, JSON.
 *
 */
public class EntityFormat implements DiagramWriteFormat {
	
	private String extension;
	private List<MediaType> mediaTypes;

	public EntityFormat(String extension, List<MediaType> mediaTypes) {
		super();
		this.extension = extension;
		this.mediaTypes = mediaTypes;
	}
	
	@Override
	public List<MediaType> getMediaTypes() {
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
