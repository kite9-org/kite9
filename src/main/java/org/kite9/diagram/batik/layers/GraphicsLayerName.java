package org.kite9.diagram.batik.layers;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;

public enum GraphicsLayerName {

	BACKGROUND(new NoopLayerCreator()), 
	SHADOW(new ShadowLayerCreator()), 
	MAIN(new MainLayerCreator()), 
	FLANNEL(new NoopLayerCreator()), 
	WATERMARK(new NoopLayerCreator()), 
	COPYRIGHT(new NoopLayerCreator()), 
	DEBUG(new NoopLayerCreator());
	
	private LayerCreator lc;
	
	GraphicsLayerName(LayerCreator lc) {
		this.lc = lc;
	}

	public IdentifiableGraphicsNode createLayer(String id, Kite9BridgeContext ctx, StyledKite9SVGElement theElement, DiagramElement de) {
		return lc.createLayer(id, ctx, theElement, this, de);
	}
	
}
