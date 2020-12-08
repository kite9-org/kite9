package org.kite9.diagram.common.elements.mapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.fraction.BigFraction;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public abstract class AbstractBaseCornerVertices extends AbstractCornerVertices {

	public static final OPair<BigFraction> FULL_RANGE = new OPair<>(BigFraction.Companion.getZERO(), BigFraction.Companion.getONE());
	
	private final Map<OPair<BigFraction>, MultiCornerVertex> elements = new HashMap<>();
	
	public AbstractBaseCornerVertices(DiagramElement rootContainer, OPair<BigFraction> cx, OPair<BigFraction> cy, int depth) {
		super(rootContainer, cx, cy, depth);
	}


	protected MultiCornerVertex getExistingVertex(OPair<BigFraction> d) {
		return elements.get(d);
	}
	
	/**
	 * There are no duplicates for independent container vertices.
	 */
	@Override
	public MultiCornerVertex mergeDuplicates(MultiCornerVertex cv, RoutableHandler2D rh) {
		return cv;
	}

	@Override
	public MultiCornerVertex createVertex(BigFraction x, BigFraction y) {
		return createVertexHere(x, y, elements);
	}

	@Override
	protected AbstractCornerVertices getTopContainerVertices() {
		return this;
	}

	@Override
	public Collection<MultiCornerVertex> getAllAscendentVertices() {
		return elements.values();
	}

	public Collection<MultiCornerVertex> getVerticesAtThisLevel() {
		return elements.values();
	}
	
	
	@Override
	public Collection<MultiCornerVertex> getAllDescendentVertices() {
		Collection<MultiCornerVertex> out = super.getAllDescendentVertices();
		out.addAll(elements.values());
		return out;
	}
}