package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * A fixed-size element on the diagram that can contain SVG sub-elements for rendering.
 * 
 * @author robmoffat
 *
 */
public class ConnectedLeafImpl extends AbstractConnectedDiagramElement implements Leaf {
	
	public ConnectedLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}
	
}
