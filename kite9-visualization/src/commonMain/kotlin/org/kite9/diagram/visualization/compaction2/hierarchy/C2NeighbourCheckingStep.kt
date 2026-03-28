package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2CompactionImpl
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.min

class C2NeighbourCheckingStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override fun compact(c: C2Compaction, g: Group) {
        invertNeighbours(c as C2CompactionImpl)
        joinOverlappingNeighbourGroups(c as C2CompactionImpl)

    }



    /**
     * This runs a check to make sure that if you can move onto a slideable in
     * one direction, you can also move onto it in the other direction.
     */
    fun invertNeighbours(c: C2CompactionImpl) {

        fun invert(perp: C2Slideable) {
            if (perp == null) {
                throw LogicException("Was expecting perp to be set")
            }
            val all = c.neighbourDetails
                .filterValues { it.flatMap { it }.contains(perp) }
                .keys

            val superSet = c.neighbourDetails.getOrPut(perp) { mutableSetOf() }
            if (superSet.size == 1) {
                val newContents = all + superSet.first()
                superSet.clear()
                superSet.add(newContents)
            }
        }

        c.getSlackOptimisation(Dimension.V).getAllSlideables().forEach { invert(it) }
        c.getSlackOptimisation(Dimension.H).getAllSlideables().forEach { invert(it) }
    }


    override val prefix: String
        get() = "NBCS"

    override val isLoggingEnabled: Boolean
        get() = true

    companion object {

        fun joinOverlappingNeighbourGroups(c: C2CompactionImpl) {

            fun calculateExtent(ss: Set<C2Slideable>) : Pair<Int, Int> {
                return Pair(ss.minOf { it.minimumPosition }, ss.maxOf { it.minimumPosition })
            }

            fun inside(x: Int, a: Pair<Int, Int>) : Boolean {
                return x>=a.first && x<=a.second
            }

            fun overlaps(a: Pair<Int, Int>, b: Pair<Int, Int>) : Boolean {
                return inside(a.first, b)
                        || inside(a.second, b)
                        || inside(b.first, a)
                        || inside(b.second, a)
            }

            fun mergeExtents(a: Pair<Int, Int>, b: Pair<Int, Int>) : Pair<Int, Int> {
                return Pair(min(a.first, b.first), max(a.second, b.second))
            }

            c.neighbourDetails.keys.forEach { k ->
                val oldGroups = c.neighbourDetails[k]!!
                val extents = oldGroups.associateBy { calculateExtent(it) }
                val newGroups = mutableMapOf<Pair<Int, Int>, Set<C2Slideable>>()
                extents.forEach { (e, ss) ->
                    val overlapGroups = newGroups.filter { (k, _) -> overlaps(e, k) }
                    if (overlapGroups.isEmpty()) {
                        newGroups[e] = ss
                    } else {
                        val combinedExtent = overlapGroups.keys.reduce { a, b -> mergeExtents(a, b) }
                        val combinedSet = overlapGroups.values.reduce { a, b -> a + b }
                        overlapGroups.keys.forEach { newGroups.remove(it) }
                        newGroups[mergeExtents(combinedExtent, e)] = combinedSet + ss
                    }
                }
                c.neighbourDetails[k] = newGroups.values.toMutableSet()
            }
        }
    }
}