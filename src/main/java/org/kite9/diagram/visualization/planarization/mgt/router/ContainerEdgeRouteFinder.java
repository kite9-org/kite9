package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.Set;

import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.ssp.State;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;

public class ContainerEdgeRouteFinder extends AbstractRouteFinder {

	public ContainerEdgeRouteFinder(MGTPlanarization p, RoutableReader rh, Edge e) {
		super(p, rh, e.getTo().getRoutingInfo(), getExpensiveAxis(e), getBoundedAxis(e), e);
		DiagramElement container = e.getOriginalUnderlying();
		this.epicentre = rh.getPlacedPosition(container);
		this.to = e.getTo();
		this.entryDirection = e.getDrawDirection();
		this.current = (Container) e.getOriginalUnderlying();
		this.parents = new UnorderedSet<Container>();
		while (container instanceof Contained) {
			container = ((Contained)container).getContainer();
			parents.add((Container)container);
		}
	}

	private static Axis getBoundedAxis(Edge e) {
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

	RoutingInfo epicentre;
	Vertex to;
	Container current;
	Set<Container> parents;

	
	@Override
	protected void createInitialPaths(State<LocatedEdgePath> pq) {
		Vertex from = e.getFrom();
		createInitialPathsFrom(pq, from);
	}

	private void createInitialPathsFrom(State<LocatedEdgePath> pq, Vertex from) {
		// remove backwards?
		generatePaths(null, p.getAboveBackwardLinks(from), pq, from,Going.BACKWARDS, PlanarizationSide.ENDING_ABOVE);
		generatePaths(null, p.getAboveForwardLinks(from), pq, from, Going.FORWARDS, PlanarizationSide.ENDING_ABOVE);
		generatePaths(null, p.getBelowBackwardLinks(from), pq, from, Going.BACKWARDS, PlanarizationSide.ENDING_BELOW);
		generatePaths(null, p.getBelowForwardLinks(from), pq, from, Going.FORWARDS, PlanarizationSide.ENDING_BELOW);
	}

	/**
	 * Rules for passing next to indvidual, unconnected vertices when doing container edges.
	 */
	protected boolean canTravel(int pathVertex, Going endingDirection, boolean pathAbove) {
		Vertex v = p.getVertexOrder().get(pathVertex);
		DiagramElement und = v.getOriginalUnderlying();
		boolean goingClockwise = (endingDirection == Going.FORWARDS) == pathAbove;

		if (und == e.getOriginalUnderlying()) {
			return isAboveAxisContainerEdge() == goingClockwise;
		}
		
		if (parents.contains(und) && rh.overlaps(v.getRoutingInfo(), to.getRoutingInfo())) {
			boolean out =  isAboveAxisContainerEdge() != goingClockwise;
			log.send(log.go() ? null : "Round Inside "+v+" "+und+" "+endingDirection+" "+(pathAbove ? "ABOVE " : "BELOW ")+(goingClockwise ? "CLOCK": "ANTI") +" result="+out);
			return out;
		}
		
		boolean elementWithinContainer = (und instanceof Contained) ? ((Contained) und).getContainer()==e.getOriginalUnderlying() : null;
		boolean elementWithinEpicentre = ((!(und instanceof Container)) && (v.getRoutingInfo() != null)) ? rh.isWithin(epicentre, v.getRoutingInfo()) : false;
			
		if (elementWithinContainer) {
			boolean out = isAboveAxisContainerEdge() == goingClockwise;
			log.send(log.go() ? null : "Round Inside "+v+" "+und+" "+endingDirection+" "+(pathAbove ? "ABOVE " : "BELOW ")+(goingClockwise ? "CLOCK": "ANTI") +" result="+out);
			return out;
		}
		
		if (elementWithinEpicentre) {
			// this can occur when a diagram element gets put in the wrong place due to a contradiction.  e.g. 33_13
			// I doubt this will work perfectly
			boolean out = isAboveAxisContainerEdge()  != goingClockwise;
			log.send(log.go() ? null : "Round Outside "+v+" "+und+" "+endingDirection+" "+(pathAbove ? "ABOVE " : "BELOW ")+(goingClockwise ? "CLOCK": "ANTI") +" result="+out);
			return out;
		}
			
		// otherwise, route is your own
		log.send(log.go() ? null : "Outside: "+v+" "+und);
		return true;
		
	}

	private boolean isAboveAxisContainerEdge(){
		return !e.isReversed();
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
	protected boolean canRouteToVertex(Vertex from, Edge outsideOf,
			boolean above, Going g, boolean arriving) {
		return true;
	}

	@Override
	protected boolean isTerminationVertex(int v) {
		return p.getVertexOrder().get(v) == to;
	}
	


}
