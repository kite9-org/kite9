package org.kite9.diagram.visualization.orthogonalization.edge;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;

/**
 * Holds info about the darts due to an incoming edge.
 */
public class IncidentDart {
	
	final Dart d;
	final Vertex external;
	final Vertex internal;
	final Direction arrivalSide;
	final PlanarizationEdge dueTo;
	
	public IncidentDart(Dart d, Vertex external, Vertex internal, Direction arrivalSide, PlanarizationEdge e) {
		super();
		this.d = d;
		this.external = external;
		this.internal = internal;
		this.arrivalSide = arrivalSide;
		this.dueTo = e;
	}
	

	public Dart getDart() {
		return d;
	}

	public Vertex getExternal() {
		return external;
	}

	public Vertex getInternal() {
		return internal;
	}

	public Direction getArrivalSide() {
		return arrivalSide;
	}

	public PlanarizationEdge getDueTo() {
		return dueTo;
	}

	
}