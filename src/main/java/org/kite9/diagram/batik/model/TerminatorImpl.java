package org.kite9.diagram.batik.model;

import org.apache.batik.anim.dom.SVGOMMarkerElement;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.End;
import org.kite9.diagram.model.style.ContentTransform;

public class TerminatorImpl extends AbstractRectangular implements Terminator {
	
	private SVGOMMarkerElement markerElement;
	private Value reference;
	private double markerReserve;
	private Direction arrivalSide;
	private End end;

	public TerminatorImpl(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}

	@Override
	protected void initialize() {
		super.initialize();		
		
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.ARRIVAL_SIDE);
		arrivalSide = (Direction) ev.getTheValue();
		
		ev = (EnumValue) getCSSStyleProperty(CSSConstants.LINK_END);
		end = (End) ev.getTheValue();

		boolean from = end == End.FROM;
		
		reference = from ? getCSSStyleProperty(CSSConstants.MARKER_START_REFERENCE) : 
			getCSSStyleProperty(CSSConstants.MARKER_END_REFERENCE);
		
		markerReserve  = getCSSStyleProperty(CSSConstants.MARKER_RESERVE).getFloatValue();
		
		if (reference != ValueConstants.NONE_VALUE) {
			Kite9DocumentLoader loader = (Kite9DocumentLoader) ctx.getDocumentLoader();
			markerElement = (SVGOMMarkerElement) loader.loadElementFromUrl(reference, getPainter().getContents());
		} 
		
		
	}
	
	@Override
	public Container getContainer() {
		Connection c = (Connection) getParent();
		if (this == c.getFromDecoration()) {
			return c.getFrom().getContainer();
		} else if (this == c.getToDecoration()) {
			return c.getTo().getContainer();
		} else {
			throw contextualException("Couldn't get container for terminator "+getID());
		}
	}

	@Override
	public double getReservedLength() {
		ensureInitialized();
		return getPadding(Direction.RIGHT) + getMargin(Direction.RIGHT);
	}

	public String getMarkerUrl() {
		ensureInitialized();
		if (markerElement != null) {
			return "url(#"+markerElement.getId()+")";
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
		return a.getPainter().getContents().getAttribute(name).equals(
				b.getPainter().getContents().getAttribute(name));
	}

	@Override
	public Dimension2D getMinimumSize() {
		return CostedDimension.ZERO;
	}

	@Override
	public String getXPathVariable(String name) {
		if (("x0".equals(name) )|| ("y0".equals(name))) {
			return "0";
		} else if ("x1".equals(name) || "width".equals(name)) {
			return ""+getSizeBasedOnPadding().getWidth();
		} else if ("y1".equals(name) || "height".equals(name)) {
			return ""+getSizeBasedOnPadding().getHeight();
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
	
}
