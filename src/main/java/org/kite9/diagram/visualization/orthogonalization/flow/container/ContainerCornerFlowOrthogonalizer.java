package org.kite9.diagram.visualization.orthogonalization.flow.container;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.LinearArc;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;
import org.kite9.diagram.visualization.orthogonalization.flow.OrthBuilder;
import org.kite9.diagram.visualization.orthogonalization.flow.balanced.BalancedFlowOrthogonalizer;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.diagram.visualization.planarization.mgt.ContainerBorderEdge;

/**
 * Handles the case where a corner of a container has several edges arriving at it.
 * 
 * The approach taken is basically the same as a dimensioned vertex.
 * 
 * @author robmoffat
 *
 */
public class ContainerCornerFlowOrthogonalizer extends BalancedFlowOrthogonalizer {

	public ContainerCornerFlowOrthogonalizer(OrthBuilder<MappedFlowGraph> fb) {
		super(fb);
	}

	@Override
	protected void createFlowGraphForVertex(MappedFlowGraph fg, Face f, Node fn, Vertex v, Edge before, Edge after,
			Planarization pln) {
		if (v instanceof ContainerVertex) {
			Node vn = checkCreateVertexNode(pln, fg, v, before, after);
			Node hn = createHelperNode(fg, f, v, vn, before, after);
			log.send("Creating container vertex "+v+" in portion "+fn);
			
			if (hn != null) {
				createContainerCornerVertexHelperArcs(fg, fn, (ContainerVertex) v, fn, before, after, hn, vn, pln);
			}
			
			
		} else {
			super.createFlowGraphForVertex(fg, f, fn, v, before, after, pln);
		}
	}


	protected void createContainerCornerVertexHelperArcs(MappedFlowGraph fg, Node p, ContainerVertex v, Node fn, Edge before,
			Edge after, Node hn, Node vn, Planarization pln) {
		
		if (before==after) {
			super.createDimensionedVertexHelperArcs(fg, p, v, fn, before, after, hn, vn, pln);
			return;
		}
		
		BalanceChoice side;
		
		if ((before instanceof ContainerBorderEdge) || (after instanceof ContainerBorderEdge)) {
            // different side is always true if we are dealing with an edge of the container
            side = BalanceChoice.DIFFERENT_SIDE_PREFFERED_LAYOUT;
		} else {
			side = decideSide(v, fn, before, after, hn, fg.getPlanarization().getEdgeOrderings().get(v).getEdgesAsList());
		}

		log.send(log.go() ? null : "V: "+v+" Between Edge "+before+" and "+after+": "+side);
		
		Arc portionArc = createBalancedPortionArc(fn, hn, side);
		Arc vertexArc = new LinearArc(TRACE, 4, 0, vn, hn, vn.getId() + "-" + hn.getId());
		addIfNotNull(fg, portionArc);
		addIfNotNull(fg, vertexArc);
	}
}
