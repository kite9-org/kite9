package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side

interface SlideableSet {

    fun getAll() : Collection<C2Slideable>
}
interface RectangularSlideables : SlideableSet {

    val l: C2Slideable
    val r: C2Slideable
}

interface RoutableSlideables {
    val bl: C2Slideable
    val br: C2Slideable
    val c: C2Slideable
}

data class RectangularSlideablesImpl(override val l: C2Slideable, override val r: C2Slideable) : RectangularSlideables {

    fun merge(old: C2Slideable, new: C2Slideable): RectangularSlideables {
        return if (l == old) {
            RectangularSlideablesImpl(new, r)
        } else if (r == old) {
            RectangularSlideablesImpl(l, new)
        } else {
            throw LogicException("Slideable not present $old")
        }
    }

    override fun getAll(): Collection<C2Slideable> {
        return listOf(l, r)
    }

}

data class RoutableSlideablesImpl(override val bl: C2Slideable,
                                   override val l: C2Slideable,
                                   override val c: C2Slideable,
                                   override val r: C2Slideable,
                                   override val br: C2Slideable) : RectangularSlideables, RoutableSlideables {

    fun merge(old: C2Slideable, new: C2Slideable): RoutableSlideables {
        return if (bl == old) {
            RoutableSlideablesImpl(new, l, c, r, br)
        } else if (l == old) {
            RoutableSlideablesImpl(bl, new, c, r, br)
        } else if (c == old) {
            RoutableSlideablesImpl(bl, l, new, r, br)
        } else if (r == old) {
            RoutableSlideablesImpl(bl, l, c, new, br)
        } else if (br == old) {
            RoutableSlideablesImpl(bl, l, c, r, new)
        } else {
            throw LogicException("Slideable not present $old")
        }
    }

    override fun getAll(): Collection<C2Slideable> {
        return listOf(bl, l, c, r, br)
    }
}



/**
 * Augments SlackOptimisation to keep track of diagram elements underlying the slideables.
 * @author robmoffat
 */
class C2SlackOptimisation(private val theDiagram: Diagram) : AbstractSlackOptimisation(), Logable {

    private val map: MutableMap<DiagramElement, SlideableSet> = HashMap()

    private fun isRectangular(underlying: DiagramElement): Boolean {
        return underlying is Rectangular
    }

    private fun isConnected(underlying: DiagramElement): Boolean {
        return underlying is Connected
    }

    override fun initialiseSlackOptimisation() {
        val ss = map[theDiagram]!!
        if (ss is RectangularSlideables) {
            ss.l.minimumPosition = 0
        }
    }

    fun getSlideablesFor(de: DiagramElement): SlideableSet? {
        return map[de]
    }

    fun merge(s1: Slideable, s2: Slideable) {

    }

    fun add(de: DiagramElement, ss: SlideableSet) {
        map[de] = ss

    }
}