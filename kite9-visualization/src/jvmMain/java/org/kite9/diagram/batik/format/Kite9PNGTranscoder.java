package org.kite9.diagram.batik.format;

import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.w3c.dom.Document;

/**
 * This is a massive, horrible hack - we end up creating the doc twice.  Isn't there a better way?
 * 
 * Only used for testing.
 */
public class Kite9PNGTranscoder extends Kite9SVGTranscoder {

	public Kite9PNGTranscoder() {
		super(Cache.NO_CACHE, new XMLHelper());
	}

	@Override
	public String getPrefix() {
		return "KPNG";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	@Override
	public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
		TranscoderOutput inter1 = new TranscoderOutput();
		super.transcode(input, inter1);
		Document svg = inter1.getDocument();
		svg.setDocumentURI(input.getURI());
		UserAgent localUserAgent = userAgent;
		ADLEmbeddingPNGTranscoder png = new ADLEmbeddingPNGTranscoder(getDocLoader(), "") {

			@Override
			protected UserAgent createUserAgent() {
				return localUserAgent;
			}
			
		};
		png.setTranscodingHints(this.getTranscodingHints());
		TranscoderInput inter2 = new TranscoderInput(svg);
		inter2.setURI(input.getURI());
		png.transcode(inter2, output);
	}
}

