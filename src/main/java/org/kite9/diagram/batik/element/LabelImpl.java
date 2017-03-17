package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * Container and link-end labels. (TEMPORARY)
 * 
 * @author robmoffat
 * 
 */
public class LabelImpl extends AbstractRectangularDiagramElement implements Label {
	
	
	public LabelImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	@Override
	public boolean hasContent() {
		return !getText().isEmpty();
	}
	
	public String getText() {
		return theElement.getTextContent().trim();
	}
	
	@Override
	protected void initialize() {
	}

	@Override
	public boolean isConnectionLabel() {
		return getParent() instanceof Connection;
	}
	
	
}