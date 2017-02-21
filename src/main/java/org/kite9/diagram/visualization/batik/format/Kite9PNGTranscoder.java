package org.kite9.diagram.visualization.batik.format;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.framework.serialization.Kite9DocumentFactory;
import org.w3c.dom.DOMImplementation;

public final class Kite9PNGTranscoder extends PNGTranscoder {
	
	@Override
	protected BridgeContext createBridgeContext(SVGOMDocument doc) {
		return new Kite9BridgeContext(userAgent);
	}

	@Override
	protected DocumentFactory createDocumentFactory(DOMImplementation domImpl, String parserClassname) {
		return new Kite9DocumentFactory(parserClassname);
	}

    

}