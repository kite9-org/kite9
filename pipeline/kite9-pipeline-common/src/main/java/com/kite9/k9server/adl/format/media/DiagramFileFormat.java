package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.pipeline.ADLBase;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * This interface marks this format as one with which we can persist diagrams into files.
 * 
 * @author robmoffat
 *
 */
public interface DiagramFileFormat extends DiagramFormat {

	/**
	 * This is used to determine how we are going to write back to github - should we commit as text or binary.
	 */
	public boolean isBinaryFormat();
	
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
			URL url = in.toURL();
			stream = url.openStream();
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Couldn't load doc " + in, e);
		}

		return handleRead(stream, in, headers);

	}
}
