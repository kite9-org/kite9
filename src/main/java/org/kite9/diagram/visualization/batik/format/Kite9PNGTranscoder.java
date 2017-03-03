package org.kite9.diagram.visualization.batik.format;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.kite9.diagram.style.DiagramElementFactory;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.visualization.batik.element.DiagramElementFactoryImpl;
import org.kite9.framework.serialization.ADLExtensibleDOMImplementation;
import org.kite9.framework.serialization.Kite9DocumentFactory;
import org.w3c.dom.DOMImplementation;

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
	
	@Override
	protected DocumentFactory createDocumentFactory(DOMImplementation domImpl, String parserClassname) {
		return new Kite9DocumentFactory((ADLExtensibleDOMImplementation) domImpl, parserClassname);
	}
	
	@Override
	protected BridgeContext createBridgeContext(SVGOMDocument doc) {
		Kite9BridgeContext out = new Kite9BridgeContext(userAgent);
		DiagramElementFactory def = new DiagramElementFactoryImpl(out);
		domImpl.setDiagramElementFactory(def);
		return out;
	}

}
