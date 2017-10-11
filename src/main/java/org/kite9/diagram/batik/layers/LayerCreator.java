package org.kite9.diagram.batik.layers;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;

public interface LayerCreator {

	IdentifiableGraphicsNode createLayer(String id, Kite9BridgeContext ctx, StyledKite9SVGElement theElement, GraphicsLayerName layer, DiagramElement de);
}
  