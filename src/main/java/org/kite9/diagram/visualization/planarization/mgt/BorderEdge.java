package org.kite9.diagram.visualization.planarization.mgt;

import java.util.Iterator;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.edge.TwoElementPlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.EdgeCrossingVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.framework.common.Kite9ProcessingException;

/**
 * This edge is used for the surrounding of a diagram element.
 * 
 * Since all diagrams are containers of vertices, these edges will be used around the perimeter of the diagram.
 * 
 * The border edge keeps track of the rectangular border.  You can work out from their containment which side is which, I guess.
 * 
 * @author robmoffat
 *
 */
public class BorderEdge extends AbstractPlanarizationEdge implements TwoElementPlanarizationEdge {

	private final Map<DiagramElement, Direction> forElements;
	String label;
	
	public BorderEdge(Vertex from, Vertex to, String label, Direction d, Map<DiagramElement, Direction> forElements) {
		super(from, to, d);
		this.label = label;
		this.forElements = forElements;
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
		out[0] = new BorderEdge(getFrom(), toIntroduce, label+"_1", drawDirection, forElements);
		out[1] = new BorderEdge(toIntroduce, getTo(), label+"_2", drawDirection, forElements);
		
		if (toIntroduce instanceof EdgeCrossingVertex) {
			// track the containers that we are involved in
			for (DiagramElement c : forElements.keySet()) {
				((EdgeCrossingVertex)toIntroduce).addUnderlying(c);
			}
		}
		
		return out;
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
		
		if ((forElements.size() != 2) && (forElements.size() != 1)){
			throw new Kite9ProcessingException("An BorderEdge must be for 1 or 2 diagram elements");
		}

		Iterator<DiagramElement> els = forElements.keySet().iterator();	
		sidea = els.next();
		sideb = els.hasNext() ? els.next() : sidea.getParent();
		
		if (from == sidea) {
			return sideb;
		} else if (from == sideb) {
			return sidea;
		} else { 
			throw new Kite9ProcessingException(from+" is not mapped to a side");
		}
	}

	@Override
	public DiagramElement getElementForSide(Direction d) {
		for (DiagramElement de : forElements.keySet()) {
			if (forElements.get(de) == d) {
				return de;
			}
		}
		
		throw new Kite9ProcessingException("No diagram element in direction: "+d+" for "+this);
	}
	
	
}