package org.kite9.diagram.common.elements.mapping;

import java.util.Collections;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Planarizer;

/**
 * This edge is created by the {@link Planarizer} to represent a connection.
 * 
 * @author robmoffat
 * 
 */
public class ConnectionEdge extends AbstractPlanarizationEdge implements BiDirectionalPlanarizationEdge {

	final Connection underlying;
	final Connected fromUnderlying;
	final Connected toUnderlying;

	public ConnectionEdge(Vertex from, Vertex to, Connection underlying, Direction d) {
		this(from, to,  d, true, underlying, underlying.getFrom(), underlying.getTo());
	}

	public ConnectionEdge(Vertex from, Vertex to, Direction d, boolean straight, Connection underlying, Connected fromUnderlying, Connected toUnderlying) {
		super(from, to, d);
		this.fromUnderlying = fromUnderlying;
		this.toUnderlying = toUnderlying;
		this.underlying = underlying;
		this.straight = straight;
	}



	public Connection getOriginalUnderlying() {
		return underlying;
	}

	@Override
	public int getCrossCost() {
		return 500;		
	}

	@Override
	public RemovalType removeBeforeOrthogonalization() {
		return RemovalType.NO;
	}

	private boolean layoutEnforcing = false;
	
	public boolean isLayoutEnforcing() {
		return layoutEnforcing;
	}

	public void setLayoutEnforcing(boolean le) {
		this.layoutEnforcing = le;
	}

	@Override
	public PlanarizationEdge[] split(Vertex toIntroduce) {
		PlanarizationEdge[] out = new PlanarizationEdge[2];
		out[0] = new ConnectionEdge(getFrom(), toIntroduce, getDrawDirection(), straight, getOriginalUnderlying(), 
				getFromConnected(), null);
		out[1] = new ConnectionEdge(toIntroduce, getTo(), getDrawDirection(), straight, getOriginalUnderlying(), null, getToConnected()); 

		return out;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return getOriginalUnderlying() == de;
	}

	@Override
	public Map<DiagramElement, Direction> getDiagramElements() {
		return Collections.singletonMap(getOriginalUnderlying(), null);
	}
	
	public Connected getFromConnected() {
		return fromUnderlying;
	}
	
	public Connected getToConnected() {
		return toUnderlying;
	}
}