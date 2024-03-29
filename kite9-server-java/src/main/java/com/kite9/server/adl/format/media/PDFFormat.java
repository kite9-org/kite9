package com.kite9.server.adl.format.media;

import java.util.Collections;
import java.util.List;

import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.format.Kite9Transcoder;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;

public final class PDFFormat extends AbstractSVGFormat implements DiagramFileFormat {

	private final List<K9MediaType> mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getPDF());

	public PDFFormat(XMLHelper helper) {
		super(helper);
	}

	public List<K9MediaType> getMediaTypes() {
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