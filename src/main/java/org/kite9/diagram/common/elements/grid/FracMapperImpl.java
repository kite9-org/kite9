package org.kite9.diagram.common.elements.grid;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractAnchoringVertex.Anchor;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.mapping.BaseGridCornerVertices;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.framework.logging.LogicException;

public class FracMapperImpl implements FracMapper {

	Map<DiagramElement, OPair<Map<BigFraction, Double>>> fracMaps = new HashMap<>();
	
	@Override
	public OPair<Map<BigFraction, Double>> getFracMapForGrid(DiagramElement c, RoutableHandler2D rh, BaseGridCornerVertices containerVertices, RoutingInfo ri) {
		OPair<Map<BigFraction, Double>> out = fracMaps.get(c);
		if (out != null) {
			return out;
		}
	
		Bounds xBounds = rh.getBoundsOf(ri, true);
		Bounds yBounds = rh.getBoundsOf(ri, false);
		
		Map<BigFraction, Bounds> left = new HashMap<>(), right  = new HashMap<>() , up  = new HashMap<>() , down  = new HashMap<>();
		
		// work out where this appears in relation to the neighbouring container's positions.
		Iterable<MultiCornerVertex> allVertices = containerVertices.getAllDescendentVertices();
		for (MultiCornerVertex cv : allVertices) {
			for (Anchor a : cv.getAnchors()) {
				RoutingInfo place = rh.getPlacedPosition(a.getDe());
				Bounds x = rh.getBoundsOf(place, true);
				if (a.getLr() == HPos.RIGHT) {
					expand(left, x, cv.getXOrdinal());
				} else if (a.getLr() == HPos.LEFT) {
					expand(right, x, cv.getXOrdinal());
				}
				
				Bounds y = rh.getBoundsOf(place, false);
				if (a.getUd() == VPos.DOWN) {
					expand(up, y, cv.getYOrdinal());
				} else if (a.getUd() == VPos.UP) {
					expand(down, y, cv.getYOrdinal());
				}
			}
		}
		
		Map<BigFraction, Double> xOut = createNullFracMap();
		for (BigFraction bf : left.keySet()) {
			if (!xOut.containsKey(bf)) {
				Bounds bleft = left.get(bf);
				Bounds bright = right.get(bf);
				
				if (bleft.getDistanceMax() > bright.getDistanceMin()) {
					throw new LogicException("Overlapping bounds in grid");
				}
				
				double midPoint = (bleft.getDistanceMax() + bright.getDistanceMin()) /2d;
				double frac = (midPoint-xBounds.getDistanceMin()) / (xBounds.getDistanceMax() - xBounds.getDistanceMin());
				xOut.put(bf, frac);
			}
		}
		
		Map<BigFraction, Double> yOut = createNullFracMap();

		for (BigFraction bf : up.keySet()) {
			if (!yOut.containsKey(bf)) {
				Bounds bup = up.get(bf);
				Bounds bdown = down.get(bf);
				
				if (bup.getDistanceMax() > bdown.getDistanceMin()) {
					throw new LogicException("Overlapping bounds in grid");
				}

				
				double midPoint = (bup.getDistanceMax() + bdown.getDistanceMin()) /2d;
				double frac = (midPoint - yBounds.getDistanceMin()) / (yBounds.getDistanceMax() - yBounds.getDistanceMin());
				yOut.put(bf, frac);
			}
		}
		
		out = new OPair<Map<BigFraction,Double>>(xOut, yOut);
		
		// add half for connecting vertices, in case needed
		if (!xOut.containsKey(BigFraction.ONE_HALF)) {
			xOut.put(BigFraction.ONE_HALF,.5d);
		}
		
		if (!yOut.containsKey(BigFraction.ONE_HALF)) {
			yOut.put(BigFraction.ONE_HALF, .5d);
		}
		
		fracMaps.put(c, out);
		return out;
	}

	static Map<BigFraction, Double> createNullFracMap() {
		Map<BigFraction, Double> xOut = new HashMap<>();
		xOut.put(BigFraction.ZERO, 0d);
		xOut.put(BigFraction.ONE, 1d);
		return xOut;
	}

	private void expand(Map<BigFraction, Bounds> boundsMap, Bounds newBounds, BigFraction ord) {
		Bounds oldBounds = boundsMap.get(ord);
		if (oldBounds == null) {
			boundsMap.put(ord, newBounds);
		} else {
			boundsMap.put(ord, oldBounds.expand(newBounds));
		}
	}
}
