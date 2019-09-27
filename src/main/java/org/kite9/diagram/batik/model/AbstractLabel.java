package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.End;
import org.kite9.diagram.model.style.ContentTransform;

public abstract class AbstractLabel extends AbstractCompactedRectangular implements Label {

	private End end;
	
	public AbstractLabel(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.LINK_END);
		end = (End) ev.getTheValue();
	}

	@Override 
	public boolean isConnectionLabel() {
		ensureInitialized();
		return !(getParent() instanceof Container);
	}
	
	/**
	 * Handles the case where labels can be nested inside terminators (this should be the way forward)
	 * and also the old case where they were separate.
	 */
	public Connection getConnectionParent() {
		ensureInitialized();
		DiagramElement out = this;
		do {
			out = out.getParent();
		} while (!(out instanceof Connection));
		
		return (Connection) out;
	}

	@Override
	public Container getContainer() {
		ensureInitialized();
		if (isConnectionLabel()) {
			Connection c = getConnectionParent();
			if (this == c.getFromLabel()) {
				return c.getFrom().getContainer();
			} else if (this == c.getToLabel()) {
				return c.getTo().getContainer();
			} else {
				throw contextualException("Couldn't get container for label "+getID());
			}
		} else {
			return super.getContainer();
		}
	}

	@Override
	public End getEnd() {
		ensureInitialized();
		return end;
	}
	
}