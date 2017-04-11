package org.kite9.diagram.common.elements.mapping;

import java.util.Collections;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.edge.SingleElementPlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Planarizer;

/**
 * This edge is created by the {@link Planarizer} to represent a connection.
 * 
 * @author robmoffat
 * 
 */
public class ConnectionEdge extends AbstractPlanarizationEdge implements SingleElementPlanarizationEdge {

	Connection underlying;

	public ConnectionEdge(Vertex from, Vertex to, Connection underlying, Direction d) {
		super(from, to, underlying.getFromDecoration(), underlying.getFromLabel(), underlying.getToDecoration(),
				underlying.getToLabel(), d);
		this.underlying = underlying;
	}
	
	private ConnectionEdge(Vertex from, Vertex to, Connection underlying, Object fromDecoration, 
			Label fromLabel, Object toDecoration, Label toLabel, Direction d, boolean reversed, boolean straight) {
		super(from, to, fromDecoration, fromLabel, toDecoration, toLabel, d);
		this.underlying = underlying;
		this.reversed = reversed;
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
		out[0] = new ConnectionEdge(getFrom(), toIntroduce, getOriginalUnderlying(), 
				getFromDecoration(), getFromLabel(),
				null, null, 
				getDrawDirection(), isReversed(), straight);
		out[1] = new ConnectionEdge(toIntroduce, getTo(), getOriginalUnderlying(), 
				null, null,
				getToDecoration(),getToLabel(), getDrawDirection(), isReversed(), straight);

		return out;
	}

	@Override
	public int getLengthCost() {
		return 1;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return getOriginalUnderlying() == de;
	}

	@Override
	public Map<DiagramElement, Direction> getDiagramElements() {
		return Collections.singletonMap(getOriginalUnderlying(), null);
	}
}