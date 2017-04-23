package org.kite9.diagram.visualization.planarization.mgt;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.edge.TwoElementPlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.AbstractAnchoringVertex.Anchor;
import org.kite9.diagram.common.elements.vertex.EdgeCrossingVertex;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.framework.common.Kite9ProcessingException;

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
public class BorderEdge extends AbstractPlanarizationEdge implements TwoElementPlanarizationEdge {

	Map<DiagramElement, Direction> forElements;
	String label;
	
	public BorderEdge(Vertex from, Vertex to, String label, Direction d, boolean reversed, Map<DiagramElement, Direction> forELements) {
		super(from, to, null, null, null, null, null);
		this.label = label;
		this.drawDirection = d;
		this.reversed = reversed;
		this.forElements = forELements;
	}
	
	public BorderEdge(MultiCornerVertex from, MultiCornerVertex to, String label, Direction d) {
		this(from, to, label, d, false,  new LinkedHashMap<>());
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
		out[0] = new BorderEdge(getFrom(), toIntroduce, label+"_1", drawDirection, isReversed(), forElements);
		out[1] = new BorderEdge(toIntroduce, getTo(), label+"_2", drawDirection, isReversed(), forElements);
		
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
		BorderTraversal out = null;
		
		for (Map.Entry<DiagramElement, Direction> e : forElements.entrySet()) {
			DiagramElement underlying =e.getKey();
			if (underlying instanceof Container) {
				BorderTraversal bt = ((Container)underlying).getTraversalRule(Direction.rotateAntiClockwise(getDrawDirection()));
				out = BorderTraversal.reduce(out, bt);
			}
		}
		
		return out;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return forElements.containsKey(de);
	}

	@Override
	public DiagramElement getOtherSide(DiagramElement from) {
		DiagramElement sidea = null, sideb = null;
		
		if (forElements.size() > 2) {
			throw new Kite9ProcessingException("An edge can only have 2 sides");
		}

		Iterator<DiagramElement> els = forElements.keySet().iterator();	
		if (els.hasNext()) {
			sidea = els.next();
		} 
		if (els.hasNext()) {
			sideb = els.next();
		}

		
		if (from == sidea) {
			return sideb;
		} else if (from == sideb) {
			return sidea;
		} else if ((sidea != null) && (sidea.getParent() == from)) {
			return sidea;
		} else { 
			throw new Kite9ProcessingException(from+" is not mapped to a side");
		}
	}

	@Override
	public DiagramElement getOriginalUnderlying() {
		throw new Kite9ProcessingException("No single underlying for BorderEdge");
	}

}