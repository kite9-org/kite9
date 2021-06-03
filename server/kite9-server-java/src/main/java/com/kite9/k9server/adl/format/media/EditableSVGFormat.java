package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;

/**
 * Returns editable SVG.  This is where all the references to 
 * stylesheets / images etc. are preserved, and text is held as
 * text rather than as glyphs.
 * 
 * @author robmoffat
 *
 */
public class EditableSVGFormat extends AbstractSVGFormat implements EditableDiagramFormat {
	
	public MediaType[] getMediaTypes() {
		return new MediaType[] { Kite9MediaTypes.ESVG };
	}

	public String getExtension() {
		return "esvg";
	}

	@Override
	protected void setupTranscoder(Kite9SVGTranscoder t, ADLDom toWrite) {
		t.addTranscodingHint(Kite9SVGTranscoder.KEY_MEDIA, "editor");
		super.setupTranscoder(t, toWrite);
	}

	
	

}