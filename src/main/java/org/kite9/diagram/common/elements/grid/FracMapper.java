package org.kite9.diagram.common.elements.grid;

import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.mapping.BaseGridCornerVertices;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public interface FracMapper {

	
	public static final Map<BigFraction, Double> NULL_FRAC_MAP = FracMapperImpl.createNullFracMap();

	/**
	 * Given a laid-out container (i.e. post phase 2 of RHD) this works out, for each fraction used on Grid X/Y positions, 
	 * where within the bounds of the container's PositionInfo the fractions should be placed.
	 * @param containerVertices 
	 * @param bounds 
	 */
	public OPair<Map<BigFraction, Double>> getFracMapForGrid(DiagramElement c, RoutableHandler2D rh, BaseGridCornerVertices containerVertices, RoutingInfo bounds);

}
