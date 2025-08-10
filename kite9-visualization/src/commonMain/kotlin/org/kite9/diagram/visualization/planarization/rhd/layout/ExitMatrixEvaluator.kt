package org.kite9.diagram.visualization.planarization.rhd.layout

import kotlin.math.max
import kotlin.math.min
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.Layout.Companion.reverse
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.layout.ExitMatrix.RelativeSide
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos

/**
 * Tries to work out the likelihood of an overlap.
 *
 * @author robmoffat
 */
class ExitMatrixEvaluator {

    /** A heuristic calculation for the likely number of crossings due to this arragement */
    fun countOverlaps(
            aMatrix: ExitMatrix,
            bMatrix: ExitMatrix,
            aDirection: Layout,
            rh: RoutableHandler2D
    ): Double {
        val sideA = sumOneSide(aMatrix, bMatrix, aDirection, -1)
        val sideB = sumOneSide(aMatrix, bMatrix, aDirection, +1)
        val occlusionA = addOneOccludedSide(aMatrix, bMatrix, aDirection, rh)
        val occlusionB = addOneOccludedSide(bMatrix, aMatrix, reverse(aDirection), rh)
        return (sideA + sideB + occlusionA + occlusionB).toDouble()
    }

    /** Works out the extra distance of external links due to this arrangement */
    fun calculateExtraExternalLinkDistance(
            aMatrix: ExitMatrix,
            bMatrix: ExitMatrix,
            aDirection: Layout?,
            rh: RoutableHandler2D?
    ): Double {
        return calcEEDistanceOneSide(
                aMatrix,
                bMatrix.getSizeInDirectionOfLayout(aDirection),
                aDirection
        ) +
                calcEEDistanceOneSide(
                        bMatrix,
                        aMatrix.getSizeInDirectionOfLayout(aDirection),
                        reverse(aDirection)
                )
    }

    private fun calcEEDistanceOneSide(aMatrix: ExitMatrix, distance: Bounds, d: Layout?): Double {
        val count =
                aMatrix.getLinkCount(d, RelativeSide.OPPOSITE, -1) +
                        aMatrix.getLinkCount(d, RelativeSide.OPPOSITE, 0) +
                        aMatrix.getLinkCount(d, RelativeSide.OPPOSITE, 1)
        val incrementalDistance = distance.distanceMax - distance.distanceMin
        return count * incrementalDistance
    }

    /** Works out the cost score for the distance travelled by the internal links */
    fun calculateInternalDistance(internal: LinkDetail?, rh: RoutableHandler2D): Double {
        if (internal == null) return 0.0
        val out = doubleArrayOf(0.0)
        internal.processLowestLevel(
                object : LinkProcessor {
                    override fun process(
                            originatingGroup: Group,
                            destinationGroup: Group,
                            ld: LinkDetail
                    ) {
                        val aRI = originatingGroup.axis.getPosition(rh, true)
                        val bRI = destinationGroup.axis.getPosition(rh, true)
                        val cost = rh.cost(aRI, bRI) * ld!!.numberOfLinks
                        out[0] += cost
                        //				log.send("Evaluating: "+cost+
                        //						"\n\tfrom "+((LeafGroup)originatingGroup).getContained()+" at "+aRI+
                        //						"\n\tto "+((LeafGroup)destinationGroup).getContained()+" at "+bRI);
                    }
                }
        )
        return out[0]
    }

    /**
     * Figures out a cost associated with one side of the matrix based on the likelihood of leaving
     * edges crossing one another.
     */
    private fun sumOneSide(aMatrix: ExitMatrix, bMatrix: ExitMatrix, ad: Layout, rank: Int): Float {
        val bFacing = bMatrix.getLinkCount(ad, RelativeSide.FACING, rank)
        val aMid = aMatrix.getLinkCount(ad, RelativeSide.MIDDLE, rank)
        val bMid = bMatrix.getLinkCount(ad, RelativeSide.MIDDLE, rank)
        val aOpp = aMatrix.getLinkCount(ad, RelativeSide.OPPOSITE, rank)
        val cornerCross = bFacing * aOpp
        val midCrossB = aOpp * bMid * STRAIGHT_VS_CORNER_COST
        val midCrossA = bFacing * aMid * STRAIGHT_VS_CORNER_COST
        return cornerCross + midCrossB + midCrossA
    }

    /**
     * Work out how many edges you will have to cross to get from the occluded square to where you
     * want to go.
     *
     * When a matrix with a small span is occluded by one with a large span, we need to add
     * crossings in order to "break in" to the space of the larger one.
     */
    private fun addOneOccludedSide(
            aMatrix: ExitMatrix,
            bMatrix: ExitMatrix,
            ad: Layout?,
            rh: RoutableHandler2D
    ): Float {
        val aOccluded = aMatrix.getLinkCount(ad, RelativeSide.OPPOSITE, 0)
        if (aOccluded == 0f) {
            return 0F
        }
        val facingOccluding = bMatrix.getLinkCount(ad, RelativeSide.FACING, 0) / 2
        var lowerOccluding = getLinkCountOnSide(bMatrix, ad, -1) + facingOccluding
        var higherOccluding = getLinkCountOnSide(bMatrix, ad, 1) + facingOccluding
        lowerOccluding *= aOccluded
        higherOccluding *= aOccluded

        // large span check
        val aSpan = aMatrix.getSpanInDirectionOfLayout(reverse(ad)!!)
        val bSpan = bMatrix.getSpanInDirectionOfLayout(reverse(ad)!!)
        if (rh.compareBounds(aSpan!!, bSpan!!) === DPos.OVERLAP) {
            val bOccluding = bMatrix.getLinkCount(ad, RelativeSide.OPPOSITE, 0)

            // works out the occluded span
            val occludedMin = max(aSpan!!.distanceMin, bSpan!!.distanceMin)
            val occludedMax = min(aSpan.distanceMax, bSpan.distanceMax)
            val topOccludedFrac =
                    (occludedMax - occludedMin) / (aSpan.distanceMax - aSpan.distanceMin)

            // work out how much of b you would have to cross to get back to where you started from
            val lowerTravel = occludedMax - bSpan.distanceMin
            val higherTravel = bSpan.distanceMax - occludedMin

            // work out travel as a fraction of b
            val lowerFrac = lowerTravel / (bSpan.distanceMax - bSpan.distanceMin)
            val higherFrac = higherTravel / (bSpan.distanceMax - bSpan.distanceMin)

            // work out how many edge crossings that's likely to be
            val lowerAmt = topOccludedFrac * lowerFrac * bOccluding * aOccluded
            val higherAmt = topOccludedFrac * higherFrac * bOccluding * aOccluded
            lowerOccluding += lowerAmt.toFloat()
            higherOccluding += higherAmt.toFloat()
        }
        return min(lowerOccluding, higherOccluding) * OCCLUDED_COST
    }

    private fun getLinkCountOnSide(mat: ExitMatrix, ad: Layout?, side: Int): Float {
        return (mat.getLinkCount(ad, RelativeSide.FACING, side) +
                mat.getLinkCount(ad, RelativeSide.MIDDLE, side) +
                mat.getLinkCount(ad, RelativeSide.OPPOSITE, side))
    }

    companion object {

        private const val OCCLUDED_COST = .5f
        private const val STRAIGHT_VS_CORNER_COST = .5f
    }
}
