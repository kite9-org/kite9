
package com.kite9.server.adl.format.media;

import java.util.Collections;
import java.util.List;

import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.format.Kite9Transcoder;

import com.kite9.pipeline.adl.format.media.EditableDiagramFormat;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;

/**
 * Returns editable SVG.  This is where all the references to 
 * stylesheets / images etc. are preserved, and text is held as
 * text rather than as glyphs.
 *
 * @author robmoffat
 *
 */
public class EditableSVGFormat extends AbstractSVGFormat implements EditableDiagramFormat {

    private final List<K9MediaType> mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getESVG());

    public EditableSVGFormat(XMLHelper helper) {
        super(helper);
    }

    public List<K9MediaType> getMediaTypes() {
        return mediaTypes;
    }

    public String getExtension() {
        return "esvg";
    }

    @Override
    protected void setupTranscoder(Kite9Transcoder t, ADLDom toWrite) {
    	if (!t.hasTranscodingHint(Kite9SVGTranscoder.KEY_MEDIA)) {
    		t.addTranscodingHint(Kite9SVGTranscoder.KEY_MEDIA, "editor");
    	}
        super.setupTranscoder(t, toWrite);
    }

}