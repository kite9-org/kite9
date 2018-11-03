package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Leaf;

/**
 * For text or shape-based labels within the diagram.
 * 
 * @author robmoffat
 * 
 */
public class LabelLeafImpl extends AbstractLabel implements Label, Leaf {
	
	public LabelLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, LeafPainter lo) {
		super(el, parent, ctx, lo);
	}
}