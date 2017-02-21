package org.kite9.diagram.visualization.batik.element;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.sizing.AdaptiveSizedGraphics;
import org.kite9.diagram.adl.sizing.HasLayeredGraphics;
import org.kite9.diagram.adl.sizing.ScaledGraphics;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.StyledKite9SVGElement;

/**
 * This contains extra code relating to the Diagram itself, specifically, managing 
 * the two-way referencing of links between diagram element.
 * 
 * @author robmoffat
 *
 */
public class DiagramImpl extends AbstractConnectedDiagramElement implements Diagram, ScaledGraphics {
	
	public DiagramImpl(StyledKite9SVGElement el, Kite9BridgeContext ctx) {
		super(el, null, ctx);
	}
	
	private transient Map<String, Collection<Connection>> references = new HashMap<>();
	Label label;


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
	
	/**
	 * Is able to build a group for each layer
	 */
	@Override
	protected GraphicsNode initGraphicsForLayer(GraphicsLayerName name) {
		IdentifiableGraphicsNode ign = createGraphicsNode(name);
//		ign.setId(name.name());
//		for (DiagramElement	de : getContents()) {
//			if (de instanceof HasLayeredGraphics) {
//				GraphicsNode graphicsForLayer = ((HasLayeredGraphics) de).getGraphicsForLayer(name);
//				if (graphicsForLayer != null) {
//					ign.add(graphicsForLayer);
//				}
//			}
//		}
		return ign;
	}

	@Override
	public boolean isBordered() {
		return false;
	}

	@Override
	public Label getLabel() {
		ensureInitialized();
		return label;
	}

	@Override
	protected void addLabelReference(Label de) {
		this.label = de;
	}
	
}


