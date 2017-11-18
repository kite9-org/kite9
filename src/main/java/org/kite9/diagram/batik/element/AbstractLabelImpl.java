package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.StyledKite9SVGElement;

public abstract class AbstractLabelImpl extends AbstractCompactedRectangularDiagramElement implements Label {

	public AbstractLabelImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<?> rp) {
		super(el, parent, ctx, rp);
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