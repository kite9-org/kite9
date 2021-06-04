package com.kite9.server.adl.format.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.server.pipeline.uri.URI;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.http.HttpHeaders;

import com.kite9.server.pipeline.adl.holder.pipeline.ADLBase;
import org.w3c.dom.Document;

/**
 * This interface marks this format as one with which we can persist diagrams into files.
 *
 * @author robmoffat
 *
 */
public interface DiagramFileFormat extends DiagramWriteFormat<Document> {

    /**
     * This is used to determine how we are going to write back to github - should we commit as text or binary.
     */
    public boolean isBinaryFormat();

    public void handleWrite(ADLDom toWrite, OutputStream baos, Kite9Transcoder<Document> t);

    /**
     * This knows how to pull back the original ADL from the format.
     */
    public default ADLBase handleRead(InputStream someFormat, URI in, HttpHeaders headers) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * As above, but loads the input stream from the uri.
     */
    public default ADLBase handleRead(URI in, HttpHeaders headers) throws Exception {
        InputStream stream;
        try {
            URL url = new URL(in.toString());
            stream = url.openStream();
        } catch (IOException e) {
            throw new Kite9XMLProcessingException("Couldn't load doc " + in, e);
        }

        return handleRead(stream, in, headers);

    }
}
