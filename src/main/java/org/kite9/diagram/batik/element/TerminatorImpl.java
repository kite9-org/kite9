package org.kite9.diagram.batik.element;

import org.apache.batik.anim.dom.SVGOMMarkerElement;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.xml.StyledKite9SVGElement;

public class TerminatorImpl extends AbstractRectangularDiagramElement implements Terminator {
	
	private SVGOMMarkerElement markerElement;
	private Value reference;

	public TerminatorImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Leaf> rp) {
		super(el, parent, ctx, rp);
	}

	@Override
	protected void initialize() {
		reference = theElement.getCSSStyleProperty(CSSConstants.MARKER_REFERENCE);
		if (reference != ValueConstants.NONE_VALUE) {
			Kite9DocumentLoader loader = (Kite9DocumentLoader) ctx.getDocumentLoader();
			markerElement = (SVGOMMarkerElement) loader.loadElementFromUrl(reference, theElement);
			if (markerElement != null) {
				double[] elemSizes = getSizesFromElement(markerElement);
				for (int i = 0; i < padding.length; i++) {
					padding[i] = Math.max(padding[i], elemSizes[i]);
				}
			}	
		} 
	}
	
	private double[] getSizesFromElement(SVGOMMarkerElement markerElement) {
		double markerWidth=markerElement.getMarkerWidth().getAnimVal().getValue();
		double markerHeight=markerElement.getMarkerHeight().getAnimVal().getValue();
		double refX=markerElement.getRefX().getAnimVal().getValue();
		double refY=markerElement.getRefY().getAnimVal().getValue();
		return new double[] {
			refY, 
			markerWidth - refX,
			markerHeight - refY,
			refX
		};
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
		return getPadding(Direction.LEFT) + getMargin(Direction.LEFT);
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

}
