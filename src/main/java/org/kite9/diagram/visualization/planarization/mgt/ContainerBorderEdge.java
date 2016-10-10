package org.kite9.diagram.visualization.planarization.mgt;

import java.util.Collection;
import java.util.Set;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;

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

	Container cide;
	Collection<DiagramElement> containers;
	String label;
	
	public ContainerBorderEdge(Vertex from, Vertex to, String label, Direction d, boolean reversed, Container cide, Collection<DiagramElement> containers) {
		super(from, to, null, null, null, null, null);
		this.cide = cide;
		this.label = label;
		this.drawDirection = d;
		this.reversed = reversed;
		this.containers = containers;
	}
	
	public ContainerBorderEdge(ContainerVertex from, ContainerVertex to, String label, Direction d) {
		this(from, to, label, d, false, getOuterContainer(from, to), createContainerCollection((ContainerVertex) from, (ContainerVertex) to));
	}
	
	private static Container getOuterContainer(ContainerVertex from, ContainerVertex to) {
		int depth = Integer.MAX_VALUE;
		DiagramElement out = null;
		for (DiagramElement f : from.getAllAnchoredContainers()) {
			int currentDepth = getDepth(f);
			if (currentDepth < depth ) {
				out = f;
				depth = currentDepth;
			}
		}
		
		for (DiagramElement f : to.getAllAnchoredContainers()) {
			int currentDepth = getDepth(f);
			if (currentDepth < depth ) {
				out = f;
				depth = currentDepth;
			}
		}
		
		return (Container) out;
	}

	private static int getDepth(DiagramElement f) {
		DiagramElement parent = f.getParent();
		if (parent==null) {
			return 0;
		} else {
			return 1 + getDepth(parent);
		}
	}

	private static Collection<DiagramElement> createContainerCollection(ContainerVertex from, ContainerVertex to) {
		Set<DiagramElement> both = from.getAllAnchoredContainers();
		both.retainAll(to.getAllAnchoredContainers());
		return both;
	}

	public DiagramElement getOriginalUnderlying() {
		return cide;
	}
	
	public Collection<DiagramElement> getContainers() {
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
		out[0] = new ContainerBorderEdge(getFrom(), toIntroduce, label+"_1", drawDirection, isReversed(), cide, containers);
		out[1] = new ContainerBorderEdge(toIntroduce, getTo(), label+"_2", drawDirection, isReversed(), cide, containers);
		return out;
	}

	@Override
	public int getLengthCost() {
		return 0;
	}
	
}