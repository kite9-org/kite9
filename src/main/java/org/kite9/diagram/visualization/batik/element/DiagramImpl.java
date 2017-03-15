package org.kite9.diagram.visualization.batik.element;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.xml.StyledKite9SVGElement;

/**
 * This contains extra code relating to the Diagram itself, specifically, managing 
 * the two-way referencing of links between diagram element.
 * 
 * @author robmoffat
 *
 */
public class DiagramImpl extends ConnectedContainerImpl implements Diagram {
	
	public DiagramImpl(StyledKite9SVGElement el, Kite9BridgeContext ctx) {
		super(el, null, ctx);
	}
	
	private transient Map<String, Collection<Connection>> references = new HashMap<>();

	protected void addConnectionReference(Connection c) {
		String fromId = c.getFrom().getID();
		String toId = c.getTo().getID();
		getConnectionsFor(fromId).add(c);
		getConnectionsFor(toId).add(c);
	}

	@Override
	public Collection<Connection> getConnectionsFor(String id) {
		Collection<Connection> c = references.get(id);
		if (c == null) {
			c = new LinkedHashSet<>();
			references.put(id, c);
		}
		
		return c;
	}
	
}


