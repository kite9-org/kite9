package org.kite9.diagram.dom.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.style.ContentTransform;

/**
 * This contains extra code relating to the Diagram itself, specifically, managing 
 * the two-way referencing of links between diagram element.
 * 
 * @author robmoffat
 *
 */
public class DiagramImpl extends ConnectedContainerImpl implements Diagram {
	
	public DiagramImpl(StyledKite9XMLElement el, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
		super(el, null, ctx, rp, t);
	}
	
	private transient List<Connection> connections = new ArrayList<>();

	protected void registerConnection(Connection c) {
		connections.add(c);
	}

	public Collection<Connection> getConnectionsFor(Connected c) {
		Collection<Connection> out = new ArrayList<Connection>();
		for (Connection co : connections) {
			if ((co.getFrom()==c) || (co.getTo() == c)) {
				out.add(co);
			}
		}		
		return out;
	}
	
	
}


