package org.kite9.diagram.visualization.planarization.mgt.router;

import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.framework.common.Kite9ProcessingException;

public class ContainerEdgeRouteFinder extends AbstractRouteFinder {

	public ContainerEdgeRouteFinder(MGTPlanarization p, RoutableReader rh, BorderEdge e) {
		super(p, rh, e.getTo().getRoutingInfo(), getExpensiveAxis(e), getBoundedAxis(e), e);
		this.to = e.getTo();
		this.pathDirection = e.getDrawDirection();
		this.entryDirection = this.pathDirection;
		this.exitDirection = this.pathDirection;
	}

	private static Axis getBoundedAxis(Edge e) {
		Direction edgeDir = e.getDrawDirection();
		
		switch (edgeDir) {
		case UP:
		case DOWN:
			return Axis.VERTICAL;
		case LEFT:
		case RIGHT:
			return Axis.HORIZONTAL;
		}
		
		return null;
	}

	private static Axis getExpensiveAxis(Edge e) {
		Direction edgeDir = e.getDrawDirection();
		
		switch (edgeDir) {
		case UP:
		case DOWN:
			return Axis.HORIZONTAL;
		case LEFT:
		case RIGHT:
			return Axis.VERTICAL;
		}
		
		return null;
	}

	Vertex to;
	
	@Override
	protected void createInitialPaths(State<LocatedEdgePath> pq) {
		Vertex from = e.getFrom();
		createInitialPathsFrom(pq, from);
	}

	private void createInitialPathsFrom(State<LocatedEdgePath> pq, Vertex from) {
		// remove backwards?
		switch (e.getDrawDirection()) {
		case LEFT: 
		case UP:
			generatePaths(null, p.getAboveBackwardLinks(from), pq, from,Going.BACKWARDS, PlanarizationSide.ENDING_ABOVE);
			generatePaths(null, p.getBelowBackwardLinks(from), pq, from, Going.BACKWARDS, PlanarizationSide.ENDING_BELOW);
			break;
		case DOWN:
		case RIGHT:
			generatePaths(null, p.getAboveForwardLinks(from), pq, from, Going.FORWARDS, PlanarizationSide.ENDING_ABOVE);
			generatePaths(null, p.getBelowForwardLinks(from), pq, from, Going.FORWARDS, PlanarizationSide.ENDING_BELOW);
		
		}
	}

	
	
	/**
	 * Rules for passing next to indvidual, unconnected vertices when doing container edges.
	 */
	protected boolean canTravel(int pathVertex, Going endingDirection, boolean pathAbove) {
		switch (e.getDrawDirection()) {
		case LEFT: 
		case UP:
			return endingDirection == Going.BACKWARDS;
		case DOWN:
		case RIGHT:
			return endingDirection == Going.FORWARDS;
		}
		
		throw new Kite9ProcessingException("Was expecting a direction for the container border edge");
	}
	
	@Override
	protected boolean canAddToQueue(LocatedEdgePath ep) {
		return true;
	}

	@Override
	protected boolean canCross(Edge edge, EdgePath forward, boolean above) {
		return false;
	}

	@Override
	protected boolean canRouteToVertex(Vertex from, PlanarizationEdge outsideOf,
			boolean above, Going g, boolean arriving) {
		return true;
	}

	@Override
	protected boolean isTerminationVertex(int v) {
		return p.getVertexOrder().get(v) == to;
	}
	


}
