package org.kite9.diagram.visualization.planarization.mgt.router;

import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.mapping.ContainerLayoutEdge;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;

/**
 * For layout edges, we usually know which vertex the connection must go from and to. 
 * So limit the search to just those.
 * 
 * @author robmoffat
 *
 */
public class LayoutEdgeRouteFinder extends AbstractTempEdgeRouteFinder2 {

	public LayoutEdgeRouteFinder(MGTPlanarization p, RoutableReader rh, ContainerLayoutEdge ci, ElementMapper em, Direction edgeDir) {
		super(p, rh, ci, em, edgeDir, CrossingType.STRICT, GeographyType.STRICT);
		this.start = identifyActualVertex(ci.getFrom(), ci.getDrawDirection());
		this.destination = identifyActualVertex(ci.getTo(), Direction.reverse(ci.getDrawDirection()));
	}
	
	Vertex start, destination;
	
	@Override
	protected void createInitialPaths(State<LocatedEdgePath> pq) {
		createInitialPathsFrom(pq, start);
	}

	private Vertex identifyActualVertex(Vertex edgeVertex, Direction d) {
		DiagramElement und = edgeVertex.getOriginalUnderlying();
		
		if (em.hasOuterCornerVertices(und)) {
			Container c = (Container) und;
			CornerVertices cvs = em.getOuterCornerVertices(c);
			MultiCornerVertex leaver = cvs.createVertex(MultiCornerVertex.getOrdForXDirection(d), MultiCornerVertex.getOrdForYDirection(d));
			return leaver;
		} else {
			return edgeVertex;
		}
	}

	@Override
	protected boolean isTerminationVertex(int v) {
		Vertex candidate = p.getVertexOrder().get(v);
		return candidate == destination;
	}

	@Override
	protected boolean allowConnectionsToContainerContents() {
		return false;
	}
	
	
	
}
