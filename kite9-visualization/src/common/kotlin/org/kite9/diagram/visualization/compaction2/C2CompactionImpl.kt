package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
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

    override val junctions = mutableMapOf<C2BufferSlideable, List<C2Slideable>>()

    override fun createInitialJunctions(along: RoutableSlideableSet, r1: RoutableSlideableSet, r2: RectangularSlideableSet?) {
        junctions[along.bl] = listOfNotNull(r1.bl, r1.c, r1.br)
        val c = along.c
        if (c != null) {
            junctions[c] = listOfNotNull(r1.bl, r2?.l, r1.c, r2?.c, r2?.r, r1.br).distinct()
        }
        junctions[along.br] = listOfNotNull(r1.bl, r1.c, r1.br)
    }

    override fun createContainerJunctions(along: RectangularSlideableSet, inside: RoutableSlideableSet) {
        inside.getAll().forEach {
            val orig = junctions[it] ?: emptyList()
            junctions[it] = (listOf(along.l) + orig + listOfNotNull(along.r, along.c))
        }
    }

    override fun createRoutableJunctions(along: RoutableSlideableSet, r1: RoutableSlideableSet) {
        setOfNotNull(along.br, along.bl, along.c).forEach {
            val orig = junctions[it] ?: emptyList()
            val full = (r1.getAll() + orig)
                .sortedWith(comparator).distinct()
            junctions[it] = full
        }
    }

    override fun replaceJunction(s1: C2Slideable?, s2: C2Slideable?, sNew: C2Slideable) {
        // handle keys first
        if (sNew is C2BufferSlideable) {
            var k1 = junctions.remove(s1) ?: emptyList()
            var k2 = junctions.remove(s2) ?: emptyList()

            junctions[sNew] = interleave(k1,k2)
        }

        // now check all values
        val keys = junctions.keys
        keys.forEach { k ->
            var vals = junctions[k]!!
            vals = vals.map {
                if ((it == s1) || (it == s2)) {
                    sNew
                } else {
                    it
                }
            }
            junctions[k] = vals
        }
    }

    private fun interleave(k1: List<C2Slideable>, k2: List<C2Slideable>): List<C2Slideable> {
        val out = (k1 + k2).sortedWith (comparator).distinct()
        return out
    }

    override fun resortJunctions() {
        junctions.keys.forEach {
            val orig = junctions[it]
            val sorted = orig!!.sortedWith(comparator).distinct()
            junctions[it]=sorted
        }
    }

    override fun consistentJunctions() {
        val reversed = junctions.entries
            .flatMap { (k, v) -> v.map { it to k } }
            .groupBy( keySelector = { it.first }, valueTransform = { it.second })

        junctions.keys.forEach {
            val old = junctions[it] ?: mutableListOf()
            val new = reversed[it] ?: mutableListOf()
            junctions[it] = old.plus(new)
        }

        reversed.keys
            .filterIsInstance<C2BufferSlideable>()
            .forEach {
                if (junctions[it] == null) {
                    junctions[it] = reversed[it]!!
                }
            }
    }

    class SlideableComparator : Comparator<C2Slideable> {
        override fun compare(a: C2Slideable, b: C2Slideable): Int {
                if (a.minimumPosition < b.minimumPosition) {
                    return -1
                } else if (b.minimumPosition < a.minimumPosition) {
                    return 1
                } else if (a.routesTo(true).containsKey(b)) {
                    return -1
                } else if (b.routesTo(true).containsKey(a)) {
                    return 1
                } else {
                    return 0
                }
            }
    }



    companion object {

        val comparator = SlideableComparator()

    }

}