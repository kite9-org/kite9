package org.kite9.diagram.batik.model;

import org.apache.batik.svggen.SVGPath;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.RoutePainterImpl;
import org.kite9.diagram.batik.text.ExtendedSVGGeneratorContext;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.LinkReferenceException;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.logging.LogicException;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.*;
import org.kite9.diagram.model.position.*;
import org.kite9.diagram.model.style.ContentTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class ConnectionImpl extends AbstractBatikDiagramElement implements Connection, XPathAware {

	private String fromId;
	private String toId;
	
	public ConnectionImpl(StyledKite9XMLElement el, DiagramElement parent, Kite9BridgeContext ctx, Painter p, ContentTransform t) {
		super(el, parent, ctx, p, t);
	}

	@Override
	protected void initialize() {
		super.initialize();
		initReferences();
		initFromTo();
		initContents();
		initDrawDirection();
		initRank();
		initSize();
	}
	
	private void initReferences() {
		this.fromId = getReference(CSSConstants.LINK_FROM_XPATH);
		this.toId = getReference(CSSConstants.LINK_TO_XPATH);
	}

	/**
	 * For elements which are containers, call this method as part of initialize.
	 */
	protected List<DiagramElement> initContents() {
		List<DiagramElement> contents = new ArrayList<>();
		for (DiagramElement de : ctx.getChildDiagramElements(getTheElement(), this)) {
			if (de instanceof Terminator) {
				End e = ((Terminator) de).getEnd();
				
				if  (e == End.FROM) {
					this.fromDecoration = this.fromDecoration == null ? (Terminator) de : this.fromDecoration;
				} else if (e == End.TO) {
					this.toDecoration = this.toDecoration == null ? (Terminator) de : this.toDecoration;
				} else if (this.fromDecoration == null) {
					this.fromDecoration = (Terminator) de;
				} else if (this.toDecoration == null) {
					this.toDecoration = (Terminator) de;
				}
 				
			} else if (de instanceof Label) { 
				End e = ((Label) de).getEnd();
				if  (e == End.FROM) {
					this.fromLabel = this.fromLabel == null ? (Label) de : this.fromLabel;
				} else if (e == End.TO) {
					this.toLabel = this.toLabel == null ? (Label) de : this.toLabel;
				} else if (this.fromLabel == null) {
					this.fromLabel = (Label) de;
				} else if (this.toLabel == null) {
					this.toLabel = (Label) de;
				}
				
			} 
		}
		
		return contents;
	}

	protected void initSize() {
		this.minimumLength = ctx.getCssDoubleValue(CSSConstants.LINK_MINIMUM_LENGTH, getTheElement());
		this.cornerRadius = ctx.getCssDoubleValue(CSSConstants.LINK_CORNER_RADIUS, getTheElement());
	}

	protected void initRank() {
		String rank = getTheElement().getAttribute("rank");
		if (!"".equals(rank)) {
			this.rank = Integer.parseInt(rank);
		} else {
			// if rank isn't set, then connections are ranked in order from last to first..
			this.rank = indexOf(getTheElement(), getTheElement().getParentNode().getChildNodes());
		}
	}

	protected void initFromTo() {
		from = (Connected) ctx.getReferencedElement(this.fromId, getTheElement());
		to = (Connected) ctx.getReferencedElement(this.toId, getTheElement());
		
		if (from == null) {
			throw contextualException("Couldn't resolve 'from' reference for "+this.getID());
		}
		
		if (to == null) {
			throw contextualException("Couldn't resolve 'to' reference for "+this.getID());
		}
	}
	
	private void initDrawDirection() {
		drawDirection = (Direction) ctx.getCSSStyleProperty(CSSConstants.CONNECTION_DIRECTION, getTheElement());
	}

	private int indexOf(Element e, NodeList within) {
		for (int i = 0; i < within.getLength(); i++) {
			if (within.item(i) == e) {
				return i;
			}
		}
		
		return -1;
	}
	
	private String getReference(String css) {
		String reference = ctx.getReference(css, getTheElement());
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
			throw contextualException("otherEnd of neither from or to "+this+" "+end);
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
			return Direction.Companion.reverse(getDrawDirection());
		}
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
					getDOMElement().getOwnerDocument());
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
		
		return Direction.Companion.reverse(drawDirection);
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
