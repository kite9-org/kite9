package org.kite9.diagram.batik.element;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.layers.AbstractLayerCreator;
import org.kite9.diagram.batik.layers.GraphicsLayerName;
import org.kite9.diagram.batik.layers.LayerCreator;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

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
	
	private static final LayerCreator EMPTY_LAYER_CREATOR = new AbstractLayerCreator() {

		@Override
		protected List<GraphicsNode> initSVGGraphicsContents(Element theElement, Kite9BridgeContext ctx, DiagramElement de) {
			return Collections.emptyList();
		}
	};
	
	/**
	 * For the diagram itself, we have to create empty CompoundGraphicsNodes for all required layers. 
	 */
	protected GraphicsNode initGraphicsForLayer(GraphicsLayerName name) {
		initializeChildXMLElements();
		if (name == GraphicsLayerName.MAIN) {
			return name.createLayer(getID(), ctx, theElement, this);
		} else {
			return EMPTY_LAYER_CREATOR.createLayer(getID(), ctx, theElement, name, this);
		}
	}
	
}


