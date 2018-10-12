package org.kite9.diagram.batik.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.batik.anim.dom.SVGOMMarkerElement;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.framework.common.Kite9ProcessingException;

public class TerminatorImpl extends AbstractRectangular implements Terminator {
	
	private SVGOMMarkerElement markerElement;
	private Value reference;
	private double markerReserve;
	private Direction arrivalSide;

	public TerminatorImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}

	@Override
	protected void initialize() {
		super.initialize();
		
		boolean from = ((Connection)parent).getFromDecoration() == this;
		
		reference = from ? getCSSStyleProperty(CSSConstants.MARKER_START_REFERENCE) : 
			getCSSStyleProperty(CSSConstants.MARKER_END_REFERENCE);
		
		markerReserve  = getCSSStyleProperty(CSSConstants.MARKER_RESERVE).getFloatValue();
		
		if (reference != ValueConstants.NONE_VALUE) {
			Kite9DocumentLoader loader = (Kite9DocumentLoader) ctx.getDocumentLoader();
			markerElement = (SVGOMMarkerElement) loader.loadElementFromUrl(reference, getPainter().getContents());
		} 
		
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.ARRIVAL_SIDE);
		arrivalSide = (Direction) ev.getTheValue();
	}
	
	@Override
	public Container getContainer() {
		Connection c = (Connection) getParent();
		if (this == c.getFromDecoration()) {
			return c.getFrom().getContainer();
		} else if (this == c.getToDecoration()) {
			return c.getTo().getContainer();
		} else {
			throw new Kite9ProcessingException();
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
	public Map<String, String> getXPathVariables() {
		HashMap<String, String> out = new HashMap<>();
		out.put("x0", "0");
		out.put("y0", "0");
		double width = getSizeBasedOnPadding().getWidth();
		double height = getSizeBasedOnPadding().getHeight();
		out.put("x1", ""+ width);
		out.put("y1", ""+ height);
		out.put("width", ""+ width);
		out.put("height", ""+ height);
		
		return out;	
	}

	@Override
	public Direction getArrivalSide() {
		ensureInitialized();
		return arrivalSide;
	}
	
}
