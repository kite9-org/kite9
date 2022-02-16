package org.kite9.diagram.batik.bridge;

import java.util.Collections;
import java.util.Iterator;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeExtension;
import org.w3c.dom.Element;

public class Kite9BridgeExtensions implements BridgeExtension {

	@Override
	public float getPriority() {
		return 1;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getImplementedExtensions() {
        return Collections.EMPTY_LIST.iterator();
	}

	@Override
	public String getAuthor() {
		return "Kite9";
	}

	@Override
	public String getContactAddress() {
		return "kite9.com";
	}

	@Override
	public String getURL() {
		return "http://kite9.com";
	}

	@Override
	public String getDescription() {
		return "Replaces some built-in Batik functionality, to fix bugs mainly";
	}

	@Override
	public void registerTags(BridgeContext ctx) {
		ctx.putBridge(new FixedSVGImageElementBridge());
		ctx.putBridge(new FixedSVGMultiImageElementBridge());
	}

	@Override
	public boolean isDynamicElement(Element e) {
		return false;
	}

}
