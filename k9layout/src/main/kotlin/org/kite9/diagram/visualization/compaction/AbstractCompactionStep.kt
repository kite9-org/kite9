package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.*
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.display.CompleteDisplayer
import kotlin.math.floor
import kotlin.math.max

/**
 * This contains utility methods to deal with insertion of sub-graphs within the overall graph.
 * You should extend this wherever you need to add vertices to a segment.
 *
 *
 */
abstract class AbstractCompactionStep(protected val displayer: CompleteDisplayer) : CompactionStep, Logable {

	protected var log = Kite9Log.instance(this)

    override val isLoggingEnabled = true

    fun getMinimumDistance(first: ElementSlideable, second: ElementSlideable, along: ElementSlideable?, concave: Boolean): Double {
        return first.getMinimumDistance(second, along, concave, displayer)
    }

    protected fun separate(s1: ElementSlideable?, fs: FaceSide) {
        if (s1 != null) {
            for (s2 in fs.all) {
                separate(s1, s2)
            }
        }
    }

    protected fun separate(fs: FaceSide, s2: ElementSlideable) {
        for (s1 in fs.all) {
            separate(s1, s2)
        }
    }

    protected fun separate(s1: ElementSlideable?, s2: ElementSlideable?) {
        if ((s1!=null) && (s2 !=null)) {
            val minDistance = getMinimumDistance(s1, s2, null, true)
            s1.so.ensureMinimumDistance(s1, s2, minDistance.toInt())
        }
    }

    protected fun getLeavingConnections(
        s: ElementSlideable?,
        c: Compaction
    ): Set<Connection> {
        if (s==null) {
            return emptySet()
        }

        return s.getAdjoiningSlideables(c)
            .flatMap { it.connections }
            .toSet()
    }

}