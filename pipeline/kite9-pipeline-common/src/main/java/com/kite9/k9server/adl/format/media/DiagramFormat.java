package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;

import java.io.OutputStream;

/**
 * Marks the Format as being one which can express a rendered diagram.
 * 
 * @author robmoffat
 *
 */
public interface DiagramFormat extends Format {

	public void handleWrite(ADLDom toWrite, OutputStream baos, Kite9SVGTranscoder t) throws Exception;
	
}
