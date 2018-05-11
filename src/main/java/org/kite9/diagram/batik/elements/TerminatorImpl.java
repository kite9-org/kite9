package org.kite9.diagram.batik.elements;

import org.apache.batik.anim.dom.SVGOMMarkerElement;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.batik.painter.Painter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.elements.StyledKite9SVGElement;
import org.kite9.framework.logging.LogicException;

public class TerminatorImpl extends AbstractRectangularDiagramElement implements Terminator {
	
	private SVGOMMarkerElement markerElement;
	private Value reference;
	private double markerReserve;

	public TerminatorImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter rp) {
		super(el, parent, ctx, rp);
	}

	@Override
	protected void initialize() {
		super.initialize();
		
		boolean from = ((Connection)parent).getFromDecoration() == this;
		
		reference = from ? theElement.getCSSStyleProperty(CSSConstants.MARKER_START_REFERENCE) : 
			theElement.getCSSStyleProperty(CSSConstants.MARKER_END_REFERENCE);
		
		markerReserve  = theElement.getCSSStyleProperty(CSSConstants.MARKER_RESERVE).getFloatValue();
		
		if (reference != ValueConstants.NONE_VALUE) {
			Kite9DocumentLoader loader = (Kite9DocumentLoader) ctx.getDocumentLoader();
			markerElement = (SVGOMMarkerElement) loader.loadElementFromUrl(reference, theElement);
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
	public CostedDimension getSize(Dimension2D within) {
		throw new LogicException("Shouldn't be using size for terminators");
	}

	@Override
	public double getMarkerReserve() {
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
		return a.theElement.getAttribute(name).equals(b.theElement.getAttribute(name));
	}

	@Override
	public Dimension2D getMinimumSize() {
		return CostedDimension.ZERO;
	}
	
	
}
