package org.kite9.diagram.batik.model;

import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension2D;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.End;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementSizing;

public class TerminatorImpl extends AbstractRectangular implements Terminator {
	
	private String reference;
	private double markerReserve;
	private Direction arrivalSide;
	private End end;

	public TerminatorImpl(StyledKite9XMLElement el, DiagramElement parent, ElementContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}

	@Override
	protected void initialize() {
		super.initialize();		
		
		arrivalSide = (Direction) ctx.getCSSStyleProperty(CSSConstants.ARRIVAL_SIDE, getTheElement());
		end = (End) ctx.getCSSStyleProperty(CSSConstants.LINK_END, getTheElement());
		boolean from = end == End.FROM;
		
		reference = from ? ctx.getCssStringValue(CSSConstants.MARKER_START_REFERENCE, getTheElement()) :
			ctx.getCssStringValue(CSSConstants.MARKER_END_REFERENCE, getTheElement());
		
		markerReserve  = ctx.getCssDoubleValue(CSSConstants.MARKER_RESERVE, getTheElement());
	}
	
	@Override
	public Container getContainer() {
		Connection c = (Connection) getParent();
		if (this == c.getFromDecoration()) {
			return c.getFrom().getContainer();
		} else if (this == c.getToDecoration()) {
			return c.getTo().getContainer();
		} else {
			throw ctx.contextualException("Couldn't get container for terminator "+getID(), getTheElement());
		}
	}

	@Override
	public double getReservedLength() {
		ensureInitialized();
		return getPadding(Direction.RIGHT) + getMargin(Direction.RIGHT);
	}

	public String getMarkerUrl() {
		ensureInitialized();
		if (reference != null) {
			return "url(#"+reference+")";
		} else {
			return null;
		}
	}

	@Override
	public double getMarkerReserve() {
		ensureInitialized();
		return markerReserve;
	}

	@Override
	public boolean styleMatches(Terminator t2) {
		if (t2 instanceof TerminatorImpl) {
			boolean styleMatch = attributesMatch("style", this, (TerminatorImpl) t2);
			boolean classMatch = attributesMatch("class", this, (TerminatorImpl) t2);
			return styleMatch && classMatch;
		} else {
			return false;
		}
	}
	
	
	
	private static boolean attributesMatch(String name, TerminatorImpl a, TerminatorImpl b) {
		return a.getDOMElement().getAttribute(name).equals(
				b.getDOMElement().getAttribute(name));
	}

	@Override
	public Dimension2D getMinimumSize() {
		return CostedDimension2D.Companion.getZERO();
	}

	@Override
	public String getXPathVariable(String name) {
		if (("x0".equals(name) )|| ("y0".equals(name))) {
			return "0";
		} else if ("x1".equals(name) || "width".equals(name)) {
			return ""+getSizeBasedOnPadding().width();
		} else if ("y1".equals(name) || "height".equals(name)) {
			return ""+getSizeBasedOnPadding().height();
		}
		
		return null;
	}

	@Override
	public Direction getArrivalSide() {
		ensureInitialized();
		return arrivalSide;
	}

	/**
	 * This is currently true, but won't always be.
	 */
	@Override
	public Connection getConnection() {
		ensureInitialized();
		return (Connection) getParent();
	}

	@Override
	public End getEnd() {
		ensureInitialized();
		return end;
	}

	@Override
	public DiagramElementSizing getSizing(boolean horiz) {
		return DiagramElementSizing.MINIMIZE;
	}
	
}
