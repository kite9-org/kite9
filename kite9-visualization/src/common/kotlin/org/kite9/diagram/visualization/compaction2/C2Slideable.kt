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

enum class BlockType { NOT_BLOCKING, BLOCKING, ENTERING_CONTAINER, LEAVING_CONTAINER }


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


    /**
     * Figures out what crossing this slideable would mean for a route.
     * Note that if you're on an intersection slideable, you can't cross the rectangular
     * that it's part of.
     */
    fun isBlocker(d: Direction, along: C2Slideable) : BlockType {
        val rs = getRectangulars()
        val out = rs
            .map {
                if (it.e is Container) {
                    if (along.intersecting().contains(it.e)) {
                        return BlockType.BLOCKING
                    } else if (it.s != null) {
                        if (alongDimension(d)) {
                            when (it.e.getTraversalRule(d)) {
                                BorderTraversal.ALWAYS -> if (it.s.isEntering(d)) BlockType.ENTERING_CONTAINER else BlockType.LEAVING_CONTAINER
                                BorderTraversal.LEAVING -> if (it.s.isEntering(d)) BlockType.BLOCKING else  BlockType.LEAVING_CONTAINER
                                BorderTraversal.PREVENT -> BlockType.BLOCKING
                            }
                        } else {
                            BlockType.BLOCKING
                        }
                    } else {
                        BlockType.BLOCKING
                    }
                } else {
                    BlockType.NOT_BLOCKING
                }

            }

        val out2 =  out.reduceOrNull { acc, v ->
            when (acc) {
                BlockType.BLOCKING -> BlockType.BLOCKING
                BlockType.ENTERING_CONTAINER -> when (v) {
                    BlockType.BLOCKING -> BlockType.BLOCKING
                    BlockType.LEAVING_CONTAINER -> throw LogicException("Can't enter and leave!")
                    else -> BlockType.ENTERING_CONTAINER
                }
                BlockType.LEAVING_CONTAINER -> when (v) {
                    BlockType.BLOCKING -> BlockType.BLOCKING
                    BlockType.ENTERING_CONTAINER -> throw LogicException("Can't enter and leave!")
                    else -> BlockType.LEAVING_CONTAINER
                }
                BlockType.NOT_BLOCKING -> v
            }
        } ?: BlockType.NOT_BLOCKING

        //println ("$d is $out2 on $this")

        return out2
    }

    private fun alongDimension(d: Direction): Boolean {
        return when (d) {
            Direction.UP -> dimension == Dimension.V
            Direction.DOWN ->  dimension == Dimension.V
            Direction.LEFT -> dimension == Dimension.H
            Direction.RIGHT -> dimension == Dimension.H
        }
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