package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;

public final class PDFFormat extends AbstractSVGFormat implements DiagramFileFormat {

	public MediaType[] getMediaTypes() {
		return new MediaType[] { Kite9MediaTypes.PDF };
	}

	public String getExtension() {
		return "pdf";
	}

	@Override
	public boolean isBinaryFormat() {
		return true;
	}

	@Override
	protected void setupTranscoder(Kite9SVGTranscoder t, ADLDom toWrite) {
		throw new UnsupportedOperationException();		// not implemented
	}
	
}