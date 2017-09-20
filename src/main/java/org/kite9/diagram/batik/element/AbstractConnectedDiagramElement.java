package org.kite9.diagram.batik.element;

import java.util.Collection;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.ConnectionsSeparation;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * Handles DiagramElements which are also Connnected.
 * 
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractConnectedDiagramElement extends AbstractCompactedRectangularDiagramElement implements Connected {
	
	public AbstractConnectedDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}
	
	/**
	 * Call this method prior to using the functionality, so that we can ensure 
	 * all the members are set up correctly.
	 */
	protected void initialize() {
		super.initialize();
		Diagram d = getDiagram();
		links = d.getConnectionsFor(this.getID());
	}


	private Collection<Connection> links;

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
	
	

}
