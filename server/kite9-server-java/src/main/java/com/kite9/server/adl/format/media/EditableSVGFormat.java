
package com.kite9.server.adl.format.media;

import org.kite9.diagram.batik.format.Kite9SVGTranscoder;

import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.format.Kite9Transcoder;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;

/**
 * Returns editable SVG.  This is where all the references to 
 * stylesheets / images etc. are preserved, and text is held as
 * text rather than as glyphs.
 *
 * @author robmoffat
 *
 */
public class EditableSVGFormat extends AbstractSVGFormat implements EditableDiagramFormat<Document> {

    private final List<MediaType> mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getESVG());

    public List<MediaType> getMediaTypes() {
        return mediaTypes;
    }

    public String getExtension() {
        return "esvg";
    }

    @Override
    protected void setupTranscoder(Kite9Transcoder t, ADLDom toWrite) {
        t.addTranscodingHint(Kite9SVGTranscoder.KEY_MEDIA, "editor");
        super.setupTranscoder(t, toWrite);
    }

}