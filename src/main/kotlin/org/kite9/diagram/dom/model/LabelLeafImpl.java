package org.kite9.diagram.dom.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.painter.LeafPainter;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.ContentTransform;

/**
 * For text or shape-based labels within the diagram.
 * 
 * @author robmoffat
 * 
 */
public class LabelLeafImpl extends AbstractLabel implements Label, Leaf {
	
	public LabelLeafImpl(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, LeafPainter lo, ContentTransform t) {
		super(el, parent, ctx, lo, t);
	}
	
	
}