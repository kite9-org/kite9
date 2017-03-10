package org.kite9.diagram.visualization.batik.element;

import java.awt.Graphics2D;
import java.awt.Shape;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.Terminator;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformationImpl;
import org.kite9.diagram.style.DiagramElementSizing;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.visualization.batik.bridge.Kite9RouteBridge;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.display.RoutePainter;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.LinkLineStyle;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;

public class ConnectionImpl extends AbstractSVGDiagramElement implements Connection {

	public ConnectionImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	@Override
	protected void initialize() {
		XMLElement fromElement = getFromElement(theElement);
		XMLElement toElement = getToElement(theElement);
		
		from = (Connected) fromElement.getDiagramElement();
		to = (Connected) toElement.getDiagramElement();
		drawDirection = Direction.getDirection(theElement.getAttribute("drawDirection"));
		
		XMLElement fromDecorationEl = theElement.getProperty("fromDecoration");
		this.fromDecoration = getTerminator(fromDecorationEl);
		
		XMLElement toDecorationEl = theElement.getProperty("toDecoration");
		this.toDecoration = getTerminator(toDecorationEl);
		
		XMLElement fromLabelEl = theElement.getProperty("fromLabel");
		this.fromLabel = getLabel(fromLabelEl);
		
		XMLElement toLabelEl = theElement.getProperty("toLabel");
		this.toLabel = getLabel(toLabelEl);
		
		String rank = theElement.getAttribute("rank");
		if (!"".equals(rank)) {
			this.rank = Integer.parseInt(rank);
		}
	}


	private Terminator getTerminator(XMLElement el) {
		if (el == null) {
			el = (XMLElement) theElement.getOwnerDocument().createElement("terminator");
			theElement.appendChild(el);
		}
		return (Terminator) el.getDiagramElement();
	}
	
	private Label getLabel(XMLElement el) {
		if (el == null) {
			return null;
		}
		return (Label) el.getDiagramElement();
	}


	public static XMLElement getFromElement(XMLElement theElement) {
		String reference = getFromReference(theElement);
		ADLDocument owner = theElement.getOwnerDocument();
		XMLElement from = (XMLElement) owner.getChildElementById(owner, reference);
		return from;
	}


	public static String getFromReference(XMLElement theElement) {
		Element fromEl = theElement.getProperty("from");
		String reference = fromEl.getAttribute("reference");
		return reference;
	}

	public static XMLElement getToElement(XMLElement theElement) {
		String reference = getToReference(theElement);
		ADLDocument owner = theElement.getOwnerDocument();
		XMLElement to = (XMLElement) owner.getChildElementById(owner, reference);
		return to;
	}


	public static String getToReference(XMLElement theElement) {
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
	public void setFrom(Connected v) {
		System.out.println("Setting value in something that should be immutable");
		this.from = v;
	}

	@Override
	public void setTo(Connected v) {
		System.out.println("Setting value in something that should be immutable");
		this.to = v;
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
	public DiagramElementSizing getSizing() {
		return DiagramElementSizing.UNSPECIFIED;
	}
	
	
}
