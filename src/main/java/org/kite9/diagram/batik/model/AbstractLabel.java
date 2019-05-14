package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.framework.common.Kite9ProcessingException;

public abstract class AbstractLabel extends AbstractCompactedRectangular implements Label {

	public AbstractLabel(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}

	@Override 
	public boolean isConnectionLabel() {
		ensureInitialized();
		return !(getParent() instanceof Container);
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