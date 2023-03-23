package org.kite9.diagram.visualization.compaction.align

import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.AlignedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.Layout.Companion.isHorizontal
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


/**
 * Basic approach:
 * - The container is fixed, but we want to make the amount of space between each element, or element and container, even.
 * - To do this, proceed from the outside to the inside elements.
 * - For the outside elements, work out the minimum amount of slack they have, divide it by the number of gaps (i.e. remaining elements + 1)
 * - Reduce the outside gaps by this amount.
 * - Move to the next level of analysis.
 */
class CenteringAligner : Aligner, Logable {

    protected var log = Kite9Log.instance(this)

    override fun alignFor(co: Container, des: Set<Rectangular>, c: Compaction, horizontal: Boolean) {
        val sso = if (horizontal) c.getVerticalSegmentSlackOptimisation() else c.getHorizontalSegmentSlackOptimisation()
        log.send("Center Align: " + if (horizontal) "horiz" else "vert", des)
        val l = co.getLayout()
        val inLine = isHorizontal(l) == horizontal
        if (l === Layout.GRID) {
            // you can't centre anything on a grid, since cells use all the space in the grid.
            return
        }

        val containerSlideables = sso.getSlideablesFor(co)
        val leftSlack = minSlack(containerSlideables.a)
        val rightSlack = minSlack(containerSlideables.b)
        var slackToHave = min(leftSlack, rightSlack)

        if (inLine) {
            val matches = findRelevantSlideables(des, sso)
            if (matches.size != des.size * 2) {
                return
                //throw new LogicException("Elements missing");
            }
            log.send("Slideables to Align: ", matches)
            var i = 0
            while (i < ceil(des.size / 2.0)) {
                val leftD = matches[i * 2]
                val rightD = matches[matches.size - i * 2 - 1]
                var slackUsed = centerSlideables(leftD, rightD, des.size - i * 2, slackToHave)
                i++
            }
        } else {
            // do one-at-a-time
            for (r in des) {
                val (a, b) = sso.getSlideablesFor(
                    r
                )
                var slackUsed = centerSlideables(a, b, 1, slackToHave)
            }
        }
    }

    fun findRelevantSlideables(des: Set<Rectangular>, sso: SegmentSlackOptimisation): List<SegmentSlideable> {
        return sso.getAllSlideables()
            .filterIsInstance<SegmentSlideable>()
            .filter { it.hasUnderlying(des) }
            .sortedBy { it.minimumPosition }
    }

    private fun centerSlideables(left: SegmentSlideable?, right: SegmentSlideable?, elementCount: Int, slackToHave: Int): Int {
        val leftSlack = minSlack(left)
        val rightSlack = minSlack(right)
        var slackToUse = max(min(leftSlack, rightSlack) - slackToHave,0)
        if (slackToUse == 0) {
            return 0
        }
        slackToUse /= (elementCount + 1)
        try {
            val leftFixed = left!!.minimumPosition + slackToUse
            val rightFixed = right!!.maximumPosition!! - slackToUse
            left.minimumPosition = leftFixed
            right.maximumPosition = rightFixed

            // remove all remaining slack
            right.minimumPosition = right.maximumPosition!!
            left.maximumPosition = left.minimumPosition

            return slackToUse
        } catch (e: Exception) {
            throw LogicException("Could not set center align constraint: ", e)
        }
    }

    private fun minSlack(l: SegmentSlideable?): Int {
        val leftMin = l!!.minimumPosition
        val leftMax = l.maximumPosition
        return leftMax!! - leftMin
    }

    override fun willAlign(de: Rectangular, horizontal: Boolean): Boolean {
        if (de !is AlignedRectangular) {
            return false
        }
        return if (horizontal) {
            de.getHorizontalAlignment() === HorizontalAlignment.CENTER
        } else {
            de.getVerticalAlignment() === VerticalAlignment.CENTER
        }
    }

    override val prefix: String
        get() = "CNRA"
    override val isLoggingEnabled: Boolean
        get() = true
}