package com.kite9.server.adl.format.media;

import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.format.Kite9Transcoder;

import java.util.Collections;
import java.util.List;

public final class PDFFormat extends AbstractSVGFormat implements DiagramFileFormat {

	private final List<MediaType> mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getPDF());

	public List<MediaType> getMediaTypes() {
		return mediaTypes;
	}


	public String getExtension() {
		return "pdf";
	}

	@Override
	public boolean isBinaryFormat() {
		return true;
	}

	@Override
	protected void setupTranscoder(Kite9Transcoder t, ADLDom toWrite) {
		throw new UnsupportedOperationException();		// not implemented
	}
	
}