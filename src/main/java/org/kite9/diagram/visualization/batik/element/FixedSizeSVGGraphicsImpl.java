package org.kite9.diagram.visualization.batik.element;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.sizing.FixedSizeGraphics;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.xml.StyledKite9SVGElement;

/**
 * A fixed-size element on the diagram that can contain SVG sub-elements for rendering.
 * 
 * @author robmoffat
 *
 */
public class FixedSizeSVGGraphicsImpl extends AbstractConnectedDiagramElement implements FixedSizeGraphics {
	
	public FixedSizeSVGGraphicsImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

}
