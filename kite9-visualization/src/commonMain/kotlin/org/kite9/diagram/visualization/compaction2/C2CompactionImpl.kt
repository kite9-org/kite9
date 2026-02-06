package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Positioned
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import kotlin.math.max
import kotlin.math.min

/**
 * Flyweight class that handles the state of the compaction as it goes along.
 * Contains lots of utility methods too.
 *
 *
 * @author robmoffat
 */
class C2CompactionImpl(private val diagram: Diagram) : C2Compaction {

    private val horizontalSegmentSlackOptimisation = C2SlackOptimisation(this, Dimension.H)
    private val verticalSegmentSlackOptimisation = C2SlackOptimisation(this, Dimension.V)

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

    private val neighbourDetails = mutableMapOf<C2Slideable, MutableSet<Set<C2Slideable>>>()

    private fun addNeighbour(along: C2Slideable?, n1: C2Slideable?, n2: C2Slideable?) {
        if ((n1 == null) || (n2==null) || (along==null)) {
            // first guard - along, n1 and n2 must be non-null
            return
        }

        if (along.getRectAnchors().isNotEmpty()) {
            // second guard - you can't route along rectangular edges
            return
        }

        //println("Adding neighbour ${along.number} ${n1.number}-${n2.number}")

        val superSet = neighbourDetails.getOrPut(along) { mutableSetOf() }

        val ns1 = superSet.find { it.contains(n1) }
        val ns2 = superSet.find { it.contains(n2) }


        if ((ns1 == null) && (ns2 == null)) {
            superSet.add(mutableSetOf(n1, n2))
        } else if ((ns1 == null) && (ns2 != null)){
            superSet.remove(ns2)
            superSet.add(ns2.plus(n1))
        } else if ((ns2 == null) && (ns1 != null)) {
            superSet.remove(ns1)
            superSet.add(ns1.plus(n2))
        } else if ((ns2 != ns1) && (ns2 != null) && (ns1!=null)) {
            val ns3 = ns2.plus(ns1)
            superSet.remove(ns2)
            superSet.remove(ns1)
            superSet.add(ns3)

        } else {
            // already in the same set.
        }
    }

    /**
     * If any sets within the superset share members, join those sets.
     */
    private fun simplifySuperSet(s: Set<Set<C2Slideable>>) : MutableSet<Set<C2Slideable>> {
        val out = mutableSetOf<Set<C2Slideable>>()

        s.forEach { s ->
            val hitSets = out.filter { s2 -> s2.any { e -> s.contains(e) } }
            val newSet = hitSets.flatMap { it }.toMutableSet()
            newSet.addAll(s)
            out.removeAll (hitSets.toSet())
            out.add(newSet)
        }

        return out
    }

    override fun getNeighbours(along: C2Slideable, perp: C2Slideable) : Set<C2Slideable> {
        val superSet = neighbourDetails.getOrPut(along) { mutableSetOf() }
        val neighbourSet = superSet.firstOrNull { it.contains(perp) } ?: emptySet()
        return neighbourSet
    }

    private fun getSlideablesIncidentWith(s: C2Slideable) : Set<C2Slideable> {
        return this.neighbourDetails.filter { (_, v) -> v.firstOrNull { it.contains(s) } != null }.keys
    }

    fun replaceIntersections(s1: C2Slideable, s2: C2Slideable, sNew: C2Slideable) {

        // merge alongs
        val s1ss = neighbourDetails.remove(s1) ?: mutableSetOf()
        val s2ss = neighbourDetails.remove(s2) ?: mutableSetOf()
        val combinedSs = simplifySuperSet(s1ss + s2ss)
        neighbourDetails[sNew] = combinedSs

        fun replaceInSuperSet(ss: MutableSet<Set<C2Slideable>>) : MutableSet<Set<C2Slideable>> {
            val out = mutableSetOf<Set<C2Slideable>>()

            ss.forEach {
                if (it.contains(s1) || it.contains(s2)) {
                    val newIt = it.minus(s1).minus(s2).plus(sNew)
                    out.add(newIt)
                } else {
                    out.add(it)
                }
            }

            val out2 = simplifySuperSet(out)
            return out2
        }

        // go through contents and replace
        neighbourDetails.values.forEach { ss ->
            val new = replaceInSuperSet(ss)
            ss.clear()
            ss.addAll(new)
        }
    }

    override fun checkConsistency() {
        verticalSegmentSlackOptimisation.checkConsistency()
        horizontalSegmentSlackOptimisation.checkConsistency()

        neighbourDetails.forEach { (s, ss) ->
            if (s.isDone()) {
                throw LogicException("Done slideable")
            }

            ss.forEach { m ->
                m.forEach {
                    if (it.isDone()) {
                        throw LogicException("Done slideable")
                    }
                }
            }

            val ss2 = simplifySuperSet(ss)
            if (ss2 != ss) {
                throw LogicException("Simplified!")
            }

        }



        println("Consistency ${neighbourDetails.size} ${neighbourDetails.size}")
        println("Consistency ${neighbourDetails.values.sumOf { it.size }}")
    }

    override fun getNeighbourSetsOn(s: C2Slideable) : Set<Set<C2Slideable>> {
        val superSet = neighbourDetails.getOrPut(s) { mutableSetOf() }
        return superSet
    }

    override fun getLocationsOn(s: C2Slideable): Set<C2Slideable> {
        return getSlideablesIncidentWith(s)
    }

    override fun joinOverlappingNeighbourGroups() {

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

        neighbourDetails.keys.forEach { k ->
            val oldGroups = neighbourDetails[k]!!
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
            neighbourDetails[k] = newGroups.values.toMutableSet()
        }
    }

    override fun buildNeighbours() {
        neighbourDetails.clear()
        handleIntersections()
        handleOrbits()
        joinOverlappingNeighbourGroups()
    }

    private fun handleIntersections() {
        val v = getSlackOptimisation(Dimension.V)
        val h = getSlackOptimisation(Dimension.H)

        handleNeighboursOnDimension(v, h, Direction.RIGHT, v.getAllSlideables().filter { it.getIntersectingElements().isNotEmpty() })
        handleNeighboursOnDimension( h, v, Direction.DOWN, h.getAllSlideables().filter { it.getIntersectingElements().isNotEmpty() })
    }

    private fun handleOrbits() {
        val v = getSlackOptimisation(Dimension.V)
        val h = getSlackOptimisation(Dimension.H)

        handleNeighboursOnDimension(v, h, Direction.RIGHT, v.getAllSlideables().filter { it.getOrbitingElements().isNotEmpty() })
        handleNeighboursOnDimension(h, v, Direction.DOWN, h.getAllSlideables().filter { it.getOrbitingElements().isNotEmpty() })
    }


    /**
     * The basic approach here is to trace along the intersection and make sure that every rectangular that crosses the path of it
     * intersects with the intersection.
     */
    private fun handleNeighboursOnDimension(so: C2SlackOptimisation,
                                            sox: C2SlackOptimisation,
                                            d: Direction,
                                            toDo: List<C2Slideable>) {

        fun intersects(s: C2Slideable, from: C2Slideable, to: C2Slideable) : Boolean {
            return (s.minimumPosition >= min(from.minimumPosition, to.minimumPosition)) &&
                    (s.minimumPosition <= max(from.minimumPosition, to.minimumPosition))
        }

        fun getAllPositionedInHierarchy(e: DiagramElement?) : Set<Positioned> {
            return when (e) {
                null -> { emptySet() }
                is Positioned -> {  setOf(e) + getAllPositionedInHierarchy(e.getParent()) }
                else -> { emptySet() }
            }
        }


        fun withinOrbit(along: Set<DiagramElement>, orbits: Set<DiagramElement>) : Boolean {
            if (orbits.isEmpty()) {
                return false
            }
            val out = orbits
                .filterIsInstance<Container>()
                .all { o ->
                    val found = along.any { i ->
                        o.deepContains(i) || o == i
                    }
                    found
                }
            return out
        }

        fun updateOrbits(s: C2Slideable, currentOrbits: Set<DiagramElement>, relevantOrbitElements: Set<DiagramElement>) : Set<DiagramElement> {
            val orbs = s.getOrbitAnchors()
            val out = currentOrbits.toMutableSet()
            orbs.forEach {
                if (relevantOrbitElements.contains(it.e)) {
                    if (it.s == Side.START) {
                        out.add(it.e)
                    } else {
                        out.remove(it.e)
                    }
                }
            }

            return out.toSet()
        }

        fun updateBlocking(s: C2Slideable, potentialBlockers: Set<C2Slideable>, alongElements: Set<DiagramElement>, currentBlockedBy: Set<DiagramElement>, allCrossableParentElements: Set<DiagramElement>) : Set<DiagramElement> {
            if (potentialBlockers.contains(s)) {
                val a = s.getRectAnchors()
                if (a.isEmpty()) {
                    return currentBlockedBy
                } else {
                    val newBlockedBy = currentBlockedBy.toMutableSet()
                    a.forEach {
                        if (it.s == Side.START) {
                            if (alongElements.contains(it.e)) {
                                // you shouldn't be able to move inside your own intersection
                                newBlockedBy.add(it.e)
                            } else if (it.canCross(d, allCrossableParentElements)) {
                                // do nothing
                            } else if (it.e is Diagram) {
                                // do nothing
                            } else {
                                newBlockedBy.add(it.e)
                            }
                        } else {
                            newBlockedBy.remove(it.e)
                        }
                    }
                    return newBlockedBy.toSet()
                }
            } else {
                return currentBlockedBy
            }
        }

        val allElements = so.getAllPositionedRectangulars()

        toDo
            .forEach { along ->
                val alongElements = (along.getIntersectingElements() + along.getOrbitingElements())
                    .filterIsInstance<Positioned>()
                    .toSet()

                val childElements =alongElements
                    .flatMap { if (it is Container) it.getContents().toSet() else emptySet() }
                    .toSet()

                val allCrossableParentElements = alongElements
                    .flatMap { getAllPositionedInHierarchy(it) }
                    .minus(alongElements)
                    .filter { it !is Diagram }
                    .toSet()

                // anything that is along the path of along
                val blockingElements = allElements
                    .minus(allCrossableParentElements)
                    .filter { e ->
                        val set = so.getSlideablesFor(e)!!
                        intersects(along, set.l, set.r) }
                    .toSet()

                // slideables for the above elements
                val relevantBlockingSlideables = blockingElements
                    .map { e -> sox.getSlideablesFor(e)!!}
                    .flatMap { listOf(it.l, it.r) }
                    .toSet()

                // tracks which slideables you need to be in the orbit of
                // in order to have neighbours
                val relevantOrbitElements = blockingElements + allCrossableParentElements + alongElements

                // elements that we might want to include a neighbour intersection with
                val allIntersectableElements = relevantOrbitElements + childElements

                val allIntersectableSlideables = sox.getAllSlideables()
                    .filter {
                        it.getOrbitingElements().intersect(allIntersectableElements).isNotEmpty() ||
                        it.getIntersectingElements().intersect(allIntersectableElements).isNotEmpty() }
                    .toSet()

                // actual intersecting elements
                val traversalOrder = (relevantBlockingSlideables + allIntersectableSlideables)
                    .sortedBy { it.minimumPosition }

                var prev : C2Slideable? = null
                var withinOrbits: Set<DiagramElement> = emptySet()
                var blockedBy : Set<DiagramElement> = emptySet()

                for(curr in traversalOrder) {
                    if (blockedBy.isEmpty() && withinOrbit(alongElements, withinOrbits) && (prev != null)) {
                        // join this element to the previous one
                        this.addNeighbour(along, prev, curr)
                        if (along.number == 112) {
                            println("Joining ${prev.number} ${curr.number}")
                        }
                    }

                    blockedBy = updateBlocking(curr, relevantBlockingSlideables, along.getIntersectingElements(), blockedBy, allCrossableParentElements)
                    withinOrbits = updateOrbits(curr, withinOrbits, relevantOrbitElements)

                    if (blockedBy.isEmpty() && (withinOrbit(alongElements, withinOrbits))) {
                        prev = curr
                    } else {
                        prev = null
                    }
                }
            }
    }


}