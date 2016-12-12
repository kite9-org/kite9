package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.AbstractAnchoringVertex.Anchor;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

/**
 * Adds vertices to the parent container vertices, and operates in a narrower range.
 * 
 * This is used for containers embedded in a grid layout, which add their container vertices to the parent container.
 * 
 * @author robmoffat
 *
 */
public class SubwindowCornerVertices extends AbstractCornerVertices {
	
	AbstractCornerVertices parent;
	
	private final Map<OPair<BigFraction>, MultiCornerVertex> elements;
	
	public SubwindowCornerVertices(DiagramElement c, OPair<BigFraction> x, OPair<BigFraction> y, CornerVertices parentCV) {
		super(c, getXSpan(x, parentCV), getYSpan(y, parentCV));
		((AbstractCornerVertices) parentCV).children.add(this);
		this.parent = (AbstractCornerVertices) parentCV;
		this.elements = new HashMap<>();
		createInitialVertices(c);
	}

	private static OPair<BigFraction> getYSpan(OPair<BigFraction> y, CornerVertices parentCV) {
		return new OPair<BigFraction>(
				scale(y.getA(), ((AbstractCornerVertices)parentCV).getYRange()),
				scale(y.getB(), ((AbstractCornerVertices)parentCV).getYRange())
				);
	}

	private static OPair<BigFraction> getXSpan(OPair<BigFraction> x, CornerVertices parentCV) {
		return new OPair<BigFraction>(
				scale(x.getA(), ((AbstractCornerVertices)parentCV).getXRange()),
				scale(x.getB(), ((AbstractCornerVertices)parentCV).getXRange())
				);
	}

	@Override
	public MultiCornerVertex mergeDuplicates(MultiCornerVertex cv, RoutableHandler2D rh) {
		if (elements.values().contains(cv)) {
			MultiCornerVertex out = getTopContainerVertices().findOverlappingVertex(cv, rh);
			
			if (out != null) {
				// merge the anchors
				for (Anchor a : cv.getAnchors()) {
					out.addAnchor(a.getLr(), a.getUd(), a.getDe());
				}
				
				// replace the element in the map
				elements.put(new OPair<BigFraction>(cv.getXOrdinal(), cv.getYOrdinal()), out);
				
				return null;
			}
			
			return cv;
		} else {
			return parent.mergeDuplicates(cv, rh);
		}
	}
	
	protected AbstractCornerVertices getTopContainerVertices() {
		return parent.getTopContainerVertices();
	}

	@Override
	public MultiCornerVertex createVertex(BigFraction x, BigFraction y) {
		x = scale(x, getXRange());
		y = scale(y, getYRange());
		
		return parent.createVertexHere(x, y);
	}

	@Override
	public MultiCornerVertex createVertexHere(BigFraction x, BigFraction y) {
		return createVertexHere(x, y, elements);
	}

	@Override
	public Collection<MultiCornerVertex> getAllAscendentVertices() {
		Collection<MultiCornerVertex> out = new ArrayList<>();
		out.addAll(parent.getAllAscendentVertices());
		out.addAll(elements.values());
		return out;
	}
	
	@Override
	public Collection<MultiCornerVertex> getAllDescendentVertices() {
		Collection<MultiCornerVertex> out = super.getAllDescendentVertices();
		out.addAll(elements.values());
		return out;
	}

	@Override
	protected MultiCornerVertex getExistingVertex(OPair<BigFraction> d) {
		MultiCornerVertex out = parent.getExistingVertex(d);
		if (out == null) {
			return elements.get(d);
		} else {
			return out;
		}
	}
	
	public Collection<MultiCornerVertex> getVerticesAtThisLevel() {
		return elements.values();
	}
	
}
