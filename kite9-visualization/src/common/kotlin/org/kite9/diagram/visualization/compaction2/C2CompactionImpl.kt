package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet

/**
 * Flyweight class that handles the state of the compaction as it goes along.
 * Contains lots of utility methods too.
 *
 *
 * @author robmoffat
 */
class C2CompactionImpl(private val diagram: Diagram) : C2Compaction {

    private val horizontalSegmentSlackOptimisation = C2SlackOptimisation(this)
    private val verticalSegmentSlackOptimisation = C2SlackOptimisation(this)

    override fun getSlackOptimisation(d: Dimension): C2SlackOptimisation {
        return if (d ==Dimension.H) {
            horizontalSegmentSlackOptimisation
        } else {
            verticalSegmentSlackOptimisation
        }
    }

    override fun getDiagram(): Diagram {
        return diagram
    }

    override val blockers = mutableMapOf<C2BufferSlideable, Set<C2Slideable>>()

    private fun ensureBlocker(a: C2Slideable, b: C2Slideable) {
        if (a is C2BufferSlideable) {
            val items = blockers.getOrElse(a) { emptySet() } + b
            blockers[a] = items
        }

        if (b is C2BufferSlideable) {
            val items = blockers.getOrElse(b) { emptySet() } + a
            blockers[b] = items
        }
    }

    override fun setupRectangularBlockers(along: RoutableSlideableSet, perp: RectangularSlideableSet) {
        along.c.forEach {
            ensureBlocker(it, perp.l)
            ensureBlocker(it, perp.r)
        }
    }

    /**
     * Need to implement the rules
     */
    override fun setupContainerBlockers(along: RoutableSlideableSet, inside: RectangularSlideableSet) {
        val container = (inside.d as Container)
        along.getAll().forEach {
            ensureBlocker(it, inside.l)
            ensureBlocker(it, inside.r)
        }
    }

    override fun replaceBlockers(s1: C2Slideable?, s2: C2Slideable?, sNew: C2Slideable?) {
        // handle keys first
        if (blockers[sNew] != null) {
            throw LogicException("Calling replaceJunction Twice!")
        }
        if (sNew is C2BufferSlideable) {
            val k1 = blockers.remove(s1) ?: emptySet()
            val k2 = blockers.remove(s2) ?: emptySet()

            blockers[sNew] = k1 + k2
        }

        if (sNew != null) {
            // now check all values
            val keys = blockers.keys
            keys.forEach { k ->
                var vals = blockers[k]!!
                vals = vals.map {
                    if ((it == s1) || (it == s2)) {
                        sNew
                    } else {
                        it
                    }
                }.toSet()
                blockers[k] = vals
            }
        }
    }

    override fun consistentBlockers() {
        val reversed = blockers.entries
            .flatMap { (k, v) -> v.map { it to k }.toSet() }
            .groupBy( keySelector = { it.first }, valueTransform = { it.second })
            .entries
            .map { (k,v) -> k to v.toSet() }
            .toMap()

        blockers.keys.forEach {
            val old = (blockers[it] ?: mutableSetOf()).filterIsInstance<C2BufferSlideable>().toSet()
            val new = (reversed[it] ?: mutableSetOf())
            if (new != old) {
                throw LogicException("These should match")
            }
        }
    }



    override fun checkConsistency() {
        verticalSegmentSlackOptimisation.checkConsistency()
        horizontalSegmentSlackOptimisation.checkConsistency()
    }

}