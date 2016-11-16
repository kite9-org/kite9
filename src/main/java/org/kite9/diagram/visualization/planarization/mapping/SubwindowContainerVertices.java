package org.kite9.diagram.visualization.planarization.mapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
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
public class SubwindowContainerVertices extends AbstractContainerVertices {
	
	AbstractContainerVertices parent;
	
	private final Map<OPair<BigFraction>, ContainerVertex> elements;
	
	public SubwindowContainerVertices(Container c, OPair<BigFraction> x, OPair<BigFraction> y, ContainerVertices parentCV) {
		super(c, getXSpan(x, parentCV), getYSpan(y, parentCV));
		((AbstractContainerVertices) parentCV).children.add(this);
		this.parent = (AbstractContainerVertices) parentCV;
		this.elements = new HashMap<>();
		createInitialVertices(c);
	}

	private static OPair<BigFraction> getYSpan(OPair<BigFraction> y, ContainerVertices parentCV) {
		return new OPair<BigFraction>(
				scale(y.getA(), ((AbstractContainerVertices)parentCV).getYRange()),
				scale(y.getB(), ((AbstractContainerVertices)parentCV).getYRange())
				);
	}

	private static OPair<BigFraction> getXSpan(OPair<BigFraction> x, ContainerVertices parentCV) {
		return new OPair<BigFraction>(
				scale(x.getA(), ((AbstractContainerVertices)parentCV).getXRange()),
				scale(x.getB(), ((AbstractContainerVertices)parentCV).getXRange())
				);
	}

	@Override
	public ContainerVertex mergeDuplicates(ContainerVertex cv, RoutableHandler2D rh) {
		return cv;
	}

	@Override
	public ContainerVertex createVertex(BigFraction x, BigFraction y) {
		x = scale(x, getXRange());
		y = scale(y, getYRange());
		
		return parent.createVertexHere(x, y);
	}

	@Override
	public ContainerVertex createVertexHere(BigFraction x, BigFraction y) {
		return createVertexHere(x, y, elements);
	}

	private Collection<ContainerVertex> allVertices = new LinkedHashSet<>();
	private transient int parentSize = 0, elementsSize = 0;
	
	@Override
	public Collection<ContainerVertex> getAllVertices() {
		Collection<ContainerVertex> parentAllVertices = parent.getAllVertices();
		if ((parentSize != parentAllVertices.size()) || (elementsSize != elements.size())) {
			allVertices.addAll(parentAllVertices);
			allVertices.addAll(elements.values());
			this.parentSize = parentAllVertices.size();
			this.elementsSize = elements.size();
		}
		return allVertices;
	}

	@Override
	protected ContainerVertex getExistingVertex(OPair<BigFraction> d) {
		ContainerVertex out = parent.getExistingVertex(d);
		if (out == null) {
			return elements.get(d);
		} else {
			return out;
		}
	}
	
	
	
}
