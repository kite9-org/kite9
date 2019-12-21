package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.ContentTransform;

public class DecalLeafImpl extends AbstractBatikDiagramElement implements Decal, Leaf {

	public DecalLeafImpl(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, LeafPainter lo, ContentTransform t) {
		super(el, parent, ctx, lo, t);
	}

	@Override
	public RenderingInformation getRenderingInformation() {
		return parent.getRenderingInformation();
	}
}
