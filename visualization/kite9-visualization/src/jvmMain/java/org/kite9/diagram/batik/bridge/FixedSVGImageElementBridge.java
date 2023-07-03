package org.kite9.diagram.batik.bridge;

import java.io.IOException;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.SVGImageElementBridge;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;

public class FixedSVGImageElementBridge extends SVGImageElementBridge {

	public FixedSVGImageElementBridge() {
		super();
	}

	/** 
	 * Ensures open stream always fails with IOException.
	 */
	@Override
	protected ProtectedStream openStream(Element e, ParsedURL purl) throws IOException {
		try {
			return super.openStream(e, purl);
		} catch (Exception e1) {
			throw new IOException("Null opening stream from "+purl.toString(), e1);
		}
	}

	@Override
	public Bridge getInstance() {
		return new FixedSVGImageElementBridge();
	}
}
