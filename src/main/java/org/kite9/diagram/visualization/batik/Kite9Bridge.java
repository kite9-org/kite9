package org.kite9.diagram.visualization.batik;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.SVGRectElementBridge;

public class Kite9Bridge extends SVGRectElementBridge {

	@Override
	public String getNamespaceURI() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public Bridge getInstance() {
		return new Kite9Bridge();
	}

}
