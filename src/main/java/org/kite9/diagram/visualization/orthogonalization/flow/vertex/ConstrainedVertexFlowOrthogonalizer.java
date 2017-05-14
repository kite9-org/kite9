package org.kite9.diagram.visualization.orthogonalization.flow.vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.fg.LinearArc;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.SimpleNode;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;
import org.kite9.diagram.visualization.orthogonalization.flow.OrthBuilder;
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;
import org.kite9.framework.logging.LogicException;

/**
 * This handles the creation of vertex nodes in the flow graph.  Where a vertex has > 1 directed edges entering or leaving, and
 * we can work out how many turns there are between these edges, we create extra vertex nodes with the correct number of 
 * available turns in its source. 
 *
 * 
 * @author robmoffat
 *
 */
public abstract class ConstrainedVertexFlowOrthogonalizer extends AbstractFlowOrthogonalizer {

	public ConstrainedVertexFlowOrthogonalizer(VertexArranger va) {
		super(va);
	}

	int nextId = 0;
	
	class VertexDivision {
		Edge from, to;
		List<Edge> containing = new LinkedList<Edge>();
		int corners;
		int id = nextId++;
		
		public boolean useFor(Edge from, Edge to){
			return ((this.from == from) || (containing.contains(from))) &&
				((this.to == to) || (containing.contains(to)));
		}
		
		@Override
		public String toString() {
			return "VertexDivision [corners=" + corners + ", from=" + from + ", id=" + id + ", to=" + to + ", containing="+containing+"]";
		}
		
		
	}
	
	private Map<Vertex, List<VertexDivision>> vertexDivisions = new HashMap<Vertex, List<VertexDivision>>();

	@Override
	protected void createFlowGraphForVertex(MappedFlowGraph fg, Face f, Node fn, Vertex v, Edge before, Edge after, Planarization pln) {
		Node vn = checkCreateVertexNode(pln, fg, v, before, after);
		Node hn = createHelperNode(fg, f, v, vn, before, after);
		log.send("Creating vertex "+v+" in portion "+fn);
		
		if (hn != null) {
			if (v.hasDimension()) {
				createDimensionedVertexHelperArcs(fg, fn, v, fn, before, after, hn, vn, pln);
			} else {
				createDimensionlessVertexHelperArcs(fg, fn, v, fn, before, after, hn, vn, pln);
			}
		}
	}
	

	protected Node checkCreateVertexNode(Planarization pln, MappedFlowGraph fg, Vertex v, Edge before, Edge after) {
		List<VertexDivision> divs = checkCreateVertexDivisions(pln, fg, v);
		Object memento = null;
		String id;
		int supply = 4;
		
		if (divs.size() > 1) {
			VertexDivision vd = getDivStartEdge(pln, divs, before, after, v);
			memento = new VertexPart(v, vd);
			id = "v[" + v.getID() + ":"+vd.id+"]";
			supply = vd.corners;
		} else {
			memento = v;
			id = "v[" + v.getID() + "]";
		}
		
		Node an = fg.getNodeFor(memento);
		
		if (an != null)
			return an;

		Node vn = new SimpleNode(id, supply, memento);
		vn.setType(VERTEX_NODE);
		fg.setNodeFor(memento, vn);
		log.send("Creating vertex node "+vn+" with supply "+supply+" between "+before+" and "+after);
		return vn;
	}

	private Integer countAntiClockwiseTurns(Vertex v, Edge before, Edge after, Planarization pln) {
		Direction d1 = before.getDrawDirectionFrom(v);
		Direction d2 = after.getDrawDirectionFrom(v);	
		EdgeOrdering eo = pln.getEdgeOrderings().get(v);
		
		
		
		if ((d1 == d2) && (eo.getEdgeDirections() instanceof Direction)) {
			return countAntiClockwiseTurnsByPlanarizationPosition(d1, pln, before, after, v);
		}
		
		int turns = 0;
		
		while (d1 != d2) {
			d1 = Direction.rotateAntiClockwise(d1);
			turns ++;
		}
		
		log.send("Anticlockwise Turns between "+before+" and "+after+" at "+v+" = "+turns);
		return turns;
	}

	/**
	 * Returns either zero or 4 turns, or null if the order can't be ascertained.
	 */
	
	private Integer countAntiClockwiseTurnsByPlanarizationPosition(Direction d1, Planarization pln, Edge before,
			Edge after, Vertex v) {
		
		if (notStraight(before) || notStraight(after)) {
			log.send("Not Setting Turns between "+before+" and "+after+" as edges aren't straight");
			return null;
		}
		
		
		
		Connected vUnd = (Connected) v.getOriginalUnderlying();
		@SuppressWarnings("unchecked")
		BiDirectional<Connected> beforeUnd = (BiDirectional<Connected>) before.getOriginalUnderlying();
		@SuppressWarnings("unchecked")
		BiDirectional<Connected> afterUnd = (BiDirectional<Connected>) after.getOriginalUnderlying();
		
		Connected beforeConnected = beforeUnd.otherEnd(vUnd);
		Connected afterConnected = afterUnd.otherEnd(vUnd);
		RoutingInfo beforeRI = pln.getPlacedPosition(beforeConnected);
		RoutingInfo afterRI = pln.getPlacedPosition(afterConnected);
		int xc = beforeRI.compareX(afterRI);
		int yc = beforeRI.compareY(afterRI);
		
		
		switch (d1) {
		case UP:
			switch (xc) {
				case -1:
					return 4;
				case 1:
					return 0;
				case 0:
					return null;
			}
			break;
		case DOWN:
			switch (xc) {
				case -1:
					return 0;
				case 1:
					return 4;
				case 0:
					return null;
			}
			break;
		case LEFT:
			switch (yc) {
				case -1:
					return 0;
				case 1:
					return 4;
				case 0:
					return null;
			}
		break;
		case RIGHT:
			switch (yc) {
				case -1:
					return 4;
				case 1:
					return 0;
				case 0:
					return null;
			}
			break;
		}
		
		throw new LogicException("Problem identifying turns = no direction");
	}


	private boolean notStraight(Edge before) {
		if (before instanceof PlanarizationEdge) {
			return !((PlanarizationEdge)before).isStraightInPlanarization();
		}
		
		return true;
	}


	private VertexDivision getDivStartEdge(Planarization pln, List<VertexDivision> divs, Edge before, Edge after, Vertex v) {
		for (VertexDivision vertexDivision : divs) {
			if (vertexDivision.useFor(after, before)) {
				return vertexDivision;
			}
		}
		
		throw new LogicException("Couldn't find correct division for "+before+" and "+after+" around "+v);
	}


	private List<VertexDivision> checkCreateVertexDivisions(Planarization pln, MappedFlowGraph fg, Vertex v) {
		List<VertexDivision> divs = vertexDivisions.get(v);
		if (divs == null) {
			
			// first, get only directed edges
			EdgeOrdering edgeOrdering = pln.getEdgeOrderings().get(v);
			if (edgeOrdering.getEdgeDirections() != null) {
				divs = createDirectedMap(v, edgeOrdering, pln);
				if (divs.size() > 0) {
					assertFourTurns(divs);
				}
			} else {
				// we can't subdivide the vertex
				divs = Collections.emptyList();
			}
			
			vertexDivisions.put(v, divs);
			log.send("----------------------");
			log.send("Vertex Divisions for "+v+": ", divs);
		}
		
		
		return divs;
		
	}
	
	private void assertFourTurns(List<VertexDivision> divs) {
		int total = 0;
		for (VertexDivision vertexDivision : divs) {
			total += vertexDivision.corners;
		}
		if (total != 4) {
			log.send("Vertex Division Problem: ", divs);
			throw new LogicException("Should be four turns around vertex");
		}
	}


	/**
	 * Means we get one vertex between each directed edge leaving it
	 */
	private List<VertexDivision> createDirectedMap(Vertex v, EdgeOrdering edgeOrdering, Planarization pln) {
		List<VertexDivision> out = new LinkedList<VertexDivision>();
		List<PlanarizationEdge> basic = createDirectedList(edgeOrdering);
		if (basic.size() < 2) {
			return Collections.emptyList();
		}
		List<PlanarizationEdge> edgesAsList = edgeOrdering.getEdgesAsList();
		int offset = edgesAsList.indexOf(basic.get(0));
		VertexDivision open = null;
		for (int current = 0; current < edgeOrdering.size(); current++) {
			int now = (current+offset+edgeOrdering.size()) % edgeOrdering.size();
			int next = (current+offset+1+edgeOrdering.size()) % edgeOrdering.size();
			Edge currentEdge = edgesAsList.get(now);
			Edge nextEdge = edgesAsList.get(next);
			if (isConstrained(currentEdge)) {
				open = new VertexDivision();
				open.from = currentEdge;
			}
			
			if (isConstrained(nextEdge)) {
				open.to = nextEdge;
				Integer turns = countAntiClockwiseTurns(v,  open.to, open.from, pln);
				if (turns == null) {
					return Collections.emptyList();
				}
				open.corners = turns;	
				out.add(open);
				open = null;
			} else {
				open.containing.add(nextEdge);
			}
		}
		
		return out;
	}


	private List<PlanarizationEdge> createDirectedList(EdgeOrdering edgeOrdering) {
		List<PlanarizationEdge> out = new ArrayList<>();
		for (Iterator<PlanarizationEdge> iterator =  edgeOrdering.getEdgesAsList().iterator(); iterator.hasNext();) {
			PlanarizationEdge edge = iterator.next();
			if (isConstrained(edge)) {
				out.add(edge);
			} 
		}
		return out;
	}

	/** 
	 * Where the vertex has no dimension, only 1 or 0
	 * corners can be pushed in or out, meaning that all edges entering the
	 * vertex must be on separate sides.
	 */
	protected void createDimensionlessVertexHelperArcs(MappedFlowGraph fg, Node p, Vertex v, Node fn, Edge before,
			Edge after, Node hn, Node vn, Planarization pln) {
		boolean canCorner = canCorner(v, before, after);
		log.send(log.go() ? null : "Dimensionless Vertex: "+v+" before: "+before+" after: "+after+" corners: "+1);
		if (canCorner) {
			LinearArc a4 = new LinearArc(TRACE, 1, -1, fn, hn, fn.getId() + "-" + hn.getId());
			addIfNotNull(fg, a4);
		}
		LinearArc a2 = new LinearArc(TRACE, 4, 0, vn, hn, vn.getId() + "-" + hn.getId());
		addIfNotNull(fg, a2);
	}

	/**
	 * Creates the two arcs from the helper node to the vertex and portion respectively.
	 * @param vn 
	 * @param a2 
	 */
	protected void createDimensionedVertexHelperArcs(MappedFlowGraph fg, Node p, Vertex v, Node fn, Edge before,
			Edge after, Node hn, Node vn, Planarization pln) {
		LinearArc a4 = new LinearArc(TRACE, 2, -2, fn, hn, fn.getId() + "-" + hn.getId());
		LinearArc a2 = new LinearArc(TRACE, 4, 0, vn, hn, vn.getId() + "-" + hn.getId());
		addIfNotNull(fg, a4);
		addIfNotNull(fg, a2);
	}
	
	/**
	 * Decide side for container edges and edge crossing vertexes
	 */
	private boolean canCorner(Vertex v, Edge before, Edge after) {
		if (hasSameUnderlying(before, after)) {
			if (before.getDrawDirectionFrom(v) != Direction.reverse(after.getDrawDirectionFrom(v))) {
				return true;
			}
			return false;
		}
		
		return true;
	}


	private boolean hasSameUnderlying(Edge before, Edge after) {
		return ((PlanarizationEdge) before).getDiagramElements().keySet()
				.equals(((PlanarizationEdge)after).getDiagramElements().keySet());
	} 

}
