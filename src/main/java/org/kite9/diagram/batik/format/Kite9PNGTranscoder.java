package org.kite9.diagram.batik.format;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * This is a massive, horrible hack - we end up creating the doc twice.  Isn't there a better way?
 */
public class Kite9PNGTranscoder extends Kite9SVGTranscoder {

	

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
		PNGTranscoder png = new PNGTranscoder();
		png.setTranscodingHints(this.getTranscodingHints());
		TranscoderInput inter2 = new TranscoderInput(svg);
		png.transcode(inter2, output);
	}
}

