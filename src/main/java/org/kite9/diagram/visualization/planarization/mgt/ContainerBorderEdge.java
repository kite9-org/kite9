package org.kite9.diagram.visualization.planarization.mgt;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.elements.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;

/**
 * This edge is used for the surrounding of a container.
 * 
 * Since all diagrams are containers
 * of vertices, these edges will be used around the perimeter of the diagram.
 * 
 * A new constraint on ContainerBorder edge is that "from" must be before "to" in the clockwise ordering of the edges 
 * on the face, when the edge is created.  That way, we always know whether a face is inside or outside a container.
 * 
 * @author robmoffat
 *
 */
public class ContainerBorderEdge extends AbstractPlanarizationEdge {

	List<Container> containers = new ArrayList<>(2);
	String label;
	
	public ContainerBorderEdge(Vertex from, Vertex to, String label, Container c, Direction d) {
		super(from, to, null, null, null, null, null);
		this.containers.add(c);
		this.label = label;
		this.drawDirection = d;
	}
	
	public Container getOriginalUnderlying() {
		return null; // container;
	}
	
	public void addContainer(Container c) {
		this.containers.add(c);
	}

	public List<Container> getContainers() {
		return containers;
	}
	
	@Override
	public String toString() {
		return label;
	}

	@Override
	public int getCrossCost() {
		return 0;	
	}

	@Override
	public RemovalType removeBeforeOrthogonalization() {
		return RemovalType.NO;
	}

	public boolean isLayoutEnforcing() {
		return false;
	}

	public void setLayoutEnforcing(boolean le) {
		throw new UnsupportedOperationException("Container edges are never layout enforcing");
	}

	@Override
	public PlanarizationEdge[] split(Vertex toIntroduce) {
		PlanarizationEdge[] out = new PlanarizationEdge[2];
		out[0] = new ContainerBorderEdge(getFrom(), toIntroduce, label+"_1", this.container, drawDirection, isReversed());
		out[1] = new ContainerBorderEdge(toIntroduce, getTo(), label+"_2", this.container, drawDirection, isReversed());
		return out;
	}

	@Override
	public int getLengthCost() {
		return 0;
	}
	
}