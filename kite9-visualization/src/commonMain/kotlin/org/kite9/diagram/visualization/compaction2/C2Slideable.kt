package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Layout
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
    var mergedInto: C2Slideable? = null

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
            intersects: DiagramElement,
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

    fun merge(sIn: C2Slideable): C2Slideable {
        val s1 = sIn.getNotDoneVersion()
        val s2 = this.getNotDoneVersion()
        if (s1.dimension == s2.dimension) {
            val out =
                    C2Slideable(
                            so as C2SlackOptimisation,
                            s1.dimension,
                            s1.anchors.plus(s2.anchors).toMutableSet(),
                            s1.intersectingGroups.plus(s2.intersectingGroups)
                    )

            s2.handleMinimumMaximumAndDone(out, s1)
            return out
        } else {
            throw LogicException("Can't merge $s1 with $s2")
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

    fun getIntersectingElements(): Set<DiagramElement> {
        return anchors.filterIsInstance<IntersectAnchor>().map { it.e }.toSet()
    }

    fun getOrbitingElements(): Set<DiagramElement> {
        return getOrbitAnchors().map { it.e }.toSet()
    }

    fun getRectElements() : Set<DiagramElement> {
        return getRectAnchors().map { it.e }.toSet()
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
    fun getRelevantRectAnchor(c: DiagramElement) : RectAnchor? {

        fun isGridCell(e: DiagramElement) : Boolean {
            return (e.getParent() as Container)?.getLayout() == Layout.GRID
        }

        val relevant = anchors
            .filterIsInstance<RectAnchor>()
            .filter { it.e == c || ((c is Container) && (c.getContents().contains(it.e)))}
            .filter { !isGridCell(it.e) }

        if (relevant.size > 1) {
            throw LogicException("Not sure how this can be a permeable anchor for multiple things")
        }

        return relevant.firstOrNull()
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

//    fun isInsideOneOf(containers: Set<DiagramElement>) : Boolean {
//        val myContainedElements = getIntersectingElements()
//
//        if (containers.find { c -> myContainedElements.contains(c) } != null) {
//            return true
//        }
//
//        val allElements = (myContainedElements + getOrbitingElements()).toSet()
//
//        val found = containers.filterIsInstance<Container>().find {
//            c -> allElements.find {
//                e-> c.deepContains(e)
//            } != null
//        } != null
//        return found
//    }

    fun replaceConnAnchors(ca: Set<ConnAnchor>) {
        val toKeep = anchors.filter { !(it is ConnAnchor) }
        anchors.clear()
        anchors.addAll(toKeep)
        anchors.addAll(ca)
    }

    fun isDone(): Boolean = this.mergedInto != null

    private fun handleMinimumMaximumAndDone(out: C2Slideable, s: C2Slideable) {
        out.minimum.merge(minimum, setOf(s.minimum, minimum), so.getSize())
        out.minimum.merge(s.minimum, setOf(s.minimum, minimum), so.getSize())
        out.maximum.merge(maximum, setOf(s.maximum, maximum), so.getSize())
        out.maximum.merge(s.maximum, setOf(s.maximum, maximum), so.getSize())
        out.minimumPosition = max(this.minimumPosition, s.minimumPosition)
        out.maximumPosition = optionalMin(s)
        this.mergedInto = out
        s.mergedInto = out
    }

    fun addConnAnchor(a: ConnAnchor) {
        anchors.add(a)
    }

    fun addRectAnchor(a: RectAnchor) {
        if (anchors.filterIsInstance<OrbitAnchor>().isNotEmpty()) {
            throw LogicException("You shouldn't have orbits and rects on the same slideable")
        }
        anchors.add(a)
    }

    fun addIntersectAnchor(a: IntersectAnchor) {
        anchors.add(a)
    }

    fun addOrbitAnchor(o: OrbitAnchor) {
        anchors.add(o)
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

    fun getNotDoneVersion() : C2Slideable {
        return getNonDoneVersion(this)!!
    }

    override fun addMinimumForwardConstraint(to: Slideable, dist: Int) {
        if (this.number != (to as C2Slideable).number) {
            super.addMinimumForwardConstraint(to, dist)
        } else {
            throw LogicException("Can't add a constraint to yourself")
        }
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

        fun getNonDoneVersion(c2Slideable: C2Slideable?): C2Slideable? {
            if (c2Slideable == null) {
                return null
            }

            while (c2Slideable.isDone()) {
                return getNonDoneVersion(c2Slideable.mergedInto)
            }

            return c2Slideable
        }
    }
}
