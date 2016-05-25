package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.framework.logging.LogicException;

public class FaceMappedFlowGraph extends MappedFlowGraph {

	Map<Face, List<PortionNode>> facePortionMap;
	
	public FaceMappedFlowGraph(Planarization pln) {
		super(pln);
	}

	public void setFacePortionMap(Map<Face, List<PortionNode>> facePortionMap2) {
		this.facePortionMap = facePortionMap2;
	}

	@Override
	public Collection<Node> getNodesForEdgePart(Face f, Edge e, Vertex startVertex) {
		List<PortionNode> faceNodes = facePortionMap.get(f);
		Collection<Node> out = new UnorderedSet<Node>(faceNodes.size()*2);
		int pos = f.indexOf(startVertex, e);
		for (PortionNode node : faceNodes) {
			if (node.containsFacePart(pos)) {
				out.add(node);
			}
		}

		if (out.size() == 0)
			throw new LogicException("Could not find portion for face " + f.getId() + " edge " + e + " start "
					+ startVertex + " at pos " + pos);

		if (out.size() > 2) {
			throw new LogicException("Only two portions should ever meet on the same side of an edge: " + out + " for "
					+ e + " going from " + startVertex);
		}

		return out;
	}
}
