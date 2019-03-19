package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.ContentTransform;

/**
 * A fixed-size element on the diagram that can contain SVG sub-elements for rendering.
 * 
 * @author robmoffat
 *
 */
public class ConnectedLeafImpl extends AbstractConnected implements Leaf {
	
	public ConnectedLeafImpl(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, LeafPainter lo, ContentTransform t) {
		super(el, parent, ctx, lo, t);
	}
}
