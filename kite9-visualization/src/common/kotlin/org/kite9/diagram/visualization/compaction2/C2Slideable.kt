package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.anchors.*
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import kotlin.math.max
import kotlin.math.min

class C2Slideable(
    so: C2SlackOptimisation,
    val dimension: Dimension,
    private val anchors: MutableSet<Anchor<*>>,
    val intersectingGroups: Set<LeafGroup>
) : Slideable(so) {

    val number: Int = nextNumber()
    var mergedInto : C2Slideable? = null

    /**
     * This is for rectangular slideables
     */
    constructor(so: C2SlackOptimisation,
                dimension: Dimension,
                de: Rectangular,
                side: Side) : this(so, dimension, mutable2(setOf(RectAnchor(de, side))), emptySet())


    /**
     * For new connection slideables or Orbit Slideables
     */
    constructor(so: C2SlackOptimisation, dimension: Dimension, anchors: Set<Anchor<*>>) : this(so, dimension, mutable2(anchors), emptySet())

    /**
     * For intersection slideables
     */
    constructor(so: C2SlackOptimisation, dimension: Dimension, g: LeafGroup?, intersects: Rectangular, sides: Set<Side>) : this(so, dimension,
        mutable2(setOf(IntersectAnchor(intersects, sides))), setOfNotNull(g)
    )

    /**
     * Used for labels
     */
    constructor(so: C2SlackOptimisation, dimension: Dimension, intersects: List<Label>) : this(so, dimension,
        mutable2(intersects.map { IntersectAnchor(it, setOf(Side.START, Side.END)) }.toSet() ), emptySet())

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
        if (this.isDone() || s.isDone()) {
            throw LogicException("Already merged")
        } else if (s.dimension == dimension) {
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
        return "C2S${char}($number, $dimension, min=$minimumPosition, max=$maximumPosition done=${isDone()} ${if (anchors.isNotEmpty()) " i/s=${intersecting()} orbits=${orbiting()} anchors=$anchors" else ""})"
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

    fun getConnAnchors(): Set<ConnAnchor> {
        return anchors
            .filterIsInstance<ConnAnchor>()
            .toSet()
    }

    fun getIntersectionAnchors(): Set<IntersectAnchor> {
        return anchors
            .filterIsInstance<IntersectAnchor>()
            .toSet()
    }

    fun getRectangulars(): Set<RectAnchor> {
        return anchors.filterIsInstance<RectAnchor>().toSet()
    }

    fun isBlocker(d: Direction) : Boolean {
        return getRectangulars()
            .filter {
                if (it.e is Container) {
                    if (it.s != null) {
                        return when (it.e.getTraversalRule(d)) {
                            BorderTraversal.ALWAYS -> false
                            BorderTraversal.LEAVING -> it.s.isEntering(d)
                            BorderTraversal.PREVENT -> true
                        }
                    } else {
                        return false
                    }

                } else {
                    return true
                }

            }.isNotEmpty()
    }

    fun replaceConnAnchors(ca: Set<ConnAnchor>) {
        val toKeep = anchors.filter { !(it is ConnAnchor) }
        anchors.clear()
        anchors.addAll(toKeep)
        anchors.addAll(ca)
    }

    fun isDone() : Boolean = this.mergedInto != null

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
        this.mergedInto = out
        s.mergedInto = out
    }

    fun addAnchor(a: ConnAnchor) {
        anchors.add(a)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as C2Slideable

        if (number != other.number) return false

        return true
    }

    override fun hashCode(): Int {
        return number
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