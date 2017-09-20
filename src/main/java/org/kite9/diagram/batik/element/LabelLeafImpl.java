package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Leaf;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * For text or shape-based labels within the diagram.
 * 
 * @author robmoffat
 * 
 */
public class LabelLeafImpl extends AbstractLabelImpl implements Label, Leaf {
	
	public LabelLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	@Override
	public boolean hasContent() {
		return !getText().isEmpty();
	}
	
	public String getText() {
		return theElement.getTextContent().trim();
	}
	
}