package org.kite9.diagram.batik.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.painter.Painter;
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
	
	public DiagramImpl(StyledKite9SVGElement el, Kite9BridgeContext ctx, Painter rp, ContentTransform t) {
		super(el, null, ctx, rp, t);
	}
	
	private transient Map<String, Collection<Connection>> references = new HashMap<>();

	protected void addConnectionReference(String id, Connection c) {
		getConnectionsFor(id).add(c);
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


