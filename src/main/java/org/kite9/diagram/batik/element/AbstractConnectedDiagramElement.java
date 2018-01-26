package org.kite9.diagram.batik.element;

import java.util.Collection;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.RectangularPainter;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.ConnectionAlignment;
import org.kite9.diagram.model.style.ConnectionsSeparation;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Handles DiagramElements which are also Connnected.
 * 
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractConnectedDiagramElement extends AbstractCompactedRectangularDiagramElement implements Connected {
	
	public AbstractConnectedDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<?> rp) {
		super(el, parent, ctx, rp);
	}
	
	/**
	 * Call this method prior to using the functionality, so that we can ensure 
	 * all the members are set up correctly.
	 */
	protected void initialize() {
		super.initialize();
		Diagram d = getDiagram();
		links = d.getConnectionsFor(this.getID());
		linkGutter = theElement.getCSSStyleProperty(CSSConstants.LINK_GUTTER).getFloatValue();
		linkInset = theElement.getCSSStyleProperty(CSSConstants.LINK_INSET).getFloatValue();
		initConnectionAlignment();
	}


	protected void initConnectionAlignment() {
		alignments = new ConnectionAlignment[] {
				getConnectionAlignment(theElement, CSSConstants.CONNECTION_ALIGN_TOP_PROPERTY),
				getConnectionAlignment(theElement, CSSConstants.CONNECTION_ALIGN_RIGHT_PROPERTY),
				getConnectionAlignment(theElement, CSSConstants.CONNECTION_ALIGN_BOTTOM_PROPERTY),
				getConnectionAlignment(theElement, CSSConstants.CONNECTION_ALIGN_LEFT_PROPERTY),			
		};
	}


	public static ConnectionAlignment getConnectionAlignment(StyledKite9SVGElement theElement, String prop) {
		Value v = theElement.getCSSStyleProperty(prop);
		
		if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
			return new ConnectionAlignment(ConnectionAlignment.Measurement.PERCENTAGE, v.getFloatValue());
		} else if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
			return new ConnectionAlignment(ConnectionAlignment.Measurement.PIXELS, v.getFloatValue());
		}
		
		return ConnectionAlignment.NONE;
	}


	private Collection<Connection> links;
	private double linkGutter;
	private double linkInset;
	private ConnectionAlignment[] alignments;

	@Override
	public Collection<Connection> getLinks() {
		ensureInitialized();
		return links;
	}

	public Connection getConnectionTo(Connected c) {
		for (Connection link : getLinks()) {
			if (link.meets(c)) {
				return link;
			}
		}

		return null;
	}

	public boolean isConnectedDirectlyTo(Connected c) {
		return getConnectionTo(c) != null;
	}

	@Override
	public ConnectionsSeparation getConnectionsSeparationApproach() {
		EnumValue ev = (EnumValue) getCSSStyleProperty(CSSConstants.CONNECTIONS_PROPERTY);
		return (ConnectionsSeparation) ev.getTheValue();
	}

	@Override
	public double getLinkGutter() {
		return linkGutter;
	}

	@Override
	public double getLinkInset() {
		return linkInset;
	}

	@Override
	public ConnectionAlignment getConnectionAlignment(Direction d) {
		ensureInitialized();
		return alignments[d.ordinal()];
	}
	
	

}
