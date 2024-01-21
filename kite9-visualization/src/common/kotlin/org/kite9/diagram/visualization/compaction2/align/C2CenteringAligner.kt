package org.kite9.diagram.visualization.compaction2.align

import org.kite9.diagram.common.elements.Dimension
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
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
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
class C2CenteringAligner : Aligner, Logable {

    private var log = Kite9Log.instance(this)

    override fun alignFor(co: Container, de: Set<Rectangular>, c: C2Compaction, d: Dimension) {
        val sso = c.getSlackOptimisation(d)
        log.send("Center Align: $d $co", de)
        val l = co.getLayout()
        val inLine = isHorizontal(l) == d.isHoriz()
        if (l === Layout.GRID) {
            // you can't centre anything on a grid, since cells use all the space in the grid.
            return
        }
        
        val containerSlideables = sso.getSlideablesFor(co)

        if (containerSlideables == null) {
            log.send("Was expecting something for $co")
            return;
        }

        val leftSlack = minSlack(containerSlideables.l)
        val rightSlack = minSlack(containerSlideables.r)
        val slackToHave = min(leftSlack, rightSlack)

        if (inLine) {
            val matches = findRelevantSlideables(de, sso)
            log.send("Slideables to Align: ", matches)
            var i = 0
            while (i < ceil(matches.size / 2.0)) {
                val leftD = matches[i]
                val rightD = matches[matches.size - i - 1]
                var slackUsed = centerSlideables(leftD, rightD, matches.size - i, slackToHave)
                i++
            }

        } else {
            // everything centered, in a line
            val matches = findRelevantSlideables(de, sso)
            log.send("Slideables to Align: ", matches)
            matches.forEach { centerSlideables(it, it, 1, slackToHave) }
        }
    }

    fun findRelevantSlideables(des: Set<Rectangular>, sso: C2SlackOptimisation): List<RectangularSlideableSet> {
        return des.map { sso.getSlideablesFor(it) }
            .filterIsInstance<RectangularSlideableSet>()
            .sortedBy { it.l.minimumPosition }
    }

    private fun centerSlideables(left: RectangularSlideableSet, right: RectangularSlideableSet, elementCount: Int, slackToHave: Int): Int {
        val leftSlack = minSlack(left.l)
        val rightSlack = minSlack(right.r)
        var slackToUse = max(min(leftSlack, rightSlack) - slackToHave,0)
        if (slackToUse == 0) {
            return 0
        }
        slackToUse /= (elementCount + 1)
        try {
            val leftFixed = left.l.minimumPosition + slackToUse
            val rightFixed = right.r.maximumPosition!! - slackToUse
            left.l.minimumPosition = leftFixed
            right.r.maximumPosition = rightFixed

            // remove all remaining slack
            right.r.minimumPosition = right.r.maximumPosition!!
            left.l.maximumPosition = left.l.minimumPosition

            return slackToUse
        } catch (e: Exception) {
            throw LogicException("Could not set center align constraint: ", e)
        }
    }

    private fun minSlack(l: C2Slideable): Int {
        val leftMin = l.minimumPosition
        val leftMax = l.maximumPosition
        return leftMax!! - leftMin
    }

    override fun willAlign(de: Rectangular, d: Dimension): Boolean {
        if (de !is AlignedRectangular) {
            return false
        }
        return if (d.isHoriz()) {
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