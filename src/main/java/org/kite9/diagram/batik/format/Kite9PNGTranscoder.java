package org.kite9.diagram.batik.format;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

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
		
		PNGTranscoder png = new PNGTranscoder();
		TranscoderInput inter2 = new TranscoderInput(inter1.getDocument());
		png.transcode(inter2, output);
	}
	
	
}

