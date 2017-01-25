package org.kite9.diagram.visualization.batik;

import java.util.Collections;
import java.util.Iterator;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeExtension;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.Element;

public class Kite9BridgeExtension implements BridgeExtension {

	private BridgeContext ctx;
	
	@Override
	public float getPriority() {
		return 1f;
	}

	@Override
	public Iterator getImplementedExtensions() {
		return Collections.emptyIterator();
	}

	@Override
	public String getAuthor() {
		return "Kite9";
	}

	@Override
	public String getContactAddress() {
		return "rob@kite9.com";
	}

	@Override
	public String getURL() {
		return "http://kite9.com";
	}

	@Override
	public String getDescription() {
		return "Special handling of the Kite9 Namespace: "+XMLHelper.KITE9_NAMESPACE;
	}

	@Override
	public void registerTags(BridgeContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isDynamicElement(Element e) {
		return false;   // maybe we can change it later
	}

}
