package org.kite9.diagram.batik.model;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.svggen.SVGPath;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.RoutePainterImpl;
import org.kite9.diagram.batik.text.ExtendedSVGGeneratorContext;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.elements.ReferencingKite9XMLElement;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.End;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformationImpl;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.common.LinkReferenceException;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConnectionImpl extends AbstractBatikDiagramElement implements Connection, XPathAware {

	private String fromId;
	private String toId;
	
	public ConnectionImpl(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter p, ContentTransform t) {
		super(el, parent, ctx, p, t);
		this.fromId = getReference(CSSConstants.LINK_FROM_XPATH);
		this.toId = getReference(CSSConstants.LINK_TO_XPATH);
		addConnectionReference(fromId, this);
		addConnectionReference(toId, this);
	}

	@Override
	protected void initialize() {
		super.initialize();
		initFromTo();
		initContents();
		initDrawDirection();
		initRank();
		initSize();
	}
	
	/**
	 * For elements which are containers, call this method as part of initialize.
	 */
	protected List<DiagramElement> initContents() {
		List<DiagramElement> contents = new ArrayList<>();
		for (Kite9XMLElement xmlElement : getPainter().getContents()) {
			DiagramElement de = xmlElement.getDiagramElement();			
			if (de instanceof Terminator) {
				End e = ((Terminator) de).getEnd();
				
				if  (e == End.FROM) {
					this.fromDecoration = this.fromDecoration == null ? (Terminator) de : this.fromDecoration;
				} else if (e == End.TO) {
					this.toDecoration = this.toDecoration == null ? (Terminator) de : this.toDecoration;
				}
 				
			} else if (de instanceof Label) { 
				End e = ((Label) de).getEnd();
				if  (e == End.FROM) {
					this.fromLabel = this.fromLabel == null ? (Label) de : this.fromLabel;
				} else if (e == End.TO) {
					this.toLabel = this.toLabel == null ? (Label) de : this.toLabel;
				}
				
			} 
		}
		
		return contents;
	}

	protected void initSize() {
		this.minimumLength = getCSSStyleProperty(CSSConstants.LINK_MINIMUM_LENGTH).getFloatValue();
		this.cornerRadius = getCSSStyleProperty(CSSConstants.LINK_CORNER_RADIUS).getFloatValue();
	}

	protected void initRank() {
		Kite9XMLElement theElement = getPainter().getContents();
		String rank = theElement.getAttribute("rank");
		if (!"".equals(rank)) {
			this.rank = Integer.parseInt(rank);
		} else {
			// if rank isn't set, then connections are ranked in order from last to first..
			this.rank = indexOf(theElement, theElement.getParentNode().getChildNodes());
		}
	}

	protected void initFromTo() {
		Kite9XMLElement fromElement = getReferencedElement(this.fromId);
		if (fromElement == null) {
			throw new Kite9ProcessingException("Couldn't resolve 'from' reference for "+this.getID());
		}

		Kite9XMLElement toElement = getReferencedElement(this.toId);
		if (toElement == null) {
			throw new Kite9ProcessingException("Couldn't resolve 'to' reference for "+this.getID());
		}

		from = (Connected) fromElement.getDiagramElement();
		to = (Connected) toElement.getDiagramElement();
		
		if (from == null) {
			throw new Kite9ProcessingException("Couldn't resolve 'from' reference for "+this.getID());
		}
		
		if (to == null) {
			throw new Kite9ProcessingException("Couldn't resolve 'to' reference for "+this.getID());
		}
	}
	
	private void initDrawDirection() {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.CONNECTION_DIRECTION);
		if (ev != null) {
			drawDirection = (Direction) ev.getTheValue();
		}
	}

	private int indexOf(Element e, NodeList within) {
		for (int i = 0; i < within.getLength(); i++) {
			if (within.item(i) == e) {
				return i;
			}
		}
		
		return -1;
	}


	private Kite9XMLElement getReferencedElement(String id) {
		ADLDocument owner = getPainter().getContents().getOwnerDocument();
		Kite9XMLElement from = (Kite9XMLElement) owner.getChildElementById(owner, id);
		return from;
	}
	
	private String getReference(String css) {
		Kite9XMLElement theElement = getPainter().getContents();
		String reference = ((ReferencingKite9XMLElement) theElement).getIDReference(css);
		
		if (theElement.getOwnerDocument().getElementById(reference) == null) {
			throw new LinkReferenceException(reference, getID());
		}
		
		return reference;
	}

	public Kite9XMLElement getToElement() {
		ADLDocument owner = getPainter().getContents().getOwnerDocument();
		Kite9XMLElement to = (Kite9XMLElement) owner.getChildElementById(owner, toId);
		return to;
	}


	public String getToReference() {
		Kite9XMLElement theElement = getPainter().getContents();
		String reference = ((ReferencingKite9XMLElement) theElement).getIDReference(CSSConstants.LINK_TO_XPATH);
		
		if (theElement.getOwnerDocument().getElementById(reference) == null) {
			throw new LinkReferenceException(reference, getID());
		}
		
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
	public String getXPathVariable(String name) {
		ensureInitialized();
		if ("path".equals(name)) {
			RoutePainterImpl routePainter = new RoutePainterImpl();
			ExtendedSVGGeneratorContext ctx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(
					getPainter().getContents().getOwnerDocument());
			double startReserve = fromDecoration == null ? 0 : fromDecoration.getMarkerReserve();
			double endReserve = toDecoration == null ? 0 : toDecoration.getMarkerReserve();
			
			GeneralPath gp = routePainter.drawRouting(this.getRenderingInformation(), 
					new RoutePainterImpl.ReservedLengthEndDisplayer(startReserve), 
					new RoutePainterImpl.ReservedLengthEndDisplayer(endReserve),
					new RoutePainterImpl.CurvedCornerHopDisplayer((float) getCornerRadius()), false);
			String path = SVGPath.toSVGPathData(gp, ctx);
			return path;
		} else if ("markerstart".equals(name)) {
			if (fromDecoration instanceof TerminatorImpl) {
				return ((TerminatorImpl) fromDecoration).getMarkerUrl();
			}
		} else if ("markerend".equals(name)) {
			if (toDecoration instanceof TerminatorImpl) {
				return ((TerminatorImpl) toDecoration).getMarkerUrl();
			}
		} 
		
		return null;
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
