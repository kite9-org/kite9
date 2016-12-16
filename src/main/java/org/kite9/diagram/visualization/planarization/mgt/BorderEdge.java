package org.kite9.diagram.visualization.planarization.mgt;

import java.util.Collection;
import java.util.HashSet;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.EdgeCrossingVertex;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.elements.AbstractAnchoringVertex.Anchor;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;

/**
 * This edge is used for the surrounding of a diagram element.
 * 
 * Since all diagrams are containers
 * of vertices, these edges will be used around the perimeter of the diagram.
 * 
 * A new constraint on Border edge is that "from" must be before "to" in the clockwise ordering of the edges 
 * on the face, when the edge is created.  That way, we always know whether a face is inside or outside a container.
 * 
 * @author robmoffat
 *
 */
public class BorderEdge extends AbstractPlanarizationEdge {

	DiagramElement underlying;
	Collection<DiagramElement> forElements;
	String label;
	
	public BorderEdge(Vertex from, Vertex to, String label, Direction d, boolean reversed, DiagramElement cide, Collection<DiagramElement> forELements) {
		super(from, to, null, null, null, null, null);
		this.underlying = cide;
		this.label = label;
		this.drawDirection = d;
		this.reversed = reversed;
		this.forElements = forELements;
	}
	
	public BorderEdge(MultiCornerVertex from, MultiCornerVertex to, String label, Direction d) {
		this(from, to, label, d, false, getOuterContainer(from, to), new HashSet<>());
	}
	
	private static Container getOuterContainer(MultiCornerVertex from, MultiCornerVertex to) {
		int depth = Integer.MAX_VALUE;
		DiagramElement out = null;
		for (Anchor f : from.getAnchors()) {
			int currentDepth = getDepth(f.getDe());
			if (currentDepth < depth ) {
				out = f.getDe();
				depth = currentDepth;
			}
		}
		
		for (Anchor f : to.getAnchors()) {
			int currentDepth = getDepth(f.getDe());
			if (currentDepth < depth ) {
				out = f.getDe();
				depth = currentDepth;
			}
		}
		
		DiagramElement parent = out.getParent();
		while ((parent != null) && (((Container)parent).getLayout() == Layout.GRID)) {
			out = parent;
			parent = out.getParent();
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

	public DiagramElement getOriginalUnderlying() {
		return underlying;
	}
	
	public Collection<DiagramElement> getDiagramElements() {
		return forElements;
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
		out[0] = new BorderEdge(getFrom(), toIntroduce, label+"_1", drawDirection, isReversed(), underlying, forElements);
		out[1] = new BorderEdge(toIntroduce, getTo(), label+"_2", drawDirection, isReversed(), underlying, forElements);
		
		if (toIntroduce instanceof EdgeCrossingVertex) {
			// track the containers that we are involved in
			for (DiagramElement c : forElements) {
				((EdgeCrossingVertex)toIntroduce).addUnderlying(c);
			}
		}
		
		return out;
	}

	@Override
	public int getLengthCost() {
		return 0;
	}
	
}