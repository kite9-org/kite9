package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side

/**
 * Augments SlackOptimisation to keep track of diagram elements underlying the slideables.
 * @author robmoffat
 */
class C2SlackOptimisation(private val theDiagram: Diagram) : AbstractSlackOptimisation(), Logable {

    private val rectangularElementToSegmentSlideableMap: MutableMap<DiagramElement, OPair<C2Slideable?>> = HashMap()

    private fun isRectangular(underlying: DiagramElement): Boolean {
        return underlying is Rectangular
    }

    override fun updateMaps(s: Slideable) {
        log.send(if (log.go()) null else "Added slideable: $s")
        if (s is C2Slideable) {
            for ((underlying, side) in s.anchors) {
                if (isRectangular(underlying)) {
                    var parts = rectangularElementToSegmentSlideableMap[underlying]
                    if (parts == null) {
                        parts = OPair<C2Slideable?>(null, null)
                    }
                    if (side === Side.START) {
                        parts = OPair(s, parts.b)
                    } else if (side === Side.END) {
                        parts = OPair(parts.a, s)
                    }
                    rectangularElementToSegmentSlideableMap[underlying] = parts
                }
            }
        }
    }

    override fun initialiseSlackOptimisation() {
        val (a) = rectangularElementToSegmentSlideableMap[theDiagram]!!
        a!!.minimumPosition = 0
    }

    fun getSlideablesFor(de: DiagramElement): OPair<C2Slideable?> {
        return rectangularElementToSegmentSlideableMap[de]!!
    }

    fun merge(s1: Slideable, s2: Slideable) {

    }
}