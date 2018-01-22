package org.kite9.diagram.batik.format;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.element.DiagramElementFactoryImpl;
import org.kite9.diagram.model.style.DiagramElementFactory;
import org.kite9.framework.dom.ADLExtensibleDOMImplementation;
import org.kite9.framework.dom.Kite9DocumentFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

public class Kite9PNGTranscoder extends PNGTranscoder {

	private ADLExtensibleDOMImplementation domImpl;
	
	public Kite9PNGTranscoder() {
		super();
		TranscodingHints hints = new TranscodingHints();
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
		hints.put(XMLAbstractTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, ADLExtensibleDOMImplementation.SVG_NAMESPACE_URI);
		domImpl = new ADLExtensibleDOMImplementation();
		hints.put(XMLAbstractTranscoder.KEY_DOM_IMPLEMENTATION, domImpl);
		setTranscodingHints(hints);
	}
	
	protected Kite9DocumentFactory createDocumentFactory() {
		return createDocumentFactory(domImpl, XMLResourceDescriptor.getXMLParserClassName());
	}
	
	@Override
	protected Kite9DocumentFactory createDocumentFactory(DOMImplementation domImpl, String parserClassname) {
		return new Kite9DocumentFactory((ADLExtensibleDOMImplementation) domImpl, parserClassname);
	}
	
//	@Override
//	protected BridgeContext createBridgeContext(SVGOMDocument doc) {
//		Kite9BridgeContext out = new Kite9BridgeContext(userAgent, createDocumentFactory());
//		DiagramElementFactory def = new DiagramElementFactoryImpl(out);
//		domImpl.setDiagramElementFactory(def);
//		return out;
//	}

	@Override
	protected void transcode(Document document, String uri, TranscoderOutput output) throws TranscoderException {
		document.setDocumentURI(uri);
		super.transcode(document, uri, output);
	}
}

