package org.kite9.diagram.batik.format;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.format.Kite9Transcoder;
import org.w3c.dom.Document;

/**
 * Implements the Kite9Transcoder shared interface.
 */
public class Kite9TranscoderImpl extends Kite9SVGTranscoder implements Kite9Transcoder {

    public Kite9TranscoderImpl() {
    }

    public Kite9TranscoderImpl(Cache c) {
        super(c);
    }

    @Override
    public Document transcode(Document doc) {
        try {
            TranscoderInput ti = new TranscoderInput();
            ti.setDocument(doc);
            ti.setURI(doc.getDocumentURI());
            TranscoderOutput out = new TranscoderOutput();
            transcode(ti, out);
            return out.getDocument();
        } catch (TranscoderException e) {
            throw new Kite9XMLProcessingException("Failed to transcode", e);
        }
    }

    @Override
    public void addTranscodingHint(Object key, Object value) {
        if (key instanceof TranscodingHints.Key) {
            hints.put(((TranscodingHints.Key)key), value);
        } else {
            throw new UnsupportedOperationException("Hint not understood: "+key);
        }
    }
}
