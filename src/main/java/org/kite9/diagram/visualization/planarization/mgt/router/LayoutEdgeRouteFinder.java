package org.kite9.diagram.visualization.planarization.mgt.router;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertices;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;

/**
 * For layout edges, we usually know which vertex the connection must go from and to. 
 * So limit the search to just those.
 * 
 * @author robmoffat
 *
 */
public class LayoutEdgeRouteFinder extends ConnectionEdgeRouteFinder {

	public LayoutEdgeRouteFinder(MGTPlanarization p, RoutableReader rh, Edge ci, ElementMapper em, Direction edgeDir) {
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
		
		if (und instanceof Container) {
			Container c = (Container) und;
			ContainerVertices cvs = em.getContainerVertices(c);
			ContainerVertex leaver = cvs.getCentralVertexOnSide(d);
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
