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

    private val neighbourDetails = mutableMapOf<C2Slideable, MutableSet<Set<C2Slideable>>>()

    private fun addNeighbour(along: C2Slideable, n1: C2Slideable?, n2: C2Slideable?) {
        if (along.getRectAnchors().isNotEmpty()) {
            // you can't route along rectangular edges
            return
        }
        if ((n1 == null) || (n2==null)) {
            return
        }
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

    override fun setupRectangularIntersections(hr: RectangularSlideableSet, vr: RectangularSlideableSet, ho: RoutableSlideableSet, vo: RoutableSlideableSet) {
        setupRectangularIntersectionsInner(hr, vr, ho, vo)
    }

    fun setupRectangularIntersectionsInner(hr: RectangularSlideableSet?, vr: RectangularSlideableSet?, ho: RoutableSlideableSet, vo: RoutableSlideableSet) {
        if (vo.bl != null) {
            addNeighbour(vo.bl!!, ho.bl, ho.c)
            addNeighbour(vo.bl!!, ho.c, ho.br)
        }

        if (vo.br != null) {
            addNeighbour(vo.br!!, ho.bl, ho.c)
            addNeighbour(vo.br!!, ho.c, ho.br)
        }

        if (ho.bl != null) {
            addNeighbour(ho.bl!!, vo.bl, vo.c)
            addNeighbour(ho.bl!!, vo.c, vo.br)
        }

        if (ho.br != null) {
            addNeighbour(ho.br!!, vo.bl, vo.c)
            addNeighbour(ho.br!!, vo.c, vo.br)
        }

        if ((vr != null) && (hr != null)) {
            if (vo.c != null) {
                addNeighbour(vo.c!!, ho.bl, hr.l)
                addNeighbour(vo.c!!, ho.br, hr.r)
            }
            if (ho.c != null) {
                addNeighbour(ho.c!!, vo.bl, vr.l)
                addNeighbour(ho.c!!, vo.br, vr.r)
            }
        }
    }

    private fun propagateAllIntersections(from: C2Slideable?, to: C2Slideable?) {
        if ((from != null) && (to != null)) {
            // first, we're going to propagate all neighbour sets between the two

            val fromSets = getNeighbourSetsOn(from)
            val toSets = getNeighbourSetsOn(to)
            val fromIncident = getSlideablesIncidentWith(from)
            val toIncident = getSlideablesIncidentWith(to)
            val newSuper = fromSets.flatMap { it } + toSets.flatMap { it } + fromIncident + toIncident

            // next, let's find all the transverse sets and do those
            newSuper.forEach { along -> addNeighbour(along, from, to) }
        }
    }

    private fun getSlideablesIncidentWith(s: C2Slideable) : Set<C2Slideable> {
        return this.neighbourDetails.filter { (k, v) -> v.firstOrNull { it.contains(s) } != null }.keys
    }
    /**
     * One-way propagation
     **/
    private fun propagateElementIntersections(from: C2Slideable?, to: C2Slideable?) {
        if ((from != null) && (to != null)) {
            // first, we're going to propagate all neighbour sets between the two

            val fromSets = getNeighbourSetsOn(from)
            val toSets = getNeighbourSetsOn(to)

            val newSuper = fromSets.flatMap { it } + toSets.flatMap { it }

            // next, let's find all the transverse sets and do those
            newSuper.forEach { along -> addNeighbour(along, from, to) }
        }
    }

    override fun propagateIntersectionsFromRectangularToOuterRoutable(
        hi: RoutableSlideableSet,
        vi: RoutableSlideableSet,
        ho: RectangularSlideableSet,
        vo: RectangularSlideableSet
    ) {
        propagateAllIntersections(ho.l, hi.bl)
        propagateAllIntersections(ho.r, hi.br)
        propagateAllIntersections(vo.l, vi.bl)
        propagateAllIntersections(vo.r, vi.br)
    }
    override fun propagateIntersectionsBetweenRoutableAndOuterRectangular(
        hi: RoutableSlideableSet,
        vi: RoutableSlideableSet,
        ho: RectangularSlideableSet,
        vo: RectangularSlideableSet,
    ) {
        propagateElementIntersections(hi.bl, ho.l)
        propagateElementIntersections(hi.br, ho.r)
        propagateElementIntersections(vi.bl, vo.l)
        propagateElementIntersections(vi.br, vo.r)
    }

    override fun setupRoutableIntersections(h: RoutableSlideableSet, v: RoutableSlideableSet) {
        setupRectangularIntersectionsInner(null, null, h, v)
   }

    override fun replaceIntersections(s1: C2Slideable, s2: C2Slideable, sNew: C2Slideable) {

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
        }

        println("Consistency ${neighbourDetails.size} ${neighbourDetails.size}")
        println("Consistency ${neighbourDetails.values.sumOf { it.size }}")
    }

    override fun getNeighbourSetsOn(s: C2Slideable) : Set<Set<C2Slideable>> {
        val superSet = neighbourDetails.getOrPut(s) { mutableSetOf() }
        return superSet
    }

    override fun getLocationsOn(s: C2Slideable): Set<C2Slideable> {
        return getNeighbourSetsOn(s).flatMap { it }.toSet()
    }

}