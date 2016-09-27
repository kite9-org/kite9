package org.kite9.diagram.visualization.planarization.mapping;

import java.util.HashMap;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.objects.OPair;

public class IndependentContainerVertices extends AbstractContainerVertices {
	
	private static final OPair<BigFraction> FULL_RANGE = new OPair<>(BigFraction.ZERO, BigFraction.ONE);
	
	public IndependentContainerVertices(Container c) {
		super(c, FULL_RANGE, FULL_RANGE, new HashMap<>());
	}

}