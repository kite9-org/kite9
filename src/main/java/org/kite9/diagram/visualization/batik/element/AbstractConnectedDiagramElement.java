package org.kite9.diagram.visualization.batik.element;

import java.util.Collection;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.style.ContainerPosition;
import org.kite9.diagram.style.GridContainerPosition;
import org.kite9.diagram.style.IntegerRange;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.kite9.framework.serialization.CSSConstants;

/**
 * Handles DiagramElements which are also Connnected.
 * 
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractConnectedDiagramElement extends AbstractRectangularDiagramElement implements Connected {
	
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
		initContainerPosition();
	}

	private void initContainerPosition() {
		if (getContainer() != null) {
			if (getContainer().getLayout() == Layout.GRID) {
				IntegerRange x = (IntegerRange) getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY);
				IntegerRange y = (IntegerRange) getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY);
				containerPosition = new GridContainerPosition(x, y);
			}
		}
	}

	private ContainerPosition containerPosition = null;
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
	public ContainerPosition getContainerPosition() {
		return containerPosition;
	}
	
}
