package com.kite9.server.adl.holder;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.AbstractADLOutput;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.w3c.dom.Document;

public class ADLOutputImpl extends AbstractADLOutput {

    private byte[] bytes;
    private Document document;

    public ADLOutputImpl(DiagramWriteFormat format,
                         ADLDom orig,
                         byte[] bytes,
                         Document document) {
        super(format, orig);
        this.bytes = bytes;
        this.document = document;
    }

    @Override
    public byte[] getAsBytes() throws Kite9ProcessingException {
        return bytes;
    }

    @Override
    public Document getAsDocument() {
        return document;
    }

}