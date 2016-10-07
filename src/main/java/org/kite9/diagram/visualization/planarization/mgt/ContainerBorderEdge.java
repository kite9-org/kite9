package org.kite9.diagram.visualization.planarization.mgt;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.grid.GridInterfaceDiagramElement;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.framework.common.Kite9ProcessingException;

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

	DiagramElement cide;
	String label;
	
	public ContainerBorderEdge(Vertex from, Vertex to, String label, Direction d, boolean reversed, DiagramElement cide) {
		super(from, to, null, null, null, null, null);
		this.cide = cide;
		this.label = label;
		this.drawDirection = d;
		this.reversed = reversed;
	}
	
	public ContainerBorderEdge(ContainerVertex from, ContainerVertex to, String label, Direction d) {
		this(from, to, label, d, false, createContainerInterfaceElement(from, to));
		this.label = label;
		this.drawDirection = d;
	}
	
	private static DiagramElement createContainerInterfaceElement(ContainerVertex from, ContainerVertex to) {
		Set<DiagramElement> both = from.getAllAnchoredContainers();
		both.retainAll(to.getAllAnchoredContainers());
		
		if (both.size() == 1) {
			return both.iterator().next();
		} else if (both.size() == 2) {
			Iterator<DiagramElement> it = both.iterator();
			return new GridInterfaceDiagramElement((Container) it.next(), (Container) it.next());
		} else {
			throw new Kite9ProcessingException("Found border of too many/few containers: "+both);
		}
	}

	public DiagramElement getOriginalUnderlying() {
		return cide;
	}
	
	@Deprecated
	public Collection<Container> getContainers() {
		if (cide instanceof Container) {
			return Collections.singleton((Container) cide);
		} else {
			return ((GridInterfaceDiagramElement)cide).getContainers();
		}
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
		out[0] = new ContainerBorderEdge(getFrom(), toIntroduce, label+"_1", drawDirection, isReversed(), cide);
		out[1] = new ContainerBorderEdge(toIntroduce, getTo(), label+"_2", drawDirection, isReversed(), cide);
		return out;
	}

	@Override
	public int getLengthCost() {
		return 0;
	}
	
}