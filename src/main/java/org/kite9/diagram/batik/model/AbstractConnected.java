package org.kite9.diagram.batik.model;

import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.ConnectionAlignment;
import org.kite9.diagram.model.style.ConnectionsSeparation;
import org.kite9.diagram.model.style.ContentTransform;

import java.util.Collection;

/**
 * Handles DiagramElements which are also Connnected.
 * 
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractConnected extends AbstractCompactedRectangular implements Connected {
	
	public AbstractConnected(StyledKite9XMLElement el, DiagramElement parent, ElementContext ctx, Painter rp, ContentTransform t) {
		super(el, parent, ctx, rp, t);
	}
	
	/**
	 * Call this method prior to using the functionality, so that we can ensure 
	 * all the members are set up correctly.
	 */
	protected void initialize() {
		super.initialize();
		linkGutter = ctx.getCssDoubleValue(CSSConstants.LINK_GUTTER, getTheElement());
		linkInset = ctx.getCssDoubleValue(CSSConstants.LINK_INSET, getTheElement());
		initConnectionAlignment();
	}


	protected void initConnectionAlignment() {
		alignments = new ConnectionAlignment[] {
				ctx.getConnectionAlignment(CSSConstants.CONNECTION_ALIGN_TOP_PROPERTY, getTheElement()),
				ctx.getConnectionAlignment(CSSConstants.CONNECTION_ALIGN_RIGHT_PROPERTY, getTheElement()),
				ctx.getConnectionAlignment(CSSConstants.CONNECTION_ALIGN_BOTTOM_PROPERTY, getTheElement()),
				ctx.getConnectionAlignment(CSSConstants.CONNECTION_ALIGN_LEFT_PROPERTY, getTheElement()),
		};
	}


	private Collection<Connection> links;
	private double linkGutter;
	private double linkInset;
	private ConnectionAlignment[] alignments;

	@Override
	public Collection<Connection> getLinks() {
		ensureInitialized();
		if (links == null) {
			links = getDiagram().getConnectionsFor(this);
		}
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
		ConnectionsSeparation out = (ConnectionsSeparation) ctx.getCSSStyleProperty(CSSConstants.CONNECTIONS_PROPERTY, getTheElement());
		return out;
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
