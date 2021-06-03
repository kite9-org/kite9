package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.ADLFactory;
import com.kite9.k9server.adl.holder.meta.Payload;
import com.kite9.k9server.adl.holder.pipeline.ADLBase;
import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.xmlgraphics.image.codec.png.PNGDecodeParam;
import org.apache.xmlgraphics.image.codec.png.PNGImageDecoder;
import org.kite9.diagram.batik.format.ADLEmbeddingPNGTranscoder;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.springframework.http.HttpHeaders;
import org.w3c.dom.Document;

import java.awt.image.RenderedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Base64;

public class PNGFormat extends AbstractSVGFormat implements DiagramFileFormat {

	private ADLFactory factory;
	
	public PNGFormat(ADLFactory factory) {
		this.factory = factory;
	}
	
	public MediaType[] getMediaTypes() {
		return new MediaType[] { MediaType.IMAGE_PNG };
	}

	/**
	 * This is probably horribly inefficient, as I think lots of resources get loaded twice.
	 * USed to use PNGTranscoderInternalCodecWriteAdapter, but now inlined to add the text.
	 */
	public void handleWrite(ADLDom toWrite, OutputStream baos, Kite9SVGTranscoder t) throws Exception {
		setupTranscoder(t, toWrite);
		Document doc = transformADL(toWrite.getDocument(), toWrite.getUri(), t, toWrite);
		String uri = toWrite.getUri().toString();
		PNGTranscoder png = new ADLEmbeddingPNGTranscoder(t.getDocLoader(), Payload.createBase64ADLString(toWrite)) {

			@Override
			protected UserAgent createUserAgent() {
				return t.getUserAgent();
			}
			
		};
		
		png.setTranscodingHints(t.getTranscodingHints());
		doc.setDocumentURI(uri);
		TranscoderInput in = new TranscoderInput(doc);
		in.setURI(uri);
		TranscoderOutput out = new TranscoderOutput(baos);
		png.transcode(in, out);
	}



	public String getExtension() {
		return "png";
	}

	@Override
	public boolean isBinaryFormat() {
		return true;
	}

	@Override
	public ADLBase handleRead(InputStream someFormat, URI in, HttpHeaders headers) throws Exception {
		PNGImageDecoder dec = new PNGImageDecoder(someFormat, new PNGDecodeParam());
		RenderedImage ri = dec.decodeAsRenderedImage();
		String base64 = (String) ri.getProperty("text_0:kite-adl");
		if (base64 == null) {
			throw new NotKite9DiagramException("Couldn't find ADL within file: "+in+".  Is this definitely a Kite9 diagram?");
		}

		String decoded = new String(Base64.getDecoder().decode(base64));
		return factory.adl(in, decoded, headers);
	}
	
}