package org.kite9.diagram.visualization.orthogonalization.flow.balanced;

import java.util.List;

import org.kite9.diagram.common.algorithms.fg.AbsoluteArc;
import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.LinearArc;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.vertex.ConnectedVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;
import org.kite9.diagram.visualization.orthogonalization.flow.face.ConstrainedFaceFlowOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;

/**
 * Implements several balancing improvements to multi-edge. These are as
 * follows:
 * <ol>
 * <li>
 * The system will prefer to balance edges around a vertex, so that there is one
 * edge on each side before adding more to any side (a la Tamassia). Indeed, a
 * bend should be preferred over a vertex having two edges on the same side.</li>
 * <li>
 * The system respects arrow heads, so that a directed arrow body has edges
 * leaving opposite ends</li>
 * <li>
 * If an arrow does not have heads/tails set, then the ends of the arrow should
 * be on opposite sides of the arrow body</li>
 * <li>Corners in containers should cost more than corners outside containers</li>
 * </ol>
 * 
 * @author robmoffat
 * 
 */
public class BalancedFlowOrthogonalizer extends ConstrainedFaceFlowOrthogonalizer {
	

	public BalancedFlowOrthogonalizer(VertexArranger va, EdgeConverter clc) {
		super(va, clc);
	}

	public static final int UNBALANCED_VERTEX_COST = 4 * CORNER;

	public static enum BalanceChoice {

		SAME_SIDE_PREFERRED(0), 
		DIFFERENT_SIDE_PREFFERED_LAYOUT(16),
		DIFFERENT_SIDE_PREFFERED_DIRECTED(8),
		DIFFERENT_SIDE_PREFFERED_MINOR(4),
		OPPOSITE_SIDE_PREFERRED(6);

		int cost;
		
		BalanceChoice(int cost) {
			this.cost = cost;
		
		}
		
		public int getCost() {
			return cost * CORNER;
		}
	}

	/**
	 * Extends creation for special arrow rules. The head of the arrow and the
	 * tail(s) must be on opposite sides of the arrow. To ensure this, we look
	 * for cases where there is one head or one tail arrow. This is manipulated
	 * so that only helper nodes to the sides of the one arrow are allowed to
	 * receive corners.
	 */
	@Override
	protected void createDimensionedVertexHelperArcs(MappedFlowGraph fg, Node p, Vertex v, Node fn, Edge before,
			Edge after, Node hn, Node vn, Planarization pln) {
		
		if (before==after) {
			super.createDimensionedVertexHelperArcs(fg, p, v, fn, before, after, hn, vn, pln);
			return;
		}

		// side being true indicates that we want the edges to be on opposite
		// sides of the vertex.
		// if side is false, then we just want them on different sides, not
		// necessarily opposite
		BalanceChoice side = BalanceChoice.DIFFERENT_SIDE_PREFFERED_MINOR;

		side = decideSide(v, fn, before, after, hn, fg.getPlanarization().getEdgeOrderings().get(v).getEdgesAsList());

		log.send(log.go() ? null : "V: "+v+" Between Edge "+before+" and "+after+": "+side);
		
		Arc portionArc = createBalancedPortionArc(fn, hn, side);
		Arc vertexArc = new LinearArc(TRACE, 4, 0, vn, hn, vn.getId() + "-" + hn.getId());
		addIfNotNull(fg, portionArc);
		addIfNotNull(fg, vertexArc);
	}

	protected Arc createBalancedPortionArc(Node fn, Node hn, BalanceChoice side) {
		switch (side) {
			
		case OPPOSITE_SIDE_PREFERRED:
			// this arc has a high cost if you try and put anything into it
			Arc a4 = new AbsoluteArc(side.getCost(), 2, fn, hn, fn.getId() + "-" + hn.getId());
			return a4;

		case DIFFERENT_SIDE_PREFFERED_LAYOUT:
		case DIFFERENT_SIDE_PREFFERED_MINOR:
		case DIFFERENT_SIDE_PREFFERED_DIRECTED:
			// this arc is very happy with you pushing -1, 0 or 1 edge from the
			// face. You can push 2 or -2, but this would
			// mean that you end up with edges on the same side of the vertex.
			Arc a5 = new StepCostArc(TRACE, 2, fn, hn, fn.getId() + "-" + hn.getId(), 1, side.getCost());
			return a5;

		case SAME_SIDE_PREFERRED:
		default:

			// this arc is minimal cost so you can push 2 into it and have edges
			// on the same side as each other
			Arc a6 = new AbsoluteArc(TRACE, 2, fn, hn, fn.getId() + "-" + hn.getId());
			return a6;

		}
	}

	private boolean opposite(Edge a, Edge b) {
		Pair<Terminator> aStyle = getEdgeStyle(a);
		Pair<Terminator> bStyle = getEdgeStyle(b);

		if (aStyle.equals(bStyle)) {
			return false;
		}

		return true;
	}

	protected BalanceChoice decideSide(Vertex v, Node fn, Edge before, Edge after, Node hn, List<? extends Edge> listOfEdges) {
		// where only 2 edges, make the arrow ends come out opposite, preferably		
		if (((PlanarizationEdge)before).isLayoutEnforcing() || ((PlanarizationEdge)after).isLayoutEnforcing())  {
			return BalanceChoice.DIFFERENT_SIDE_PREFFERED_LAYOUT;
		}  

		if (Tools.isUnderlyingContradicting(before) || Tools.isUnderlyingContradicting(after)) {
			// short-cuts the process if there is a contradiction
			return BalanceChoice.SAME_SIDE_PREFERRED;
		}
		
		if (isStraight(before) || isStraight(after)) {
			return BalanceChoice.DIFFERENT_SIDE_PREFFERED_DIRECTED;
		}
		
		// this is used for arrows - try and make heads and tails appear opposite sides
		if ((v instanceof ConnectedVertex) && (((ConnectedVertex)v).isSeparatingConnections())) {
			if (listOfEdges.size() <= 2) {
				// place edges on opposite sides
				return BalanceChoice.OPPOSITE_SIDE_PREFERRED;
			}
			
			if (opposite(before, after)) {
				return BalanceChoice.OPPOSITE_SIDE_PREFERRED;
			} 
		}
		
		// for four edges or less, try and get them all on different sides, as that 
		// deforms the arrows less
		if (listOfEdges.size() <= 4) {
			// place each edge on a different side, ideally
			return BalanceChoice.DIFFERENT_SIDE_PREFFERED_MINOR;
		}
		
		return BalanceChoice.SAME_SIDE_PREFERRED;
	}

	private boolean isStraight(Edge before) {
		return before.getDrawDirection() != null && (!Tools.isUnderlyingContradicting(before));
	}

	private TerminatorPair getEdgeStyle(Edge en) {
		if (en instanceof BiDirectionalPlanarizationEdge) {
			DiagramElement und = ((BiDirectionalPlanarizationEdge) en).getOriginalUnderlying();
			if (und instanceof Connection) {
				return new TerminatorPair(((Connection)und).getFromDecoration(), ((Connection)und).getToDecoration());
			}
		} 
		
		return new TerminatorPair(null, null);
	}

	@Override
	protected int weightCost(Edge e) {
		if (e instanceof ConnectionEdge) {
			// this tries to keep corners inside containers
			return getDepthBasedWeight(e);
		}

		return super.weightCost(e);
	}

	private int getDepthBasedWeight(Edge e) {
		int depthFrom = getVertexMaxDepth(e.getFrom());
		int depthTo = getVertexMaxDepth(e.getTo());
		int depth = Math.max(depthFrom, depthTo);
		int orig = super.weightCost(e);
		return orig -  (depth * 10) ;
	}

	private int getVertexMaxDepth(Vertex v) {
		return v.getDiagramElements().stream().map(de -> de.getDepth()).reduce(0, (a,b) -> Math.max(a, b)).intValue();
	}

}