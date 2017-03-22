package org.kite9.diagram.visualization.planarization.mgt;

import java.util.LinkedHashMap;
import java.util.Map;

import org.kite9.diagram.common.elements.AbstractAnchoringVertex.Anchor;
import org.kite9.diagram.common.elements.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.EdgeCrossingVertex;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;

/**
 * This edge is used for the surrounding of a diagram element.
 * 
 * Since all diagrams are containers of vertices, these edges will be used around the perimeter of the diagram.
 * 
 * The border edge keeps track of the elements it is bordering.  You can work out from their containment which side is which, I guess.
 * 
 * @author robmoffat
 *
 */
public class BorderEdge extends AbstractPlanarizationEdge {

	DiagramElement underlying;
	Map<DiagramElement, Direction> forElements;
	String label;
	
	public BorderEdge(Vertex from, Vertex to, String label, Direction d, boolean reversed, DiagramElement cide, Map<DiagramElement, Direction> forELements) {
		super(from, to, null, null, null, null, null);
		this.underlying = cide;
		this.label = label;
		this.drawDirection = d;
		this.reversed = reversed;
		this.forElements = forELements;
	}
	
	public BorderEdge(MultiCornerVertex from, MultiCornerVertex to, String label, Direction d) {
		this(from, to, label, d, false, getOuterContainer(from, to), new LinkedHashMap<>());
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
	
	/**
	 * For a given diagram element, shows what side of that element this edge is on.
	 */
	public Map<DiagramElement, Direction> getDiagramElements() {
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
			for (DiagramElement c : forElements.keySet()) {
				((EdgeCrossingVertex)toIntroduce).addUnderlying(c);
			}
		}
		
		return out;
	}

	@Override
	public int getLengthCost() {
		return 0;
	}
	
	private transient BorderTraversal bt = null;

	public BorderTraversal getBorderTraversal() {
		if (bt != null) {
			return bt;
		} else {
			bt = calculateTraversalRule();
			return bt;
		}
	}

	private BorderTraversal calculateTraversalRule() {
		if (underlying instanceof Container) {
			return ((Container)underlying).getTraversalRule(Direction.rotateAntiClockwise(getDrawDirection()));
		}
		
		return BorderTraversal.NONE;
	}

}