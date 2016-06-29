package org.kite9.diagram.visualization.planarization.mapping;

import org.kite9.diagram.common.elements.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.DiagramElement;

/**
 * This edge is used to enforce a particular layout within a container.
 * 
 * It is used between the sibling contents of the container.
 * 
 * @author robmoffat
 *
 */
public class ContainerLayoutEdge extends AbstractPlanarizationEdge {
	
	GeneratedLayoutElement underlying;
	
	public ContainerLayoutEdge(Vertex from, Vertex to, Direction d, Connected fromElement, Connected toElement) {
		super(from, to, null, null, null, null, null);
		this.drawDirection = d;
		this.underlying =  new GeneratedLayoutElement((Connected) from.getOriginalUnderlying(), (Connected) to.getOriginalUnderlying(), d);
	}
	
	private ContainerLayoutEdge(Vertex from, Vertex toIntroduce, Direction drawDirection, boolean reversed, GeneratedLayoutElement underlying) {
		super(from, toIntroduce, null, null, null, null, null);
		this.reversed =true;
		this.underlying = underlying;
		this.drawDirection = drawDirection;
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
		out[0] = new ContainerLayoutEdge(getFrom(), toIntroduce, getDrawDirection(), isReversed(), underlying);
		out[1] = new ContainerLayoutEdge(toIntroduce, getTo(),  getDrawDirection(), isReversed(), underlying);

		return out;
	}

	@Override
	public boolean isReversed() {
		return false;
	}

	@Override
	public int getLengthCost() {
		return 0;
	}
}