package org.kite9.diagram.batik.element;

import java.awt.geom.GeneralPath;
import java.util.Map;

import org.apache.batik.svggen.SVGPath;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Painter;
import org.kite9.diagram.batik.bridge.RoutePainter;
import org.kite9.diagram.batik.format.ExtendedSVGGeneratorContext;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformationImpl;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConnectionImpl extends AbstractBatikDiagramElement implements Connection {

	public ConnectionImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter<Connection> p) {
		super(el, parent, ctx, p);
	}

	@Override
	protected void initialize() {
		super.initialize();
		Kite9XMLElement fromElement = getFromElement(theElement);
		Kite9XMLElement toElement = getToElement(theElement);
		
		from = (Connected) fromElement.getDiagramElement();
		to = (Connected) toElement.getDiagramElement();
		
		if (from == null) {
			throw new Kite9ProcessingException("Couldn't resolve 'from' reference for "+this.getID());
		}
		
		if (to == null) {
			throw new Kite9ProcessingException("Couldn't resolve 'to' reference for "+this.getID());
		}
		
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
		
		this.minimumLength = theElement.getCSSStyleProperty(CSSConstants.LINK_MINIMUM_LENGTH).getFloatValue();
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


	public static Kite9XMLElement getFromElement(Kite9XMLElement theElement) {
		String reference = getFromReference(theElement);
		ADLDocument owner = theElement.getOwnerDocument();
		Kite9XMLElement from = (Kite9XMLElement) owner.getChildElementById(owner, reference);
		return from;
	}


	public static String getFromReference(Kite9XMLElement theElement) {
		Element fromEl = theElement.getProperty("from");
		String reference = fromEl.getAttribute("reference");
		return reference;
	}

	public static Kite9XMLElement getToElement(Kite9XMLElement theElement) {
		String reference = getToReference(theElement);
		ADLDocument owner = theElement.getOwnerDocument();
		Kite9XMLElement to = (Kite9XMLElement) owner.getChildElementById(owner, reference);
		return to;
	}


	public static String getToReference(Kite9XMLElement theElement) {
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
		return rank;
	}

	@Override
	public double getMargin(Direction d) {
		return margin[d.ordinal()];
	}

	@Override
	public double getPadding(Direction d) {
		return padding[d.ordinal()];
	}

	@Override
	protected void postProcess(Element out) {
	}
	
	

	@Override
	public Element output(Document d) {
		if (getRenderingInformation().isRendered()) {
			return super.output(d);
		} else {
			return null;
		}
	}

	@Override
	protected Map<String, String> getReplacementMap(StyledKite9SVGElement theElement) {
		Map<String, String> out = super.getReplacementMap(theElement);
		RoutePainter routePainter = new RoutePainter(0, 0);
		ExtendedSVGGeneratorContext ctx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(theElement.getOwnerDocument(), null);
		double startReserve = fromDecoration.getMarkerReserve();
		double endReserve = toDecoration.getMarkerReserve();
		
		GeneralPath gp = routePainter.drawRouting(this.getRenderingInformation(), 
				new RoutePainter.ReservedLengthEndDisplayer(startReserve), 
				new RoutePainter.ReservedLengthEndDisplayer(endReserve),
				routePainter.LINK_HOP_DISPLAYER, false);
		String path = SVGPath.toSVGPathData(gp, ctx);
		out.put("path", path);
		out.put("markerstart", fromDecoration.getMarkerUrl());
		out.put("markerend", toDecoration.getMarkerUrl());
		return out;
	}

	@Override
	public Terminator getDecorationForEnd(DiagramElement end) {
		if (from == end) {
			return fromDecoration;
		} else if (to == end) {
			return toDecoration;
		} else {
			throw new LogicException("Trying to get decoration for not-an-end");
		}
	}

	@Override
	public double getMinimumLength() {
		return minimumLength;
	}

	

}
