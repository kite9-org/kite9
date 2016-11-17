package org.kite9.diagram.visualization.planarization.mapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public class IndependentContainerVertices extends AbstractContainerVertices {
	
	private static final OPair<BigFraction> FULL_RANGE = new OPair<>(BigFraction.ZERO, BigFraction.ONE);
	
	private final Map<OPair<BigFraction>, ContainerVertex> elements = new HashMap<>();

	public IndependentContainerVertices(Container c) {
		super(c, FULL_RANGE, FULL_RANGE);
		createInitialVertices(c);
	}

	protected ContainerVertex getExistingVertex(OPair<BigFraction> d) {
		return elements.get(d);
	}
	
	/**
	 * There are no duplicates for independent container vertices.
	 */
	@Override
	public ContainerVertex mergeDuplicates(ContainerVertex cv, RoutableHandler2D rh) {
		return cv;
	}

	@Override
	public ContainerVertex createVertex(BigFraction x, BigFraction y) {
		return createVertexHere(x, y);
	}

	@Override
	public ContainerVertex createVertexHere(BigFraction x, BigFraction y) {
		return createVertexHere(x, y, elements);
	}

	@Override
	public Collection<ContainerVertex> getAllVertices() {
		return elements.values();
	}

	@Override
	protected AbstractContainerVertices getTopContainerVertices() {
		return this;
	}

}