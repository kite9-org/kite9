package org.kite9.diagram.visualization.orthogonalization.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.FlowGraph;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;

/**
 * Contains the mapping of the flow graph to the multi-edge methodology.
 * <ul>
 * <li>Face nodes are connected to portion nodes</li>
 * <li>Portion nodes are connected to helper nodes and edge nodes</li>
 * <li>Helper nodes are connected to vertex nodes</li>
 * </ul>
 * 
 * Faces push 4 or -4 (outer faces) corners through the portions, and into the
 * helper nodes of vertices. They can push to other portions via edges.
 * 
 * @author robmoffat
 * 
 */
public abstract class MappedFlowGraph extends FlowGraph {

	@Override
	public String toString() {
		return super.toString() + "\n" + outputValueArcs();
	}

	public String outputValueArcs() {
		StringBuilder sb = new StringBuilder();
		for (Arc a : getValueArcs()) {
			sb.append(a);
			sb.append(":(");
			sb.append(a.getFlow());
			sb.append(")\n");
		}
		return sb.toString();
	}

	public Set<Arc> getValueArcs() {
		Set<Arc> out = new UnorderedSet<Arc>();
		for (Arc arc : allArcs) {
			if (arc.getFlow() == 0) {
				// ignore
			} else {
				out.add(arc);
			}
		}
		return out;
	}

	private Planarization planarization;

	public Planarization getPlanarization() {
		return planarization;
	}

	public MappedFlowGraph(Planarization pln) {
		super(new ArrayList<Node>());
		this.planarization = pln;
	}

	private Map<Object, Node> map = new HashMap<Object, Node>();
	
	public Node getNodeFor(Object o) {
		return map.get(o);
	}
	
	public void setNodeFor(Object o, Node n) {
		map.put(o, n);
		allNodes.add(n);
		allArcs.addAll(n.getArcs());
	}
	
	public abstract Collection<Node> getNodesForEdgePart(Face f, Edge e, Vertex startVertex);
}
