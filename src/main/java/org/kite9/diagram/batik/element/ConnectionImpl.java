package org.kite9.diagram.batik.element;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9RouteBridge;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
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
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.LinkLineStyle;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public class ConnectionImpl extends AbstractSVGDiagramElement implements Connection {

	public ConnectionImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	@Override
	protected void initialize() {
		super.initialize();
		Kite9XMLElement fromElement = getFromElement(theElement);
		Kite9XMLElement toElement = getToElement(theElement);
		
		from = (Connected) fromElement.getDiagramElement();
		to = (Connected) toElement.getDiagramElement();
		drawDirection = Direction.getDirection(theElement.getAttribute("drawDirection"));
		
		Kite9XMLElement fromDecorationEl = theElement.getProperty("fromDecoration");
		this.fromDecoration = getTerminator(fromDecorationEl);
		
		Kite9XMLElement toDecorationEl = theElement.getProperty("toDecoration");
		this.toDecoration = getTerminator(toDecorationEl);
		
		Kite9XMLElement fromLabelEl = theElement.getProperty("fromLabel");
		this.fromLabel = getLabel(fromLabelEl);
		
		Kite9XMLElement toLabelEl = theElement.getProperty("toLabel");
		this.toLabel = getLabel(toLabelEl);
		
		String rank = theElement.getAttribute("rank");
		if (!"".equals(rank)) {
			this.rank = Integer.parseInt(rank);
		}
	}


	private Terminator getTerminator(Kite9XMLElement el) {
		if (el == null) {
			el = (Kite9XMLElement) theElement.getOwnerDocument().createElement("terminator");
			theElement.appendChild(el);
		}
		return (Terminator) el.getDiagramElement();
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
	private Terminator fromDecoration;
	private Terminator toDecoration;
	private Label fromLabel;
	private Label toLabel;
	private int rank;
	

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
	public String getStyle() {
		return LinkLineStyle.NORMAL;
	}

	@Override
	public int getRank() {
		return rank;
	}

	protected void initSVGGraphicsContents(IdentifiableGraphicsNode out) {
		Kite9RouteBridge bridge = new Kite9RouteBridge(this);
		GraphicsNode gn = bridge.createGraphicsNode(ctx, this.theElement);
		bridge.buildGraphicsNode(ctx, theElement, gn);
		out.add(gn);
	}
	
	@Override
	public double getMargin(Direction d) {
		return margin[d.ordinal()];
	}

	@Override
	public double getPadding(Direction d) {
		return padding[d.ordinal()];
	}
	
}
