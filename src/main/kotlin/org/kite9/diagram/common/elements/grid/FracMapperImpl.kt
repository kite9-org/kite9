package org.kite9.diagram.common.elements.grid

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.elements.mapping.BaseGridCornerVertices
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.fraction.BigFraction
import org.kite9.diagram.common.fraction.BigFraction.Companion.ONE
import org.kite9.diagram.common.fraction.BigFraction.Companion.ONE_HALF
import org.kite9.diagram.common.fraction.BigFraction.Companion.ZERO
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.HPos
import org.kite9.diagram.model.position.VPos
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

class FracMapperImpl : FracMapper {

    var fracMaps: MutableMap<DiagramElement, OPair<Map<BigFraction, Double>>> = HashMap()

    override fun getFracMapForGrid(
        c: DiagramElement,
        rh: RoutableHandler2D,
        containerVertices: BaseGridCornerVertices,
        ri: RoutingInfo
    ): OPair<Map<BigFraction, Double>> {
        var out = fracMaps[c]
        if (out != null) {
            return out
        }
        val xBounds = rh.getBoundsOf(ri, true)
        val yBounds = rh.getBoundsOf(ri, false)
        val left: MutableMap<BigFraction, Bounds?> = HashMap()
        val right: MutableMap<BigFraction, Bounds?> = HashMap()
        val up: MutableMap<BigFraction, Bounds?> = HashMap()
        val down: MutableMap<BigFraction, Bounds?> = HashMap()

        // work out where this appears in relation to the neighbouring container's positions.
        val allVertices: Iterable<MultiCornerVertex> = containerVertices.getAllDescendentVertices()

//		((List<MultiCornerVertex>)allVertices).stream()
//			.flatMap(e -> e.getAnchors().stream())
//			.map(a -> a.getDe())
//			.distinct()
//			.forEach(e -> System.out.println(e.toString()+" "+rh.getPlacedPosition(e)));
        for (cv in allVertices) {
            for (a in cv.getAnchors()) {
                val place = rh.getPlacedPosition(a.de)
                val x = rh.getBoundsOf(place, true)
                if (a.lr == HPos.RIGHT) {
                    expand(left, x, cv.xOrdinal)
                } else if (a.lr == HPos.LEFT) {
                    expand(right, x, cv.xOrdinal)
                }
                val y = rh.getBoundsOf(place, false)
                if (a.ud == VPos.DOWN) {
                    expand(up, y, cv.yOrdinal)
                } else if (a.ud == VPos.UP) {
                    expand(down, y, cv.yOrdinal)
                }
            }
        }
        val xOut = createNullFracMap()
        for (bf in left.keys) {
            if (!xOut.containsKey(bf)) {
                val bleft = left[bf]
                val bright = right[bf]
                if (bleft!!.distanceMax > bright!!.distanceMin) {
                    throw LogicException("Overlapping bounds in grid: " + bleft.distanceMax + " / " + bright.distanceMin)
                }
                val midPoint = (bleft.distanceMax + bright.distanceMin) / 2.0
                val frac = (midPoint - xBounds.distanceMin) / (xBounds.distanceMax - xBounds.distanceMin)
                xOut[bf] = frac
            }
        }
        val yOut = createNullFracMap()
        for (bf in up.keys) {
            if (!yOut.containsKey(bf)) {
                val bup = up[bf]
                val bdown = down[bf]
                if (bup!!.distanceMax > bdown!!.distanceMin) {
                    throw LogicException("Overlapping bounds in grid: " + bup.distanceMax + " / " + bdown.distanceMin)
                }
                val midPoint = (bup.distanceMax + bdown.distanceMin) / 2.0
                val frac = (midPoint - yBounds.distanceMin) / (yBounds.distanceMax - yBounds.distanceMin)
                yOut[bf] = frac
            }
        }
        out = OPair(xOut, yOut)

        // add half for connecting vertices, in case needed
        if (!xOut.containsKey(ONE_HALF)) {
            xOut[ONE_HALF] = .5
        }
        if (!yOut.containsKey(ONE_HALF)) {
            yOut[ONE_HALF] = .5
        }
        fracMaps[c] = out
        return out
    }

    private fun expand(boundsMap: MutableMap<BigFraction, Bounds?>, newBounds: Bounds, ord: BigFraction) {
        val oldBounds = boundsMap[ord]
        if (oldBounds == null) {
            boundsMap[ord] = newBounds
        } else {
            boundsMap[ord] = oldBounds.expand(newBounds)
        }
    }

    companion object {
        fun createNullFracMap(): MutableMap<BigFraction, Double> {
            val xOut: MutableMap<BigFraction, Double> = HashMap()
            xOut[ZERO] = 0.0
            xOut[ONE] = 1.0
            return xOut
        }
    }
}