package org.kite9.diagram.batik.model;

import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.svggen.SVGPath;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.RoutePainter;
import org.kite9.diagram.batik.text.ExtendedSVGGeneratorContext;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformationImpl;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Element;

public class ConnectionImpl extends AbstractBatikDiagramElement implements Connection, XPathAware {

	private String fromId;
	private String toId;
	private Direction fromArrivalSide = null;
	private Direction toArrivalSide = null;
	
	public ConnectionImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter p, ContentTransform t) {
		super(el, parent, ctx, p, t);
		this.fromId = getFromReference();
		this.toId = getToReference();
		addConnectionReference(fromId, this);
		addConnectionReference(toId, this);
	}

	@Override
	protected void initialize() {
		super.initialize();
		from = (Connected) getFromElement().getDiagramElement();
		to = (Connected) getToElement().getDiagramElement();
		
		if (from == null) {
			throw new Kite9ProcessingException("Couldn't resolve 'from' reference for "+this.getID());
		}
		
		if (to == null) {
			throw new Kite9ProcessingException("Couldn't resolve 'to' reference for "+this.getID());
		}
		
		Kite9XMLElement theElement = getPainter().getContents();
		
		drawDirection = Direction.getDirection(theElement.getAttribute("drawDirection"));
		
		this.fromDecoration = getTerminator(theElement.getProperty("from"));
		
		this.toDecoration = getTerminator(theElement.getProperty("to"));
		
		Kite9XMLElement fromLabelEl = theElement.getProperty("fromLabel");
		this.fromLabel = getLabel(fromLabelEl);
		
		Kite9XMLElement toLabelEl = theElement.getProperty("toLabel");
		this.toLabel = getLabel(toLabelEl);
		
		String rank = theElement.getAttribute("rank");
		if (!"".equals(rank)) {
			this.rank = Integer.parseInt(rank);
		}
		
		this.minimumLength = getCSSStyleProperty(CSSConstants.LINK_MINIMUM_LENGTH).getFloatValue();
		this.cornerRadius = getCSSStyleProperty(CSSConstants.LINK_CORNER_RADIUS).getFloatValue();
	}


	private TerminatorImpl getTerminator(Kite9XMLElement el) {
		return (TerminatorImpl) el.getDiagramElement();
	}
	
	private Label getLabel(Kite9XMLElement el) {
		if (el == null) {
			return null;
		}
		return (Label) el.getDiagramElement();
	}


	private Kite9XMLElement getFromElement() {
		ADLDocument owner = getPainter().getContents().getOwnerDocument();
		Kite9XMLElement from = (Kite9XMLElement) owner.getChildElementById(owner, fromId);
		return from;
	}


	private String getFromReference() {
		Kite9XMLElement theElement = getPainter().getContents();
		Element fromEl = theElement.getProperty("from");
		String reference = fromEl.getAttribute("reference");
		return reference;
	}

	public Kite9XMLElement getToElement() {
		ADLDocument owner = getPainter().getContents().getOwnerDocument();
		Kite9XMLElement to = (Kite9XMLElement) owner.getChildElementById(owner, toId);
		return to;
	}


	public String getToReference() {
		Kite9XMLElement theElement = getPainter().getContents();
		Element toEl = theElement.getProperty("to");
		String reference = toEl.getAttribute("reference");
		return reference;
	}
	
	private Connected from;
	private Connected to;
	private Direction drawDirection;
	private TerminatorImpl fromDecoration;
	private TerminatorImpl toDecoration;
	private Label fromLabel;
	private Label toLabel;
	private int rank;
	private double minimumLength;
	private double cornerRadius;
	

	@Override
	public Connected getFrom() {
		ensureInitialized();
		return from;
	}

	@Override
	public Connected getTo() {
		ensureInitialized();
		return to;
	}

	@Override
	public Connected otherEnd(Connected end) {
		if (end == getFrom()) {
			return getTo();
		} else if (end == getTo()) {
			return getFrom();
		} else {
			throw new Kite9ProcessingException("otherEnd of neither from or to "+this+" "+end);
		}
	}

	@Override
	public boolean meets(BiDirectional<Connected> e) {
		return meets(e.getFrom()) || meets(e.getTo());
	}

	@Override
	public boolean meets(Connected v) {
		return (getFrom()==v) || (getTo()==v);
	}

	@Override
	public Direction getDrawDirection() {
		ensureInitialized();
		return drawDirection;
	}

	@Override
	public Direction getDrawDirectionFrom(Connected from) {
		if (getFrom() == from) {
			return getDrawDirection();
		} else {
			return Direction.reverse(getDrawDirection());
		}
	}

	@Override
	public void setDrawDirectionFrom(Direction d, Connected from) {
		throw new Kite9ProcessingException("Should be immutable");
	}
	
	@Override
	public void setDrawDirection(Direction d) {
		throw new Kite9ProcessingException("Should be immutable");
	}

	@Override
	public Terminator getFromDecoration() {
		ensureInitialized();
		return fromDecoration;
	}

	@Override
	public Terminator getToDecoration() {
		ensureInitialized();
		return toDecoration;
	}

	@Override
	public Label getFromLabel() {
		ensureInitialized();
		return fromLabel;
	}

	@Override
	public Label getToLabel() {
		ensureInitialized();
		return toLabel;
	}

	private RouteRenderingInformation ri;
	
	@Override
	public RouteRenderingInformation getRenderingInformation() {
		if (ri == null) {
			ri = new RouteRenderingInformationImpl();
		}
		
		return ri;
	}

	public void setRenderingInformation(RenderingInformation ri) {
		this.ri = (RouteRenderingInformation) ri;
	}

	@Override
	public int getRank() {
		ensureInitialized();
		return rank;
	}

	@Override
	public double getMargin(Direction d) {
		ensureInitialized();
		return margin[d.ordinal()];
	}

	@Override
	public double getPadding(Direction d) {
		ensureInitialized();
		return padding[d.ordinal()];
	}

	@Override
	public Map<String, String> getXPathVariables() {
		ensureInitialized();
		Map<String, String> out = new HashMap<>();
		RoutePainter routePainter = new RoutePainter();
		ExtendedSVGGeneratorContext ctx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(
				getPainter().getContents().getOwnerDocument());
		double startReserve = fromDecoration.getMarkerReserve();
		double endReserve = toDecoration.getMarkerReserve();
		
		GeneralPath gp = routePainter.drawRouting(this.getRenderingInformation(), 
				new RoutePainter.ReservedLengthEndDisplayer(startReserve), 
				new RoutePainter.ReservedLengthEndDisplayer(endReserve),
				new RoutePainter.CurvedCornerHopDisplayer((float) getCornerRadius()), false);
		String path = SVGPath.toSVGPathData(gp, ctx);
		out.put("path", path);
		out.put("markerstart", fromDecoration.getMarkerUrl());
		out.put("markerend", toDecoration.getMarkerUrl());
		return out;
	}

	@Override
	public Terminator getDecorationForEnd(DiagramElement end) {
		ensureInitialized();
		if (from == end) {
			return fromDecoration;
		} else if (to == end) {
			return toDecoration;
		} else {
			throw new LogicException("Trying to get decoration for an end that isn't from or to");
		}
	}

	@Override
	public double getMinimumLength() {
		ensureInitialized();
		return minimumLength;
	}
	
	@Override
	public double getCornerRadius() {
		ensureInitialized();
		return cornerRadius;
	}

	@Override
	public Direction getFromArrivalSide() {
		ensureInitialized();
		
		if ((fromDecoration != null) && (fromDecoration.getArrivalSide() != null)) {
			return fromDecoration.getArrivalSide();
			
		}
		
		return Direction.reverse(drawDirection);
	}

	@Override
	public Direction getToArrivalSide() {
		ensureInitialized();

		if ((toDecoration != null) && (toDecoration.getArrivalSide() != null)) {
			return toDecoration.getArrivalSide();
			
		}
		
		return drawDirection;
	}
	
	
}
