package org.kite9.diagram.batik.bridge;

import org.apache.batik.bridge.SVGGElementBridge;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;

/**
 * Extends the regular &lt;g&gt; element bridge so that it generates {@link IdentifiableGraphicsNode}s.
 * 
 * @author robmoffat
 *
 */
public class Kite9GBridge extends SVGGElementBridge {

	public Kite9GBridge() {
		super();
	}

	@Override
	protected IdentifiableGraphicsNode instantiateGraphicsNode() {
		return new IdentifiableGraphicsNode();
	}

}