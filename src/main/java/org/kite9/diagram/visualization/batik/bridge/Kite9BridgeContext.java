package org.kite9.diagram.visualization.batik.bridge;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.script.InterpreterPool;


public final class Kite9BridgeContext extends SVG12BridgeContext {

	public Kite9BridgeContext(UserAgent userAgent, DocumentLoader loader) {
		super(userAgent, loader);
	}

	public Kite9BridgeContext(UserAgent userAgent, InterpreterPool interpreterPool, DocumentLoader documentLoader) {
		super(userAgent, interpreterPool, documentLoader);
	}

	public Kite9BridgeContext(UserAgent userAgent) {
		super(userAgent);
	}

	public boolean isInteractive() {
		return false;
	}

	public boolean isDynamic() {
		return false;
	}
	

	@Override
	public void registerSVGBridges() {
		super.registerSVGBridges();
		putBridge(new Kite9DiagramGroupBridge(this));
		putBridge(new Kite9GBridge());
		putBridge(new TextBridge());
	}
	
	
	
}