package com.kite9.server.adl.format.media;

import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.NotKite9DiagramException;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.adl.holder.ADLOutputImpl;
import com.kite9.server.adl.holder.meta.Payload;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.uri.K9URI;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.xmlgraphics.image.codec.png.PNGDecodeParam;
import org.apache.xmlgraphics.image.codec.png.PNGImageDecoder;
import org.kite9.diagram.batik.format.ADLEmbeddingPNGTranscoder;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.http.HttpHeaders;
import org.w3c.dom.Document;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class PNGFormat extends AbstractSVGFormat implements DiagramFileFormat {

	private ADLFactory factory;
	private List<K9MediaType> mediaTypes;
	
	public PNGFormat(ADLFactory factory) {
		this.factory = factory;
		this.mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getPNG());
	}
	
	public List<K9MediaType> getMediaTypes() {
		return mediaTypes;
	}

	/**
	 * This is probably horribly inefficient, as I think lots of resources get loaded twice.
	 * USed to use PNGTranscoderInternalCodecWriteAdapter, but now inlined to add the text.
	 */
	public ADLOutput handleWrite(ADLDom toWrite, Kite9Transcoder t)  {
		setupTranscoder(t, toWrite);
		Kite9SVGTranscoder svgt = (Kite9SVGTranscoder) t;
		Document doc = transformADL(toWrite.getDocument(), toWrite.getUri(), t, toWrite);
		String uri = toWrite.getUri().toString();
		PNGTranscoder png = new ADLEmbeddingPNGTranscoder(svgt.getDocLoader(), Payload.createBase64ADLString(toWrite)) {

			@Override
			protected UserAgent createUserAgent() {
				return svgt.getUserAgent();
			}
			
		};

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		png.setTranscodingHints(svgt.getTranscodingHints());
		doc.setDocumentURI(uri);
		TranscoderInput in = new TranscoderInput(doc);
		in.setURI(uri);
		TranscoderOutput out = new TranscoderOutput(baos);
		try {
			png.transcode(in, out);
			return new ADLOutputImpl(this, toWrite, baos.toByteArray(), null, doc);
		} catch (TranscoderException e) {
			throw new Kite9XMLProcessingException("Couldn't convert to png", e);
		}
	}

	public String getExtension() {
		return "png";
	}

	@Override
	public boolean isBinaryFormat() {
		return true;
	}

	@Override
	public ADLBase handleRead(InputStream someFormat, K9URI in, HttpHeaders headers) throws Exception {
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