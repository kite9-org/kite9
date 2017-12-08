package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public class TerminatorImpl extends AbstractRectangularDiagramElement implements Terminator {
	
	private Element markerElement;
	private String reference;
	private double margin;
	private double reservedLength;

	public TerminatorImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Leaf> rp) {
		super(el, parent, ctx, rp);
	}

	@Override
	protected void initialize() {
		reference = theElement.getAttribute("markerReference");
		if (reference.trim().length() > 0) {
			ADLDocument owner = theElement.getOwnerDocument();
			markerElement = owner.getChildElementById(owner, reference);
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
		return reservedLength;
	}

	@Override
	public double getMargin() {
		ensureInitialized();
		return margin;
	}

	@Override
	protected Dimension2D getRectangularRenderedSize() {
		return new Dimension2D(10, 10);
	}
	
	@Override
	protected Dimension2D getRectangularRenderedPosition() {
		return new Dimension2D(10, 10);
	}

	public String getMarkerUrl() {
		ensureInitialized();
		if (markerElement != null) {
			return "url(#"+reference+")";
		} else {
			return null;
		}
	}

	
}
