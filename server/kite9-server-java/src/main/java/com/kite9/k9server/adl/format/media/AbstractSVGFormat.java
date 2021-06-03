
package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.ADLFactoryImpl;
import com.kite9.k9server.adl.holder.meta.Payload;
import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.uri.URI;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.format.Kite9Transcoder;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;

;

public abstract class AbstractSVGFormat implements DiagramStreamWriteFormat<Document> {

    @Override
    public void handleWrite(ADLDom toWrite, OutputStream baos, Kite9SVGTranscoder t) throws Exception {
        Document d = handleWrite(toWrite, t);
        ADLFactoryImpl.duplicate(d, isOmitDeclaration(), new StreamResult(baos));
    }

    @Override
    public Document handleWrite(ADLDom toWrite, Kite9Transcoder<Document> t) {
        setupTranscoder(t, toWrite);
        return transformADL(toWrite.getDocument(), toWrite.getUri(), t, toWrite);
    }

    protected boolean isOmitDeclaration() {
        return false;
    }


    protected void setupTranscoder(Kite9Transcoder t, ADLDom toWrite) {
        t.addTranscodingHint(Kite9SVGTranscoder.KEY_ENCAPSULATING, false);
    }


    protected Document transformADL(Document d, URI uri, Kite9Transcoder<Document> transcoder, ADLDom meta) {
        try {
            d.setDocumentURI(uri.toString());
            Document svgOut = transcoder.transcode(d);
            Payload.insertADLInformationIntoSVG(meta, svgOut);
            return svgOut;
        } catch (Exception e) {
            throw new Kite9XMLProcessingException("Couldn't get SVG Representation", e, d);
        }
    }
}
