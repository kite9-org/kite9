package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.anchors.Anchor
import org.kite9.diagram.visualization.compaction2.anchors.IntersectAnchor
import org.kite9.diagram.visualization.compaction2.anchors.OrbitAnchor
import org.kite9.diagram.visualization.compaction2.anchors.RectAnchor
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import kotlin.math.max
import kotlin.math.min

class C2Slideable(
    so: C2SlackOptimisation,
    val dimension: Dimension,
    val anchors: MutableSet<Anchor<*>>,
    val intersectingGroups: Set<LeafGroup>
) : Slideable(so) {

    val number: Int = nextNumber()
    var done = false

    /**
     * This is for rectangular slideables
     */
    constructor(so: C2SlackOptimisation,
                dimension: Dimension,
                de: Rectangular,
                side: Side) : this(so, dimension, mutable2(setOf(RectAnchor(de, side))), emptySet())


    /**
     * Orbit slideables
     */
    constructor(so: C2SlackOptimisation, dimension: Dimension, anchors: Set<OrbitAnchor>) : this(so, dimension, mutable2(anchors), emptySet())

    /**
     * For intersection slideables
     */
    constructor(so: C2SlackOptimisation, dimension: Dimension, g: LeafGroup?, intersects: Rectangular) : this(so, dimension,
        mutable2(setOf(IntersectAnchor(intersects))), setOfNotNull(g)
    )

    /**
     * Used for labels
     */
    constructor(so: C2SlackOptimisation, dimension: Dimension, intersects: List<Label>) : this(so, dimension,
        mutable2(intersects.map { IntersectAnchor(it) }.toSet() ), emptySet())

    fun routesTo(increasing: Boolean) : Map<C2Slideable, Int> {
        return if (increasing) {
            minimum.forward
                .map { (k, v) -> k.owner as C2Slideable to v }
                .toMap()
        } else {
            maximum.forward
                .map { (k, v) -> k.owner as C2Slideable to v }
                .toMap()
        }
    }


    private fun optionalMin(s: C2Slideable) = if (this.maximumPosition != null) {
        if (s.maximumPosition != null) {
            min(this.maximumPosition!!, s.maximumPosition!!)
        } else {
            this.maximumPosition
        }
    } else {
        null
    }

    fun merge(s: C2Slideable) : C2Slideable {
        if (s.dimension == dimension) {
            val out = C2Slideable(
                so as C2SlackOptimisation,
                dimension,
                this.anchors.plus(s.anchors).toMutableSet(),
                this.intersectingGroups.plus(s.intersectingGroups))

            handleMinimumMaximumAndDone(out, s)
            return out
        } else {
            throw LogicException("Can't merge $this with $s")
        }
    }

    fun addForeignOrbits(a: Set<OrbitAnchor>) {
        val existing = orbiting()
        val newAnchors = a.filter { n -> existing.find { it == n.e }  == null }
        this.anchors.addAll(newAnchors as Collection<Anchor<Any>>)
    }

    override fun toString(): String {
        val ints = intersecting()
        val orbs = orbiting()
        val rects = getRectangulars()

        val char = when {
            ints.isNotEmpty() -> 'I'
            orbs.isNotEmpty() -> 'O'
            rects.isNotEmpty() -> 'R'
            else -> 'X'
        }
        return "C2S${char}($number, $dimension, min=$minimumPosition, max=$maximumPosition done=$done${if (anchors.isNotEmpty()) " i/s=${intersecting()} orbits=${orbiting()} anchors=$anchors" else ""})"
    }

    fun intersecting() : Set<DiagramElement> {
        return anchors
            .filterIsInstance<IntersectAnchor>()
            .map { it.e }
            .toSet()
    }

    fun orbiting() : Set<DiagramElement> {
        return getOrbits()
            .map { it.e }
            .toSet()
    }

    fun getOrbits(): Set<OrbitAnchor> {
        return anchors
            .filterIsInstance<OrbitAnchor>()
            .toSet()
    }

    fun getRectangulars(): Set<RectAnchor> {
        return anchors.filterIsInstance<RectAnchor>().toSet()
    }

    fun isBlocker() : Boolean {
        return getRectangulars().isNotEmpty()
    }

    private fun handleMinimumMaximumAndDone(
        out: C2Slideable,
        s: C2Slideable
    ) {
        out.minimum.merge(minimum, setOf(s.minimum, minimum))
        out.minimum.merge(s.minimum, setOf(s.minimum, minimum))
        out.maximum.merge(maximum, setOf(s.maximum, maximum))
        out.maximum.merge(s.maximum, setOf(s.maximum, maximum))
        out.minimumPosition = max(this.minimumPosition, s.minimumPosition)
        out.maximumPosition = optionalMin(s)
        this.done = true
        s.done = true
    }

    fun addAnchor(a: Anchor<*>) {
        anchors.add(a)
    }

    companion object {

        var n: Int = 0

        fun nextNumber() : Int {
            n++
            return n
        }

        private fun mutable2(anchors: Set<Anchor<*>>): MutableSet<Anchor<*>> {
            return (anchors as Set<Anchor<Any>>).toMutableSet()
        }
    }


}