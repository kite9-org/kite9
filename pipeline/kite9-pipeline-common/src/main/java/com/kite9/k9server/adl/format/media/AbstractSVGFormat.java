package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.ADLFactoryImpl;
import com.kite9.k9server.adl.holder.meta.Payload;
import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.processors.copier.BasicCopier;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.net.URI;

public abstract class AbstractSVGFormat implements DiagramFormat {

	@Override
	public void handleWrite(ADLDom toWrite, OutputStream baos, Kite9SVGTranscoder t) throws Exception {
		setupTranscoder(t, toWrite);
		Document d = transformADL(toWrite.getDocument(), toWrite.getUri(), t, toWrite);
		ADLFactoryImpl.duplicate(d, isOmitDeclaration(), new StreamResult(baos));
		
	}

	protected boolean isOmitDeclaration() {
		return false;
	}


	protected void setupTranscoder(Kite9SVGTranscoder t, ADLDom toWrite) {
		t.addTranscodingHint(Kite9SVGTranscoder.KEY_ENCAPSULATING, false);
	}


	protected Document transformADL(Document d, URI uri, Kite9SVGTranscoder transcoder, ADLDom meta) {
		try {
			ADLDocument copy = ADLFactoryImpl.createNewDocument(transcoder);
			BasicCopier bc = new BasicCopier(copy, false);
			bc.processContents(d);
			TranscoderInput in = new TranscoderInput(copy);
			in.setURI(processURI(uri));
			TranscoderOutput out = new TranscoderOutput();
			transcoder.transcode(in, out);
			Document svgOut = out.getDocument();
			Payload.insertADLInformationIntoSVG(meta, svgOut);
			return svgOut;
		} catch (TranscoderException e) {
			throw new Kite9XMLProcessingException("Couldn't get SVG Representation", e, d);
		}
	}

	
	public String processURI(URI in) {
		return in.toString();
	}
	
}
