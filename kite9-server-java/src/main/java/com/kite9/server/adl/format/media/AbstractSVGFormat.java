
package com.kite9.server.adl.format.media;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.stream.StreamResult;

import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.format.Kite9Transcoder;
import org.w3c.dom.Document;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.holder.ADLOutputImpl;
import com.kite9.server.adl.holder.meta.Payload;

public abstract class AbstractSVGFormat implements DiagramWriteFormat, RESTWriteFormat {

    private final XMLHelper xmlHelper;

    public AbstractSVGFormat(XMLHelper helper) {
        this.xmlHelper = helper;
    }

    public ADLOutput handleWrite(ADLDom toWrite, Kite9Transcoder t) {
        setupTranscoder(t, toWrite);
        Document d = transformADL(toWrite.getDocument(), toWrite.getUri(), t, toWrite);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(baos);
        xmlHelper.duplicate(d, isOmitDeclaration(), sr);
        return new ADLOutputImpl(this, toWrite, baos.toByteArray(), d);
    }

    protected boolean isOmitDeclaration() {
        return false;
    }


    protected void setupTranscoder(Kite9Transcoder t, ADLDom toWrite) {
    	if (!t.hasTranscodingHint(Kite9SVGTranscoder.KEY_ENCAPSULATING)) {
    		t.addTranscodingHint(Kite9SVGTranscoder.KEY_ENCAPSULATING, false);
    	}
    }

    protected Document transformADL(Document d, K9URI uri, Kite9Transcoder transcoder, ADLDom meta) {
        try {
            d.setDocumentURI(uri.toString());
            Document svgOut = transcoder.transcode(d);
            Payload.insertADLInformationIntoSVG(meta, svgOut);
            return svgOut;
        } catch (Exception e) {
            throw new Kite9XMLProcessingException("Couldn't get SVG Representation", e, d);
        }
    }


    public String processURI(K9URI in) {
        return in.toString();
    }

    @Override
    public String getFormatIdentifier() {
        return getMediaTypes().get(0).toString();
    }
}
