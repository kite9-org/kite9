package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Leaf;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * For text or shape-based labels within the diagram.
 * 
 * @author robmoffat
 * 
 */
public class LabelLeafImpl extends AbstractRectangularDiagramElement implements Label, Leaf {
	
	
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
	
	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	public boolean isConnectionLabel() {
		ensureInitialized();
		return getParent() instanceof Connection;
	}

	@Override
	public Container getContainer() {
		if (isConnectionLabel()) {
			Connection c = (Connection) getParent();
			if (this == c.getFromLabel()) {
				return c.getFrom().getContainer();
			} else if (this == c.getToLabel()) {
				return c.getTo().getContainer();
			} else {
				throw new Kite9ProcessingException();
			}
		} else {
			return super.getContainer();
		}
	}
	
	
	
}