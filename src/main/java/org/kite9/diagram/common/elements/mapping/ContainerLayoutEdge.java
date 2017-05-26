package org.kite9.diagram.common.elements.mapping;

import java.util.Collections;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;

/**
 * This edge is used to enforce a particular layout within a container.
 * 
 * It is used between the sibling contents of the container.
 * 
 * @author robmoffat
 *
 */
public class ContainerLayoutEdge extends AbstractPlanarizationEdge implements BiDirectionalPlanarizationEdge {
	
	final GeneratedLayoutElement underlying;
	final Connected fromUnderlying;
	final Connected toUnderlying;
	
	public ContainerLayoutEdge(Vertex from, Vertex to, Direction d, Connected fromElement, Connected toElement) {
		this(from, to, d, true, new GeneratedLayoutElement(fromElement, toElement, d), fromElement, toElement);
	}
	
	private ContainerLayoutEdge(Vertex from, Vertex to, Direction drawDirection, boolean straight, GeneratedLayoutElement underlying, Connected fromC, Connected toC) {
		super(from, to, drawDirection);
		this.straight = straight;
		this.underlying = underlying;
		this.fromUnderlying = fromC;
		this.toUnderlying = toC;
	}

	public DiagramElement getOriginalUnderlying() {
		return underlying;
	}

	@Override
	public int getCrossCost() {
		return 0;	// no cost for traversing between items in the layout
	}

	@Override
	public RemovalType removeBeforeOrthogonalization() {
		// can be removed if there is another edge to do the same job
		return RemovalType.TRY;	
	}

	public boolean isLayoutEnforcing() {
		return true;
	}

	public void setLayoutEnforcing(boolean le) {
		throw new UnsupportedOperationException("Layout edges are always layout enforcing");
	}

	@Override
	public PlanarizationEdge[] split(Vertex toIntroduce) {
		PlanarizationEdge[] out = new PlanarizationEdge[2];
		out[0] = new ContainerLayoutEdge(getFrom(), toIntroduce, getDrawDirection(), straight, underlying, fromUnderlying, null);
		out[1] = new ContainerLayoutEdge(toIntroduce, getTo(),  getDrawDirection(), straight, underlying, null, toUnderlying);

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