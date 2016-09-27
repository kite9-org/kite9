package org.kite9.diagram.visualization.planarization.mapping;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.objects.OPair;

/**
 * Adds vertices to the parent container vertices, and operates in a narrower range.
 * 
 * @author robmoffat
 *
 */
public class SubwindowContainerVertices extends AbstractContainerVertices {
	
	public SubwindowContainerVertices(Container c, OPair<BigFraction> x, OPair<BigFraction> y, ContainerVertices parentCV) {
		super(c, getXSpan(x, parentCV), getYSpan(y, parentCV), ((AbstractContainerVertices)parentCV).elements);
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
	
}
