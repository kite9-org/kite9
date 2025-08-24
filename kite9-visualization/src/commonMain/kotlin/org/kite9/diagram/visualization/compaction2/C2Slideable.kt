package org.kite9.diagram.visualization.compaction2

import kotlin.math.max
import kotlin.math.min
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.anchors.*
import org.kite9.diagram.visualization.compaction2.routing.C2Route
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

class C2Slideable(
        so: C2SlackOptimisation,
        val dimension: Dimension,
        private val anchors: MutableSet<Anchor<*>>,
        val intersectingGroups: Set<LeafGroup>
) : Slideable(so) {

    val number: Int = nextNumber()
    var mergedInto: C2Slideable? = null

    init {
        println("New slideable $this")
    }

    /** This is for rectangular Slideables */
    constructor(
            so: C2SlackOptimisation,
            dimension: Dimension,
            de: Rectangular,
            side: Side,
            p: Permeability
    ) : this(so, dimension, mutable2(setOf(RectAnchor(de, side, p))), emptySet())

    /** For new connection slideables or orbit Slideables */
    constructor(
            so: C2SlackOptimisation,
            dimension: Dimension,
            anchors: Set<Anchor<*>>
    ) : this(so, dimension, mutable2(anchors), emptySet())

    /** For intersection slideables */
    constructor(
            so: C2SlackOptimisation,
            dimension: Dimension,
            intersects: Rectangular,
            purpose: Purpose
    ) : this(so, dimension, mutable2(setOf(IntersectAnchor(intersects, purpose))), emptySet())

    /** Used for labels */
    constructor(
            so: C2SlackOptimisation,
            dimension: Dimension,
            intersects: List<Label>
    ) : this(
            so,
            dimension,
            mutable2(intersects.map { IntersectAnchor(it, Purpose.LABEL_LAYOUT) }.toSet()),
            emptySet()
    )

    private fun optionalMin(s: C2Slideable) =
            if (this.maximumPosition != null) {
                if (s.maximumPosition != null) {
                    min(this.maximumPosition!!, s.maximumPosition!!)
                } else {
                    this.maximumPosition
                }
            } else {
                null
            }

    fun merge(s: C2Slideable): C2Slideable {
        if (this.isDone() || s.isDone()) {
            throw LogicException("Already merged")
        } else if (s.dimension == dimension) {
            val out =
                    C2Slideable(
                            so as C2SlackOptimisation,
                            dimension,
                            this.anchors.plus(s.anchors).toMutableSet(),
                            this.intersectingGroups.plus(s.intersectingGroups)
                    )

            handleMinimumMaximumAndDone(out, s)
            return out
        } else {
            throw LogicException("Can't merge $this with $s")
        }
    }

    override fun toString(): String {
        val ints = getIntersectingElements()
        val orbs = getOrbitingElements()
        val rects = getRectAnchors()

        val char =
                when {
                    ints.isNotEmpty() -> 'I'
                    orbs.isNotEmpty() -> 'O'
                    rects.isNotEmpty() -> 'R'
                    else -> 'X'
                }
        return "C2S${char}($number, $dimension, min=$minimumPosition, max=$maximumPosition done=${isDone()} ${if (anchors.isNotEmpty()) " i/s=${getIntersectingElements()} orbits=${getOrbitingElements()} anchors=$anchors" else ""})"
    }

    private fun getNonPermeables(anchors: List<PermeableAnchor>, d: Direction): Set<DiagramElement> {
        return anchors
                .filter { !it.canCross(d) }
                .map { it.e }
                .toSet()
    }

    fun getIntersectingElements(): Set<DiagramElement> {
        return anchors.filterIsInstance<IntersectAnchor>().map { it.e }.toSet()
    }

    fun getOrbitingElements(): Set<DiagramElement> {
        return getOrbitAnchors().map { it.e }.toSet()
    }

    fun getOrbitAnchors(): Set<OrbitAnchor> {
        return anchors.filterIsInstance<OrbitAnchor>().toSet()
    }

    fun getConnAnchors(): Set<ConnAnchor> {
        return anchors.filterIsInstance<ConnAnchor>().toSet()
    }

    fun getIntersectAnchors(): Set<IntersectAnchor> {
        return anchors.filterIsInstance<IntersectAnchor>().toSet()
    }

    fun getRectAnchors(): Set<RectAnchor> {
        return anchors.filterIsInstance<RectAnchor>().toSet()
    }

    /**
     * For the current slideable (this), works out which anchor represents the intersection
     * with along.
     */
    fun inElementIntersection(along: C2Slideable) : PermeableAnchor? {

        fun containsTheIntersection(intersections: Set<DiagramElement>, r: PermeableAnchor)
                = intersections.firstOrNull { i -> r.e == i || r.e.deepContains(i) } != null

        fun containsTheOrbit(orbits: Set<DiagramElement>, r: PermeableAnchor)
                = orbits.firstOrNull { o -> r.e.deepContains(o) } != null


        val intersectingElements = along.getIntersectingElements()
        val orbitElements = along.getOrbitingElements()

        val out = this.anchors.filterIsInstance<PermeableAnchor>()
            .filter {
                containsTheIntersection(intersectingElements, it) ||
                        containsTheOrbit(orbitElements,  it)
            }

        if (out.isEmpty()) {
            return null
        } else if (out.size == 1) {
            return out.first()
        } else {
            throw LogicException("Multiple permeable anchors for $this")
        }
    }

    /**
     * You can't move along an intersection element if you're inside the element itself.
     * Only the visible part of the kebab stick.
     */
    fun canMoveAlongInside(container: DiagramElement) : Boolean {
        val intersecting = getIntersectingElements()
        val somethingContains = intersecting.filterIsInstance<Container>().find { it == container || it.deepContains(container)  } != null
        return !somethingContains
    }

    fun replaceConnAnchors(ca: Set<ConnAnchor>) {
        val toKeep = anchors.filter { !(it is ConnAnchor) }
        anchors.clear()
        anchors.addAll(toKeep)
        anchors.addAll(ca)
    }

    fun isDone(): Boolean = this.mergedInto != null

    private fun handleMinimumMaximumAndDone(out: C2Slideable, s: C2Slideable) {
        out.minimum.merge(minimum, setOf(s.minimum, minimum))
        out.minimum.merge(s.minimum, setOf(s.minimum, minimum))
        out.maximum.merge(maximum, setOf(s.maximum, maximum))
        out.maximum.merge(s.maximum, setOf(s.maximum, maximum))
        out.minimumPosition = max(this.minimumPosition, s.minimumPosition)
        out.maximumPosition = optionalMin(s)
        this.mergedInto = out
        s.mergedInto = out
    }

    fun addConnAnchor(a: ConnAnchor) {
        anchors.add(a)
    }

    fun addBlockAnchor(a: BlockAnchor) {
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

        fun nextNumber(): Int {
            n++
            return n
        }

        private fun mutable2(anchors: Set<Anchor<*>>): MutableSet<Anchor<*>> {
            @Suppress("UNCHECKED_CAST") return (anchors as Set<Anchor<Any>>).toMutableSet()
        }
    }
}
