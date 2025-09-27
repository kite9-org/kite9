package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
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

    private fun addNeighbour(along: C2Slideable?, n1: C2Slideable?, n2: C2Slideable?) {
        if ((n1 == null) || (n2==null) || (along==null)) {
            // first guard - along, n1 and n2 must be non-null
            return
        }

        if (along.getRectAnchors().isNotEmpty()) {
            // second guard - you can't route along rectangular edges
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

            fun intersectMatchesRect(o: C2Slideable, r: C2Slideable) : Boolean {
                val oe = o.getIntersectingElements()
                val re = r.getRectElements()
                return oe == re
            }

            if ((vo.c != null) && intersectMatchesRect(vo.c!!, hr.l)) {
                addNeighbour(vo.c!!, ho.bl, hr.l)
                addNeighbour(vo.c!!, ho.br, hr.r)
            }
            if ((ho.c != null) && intersectMatchesRect(ho.c!!, vr.l)) {
                addNeighbour(ho.c!!, vo.bl, vr.l)
                addNeighbour(ho.c!!, vo.br, vr.r)
            }
        }
    }

    private fun getSlideablesIncidentWith(s: C2Slideable) : Set<C2Slideable> {
        return this.neighbourDetails.filter { (k, v) -> v.firstOrNull { it.contains(s) } != null }.keys
    }

    override fun propagateIntersectionsFromRectangularToOuterRoutable(
        hi: RectangularSlideableSet,
        vi: RectangularSlideableSet,
        ho: RoutableSlideableSet,
        vo: RoutableSlideableSet
    ) {

        fun getIncident(i: C2Slideable) : Set<C2Slideable> {
            return getSlideablesIncidentWith(i).toSet()
        }

        fun isInBounds(s: C2Slideable) : Boolean {
            val out = when (s.dimension) {
                Dimension.H -> if ((ho.bl != null) && (ho.br != null)) {
                    (ho.bl!!.minimumPosition <= s.minimumPosition) && (ho.br!!.minimumPosition >= s.minimumPosition)
                } else {
                    false
                }
                Dimension.V -> if ((vo.bl != null) && (vo.br != null)) {
                    (vo.bl!!.minimumPosition <= s.minimumPosition) && (vo.br!!.minimumPosition >= s.minimumPosition)
                } else {
                    false
                }
            }

            return out
        }

        fun propagate(incident: Set<C2Slideable>, from: C2Slideable, to: C2Slideable?, e: DiagramElement) {
            if ((from != null) && (to != null)) {
                if (to.getOrbitingElements().contains(e)) {
                    incident.forEach { along ->
                        if (isInBounds(along) && isInBounds(to) && isInBounds(from)) {
                            addNeighbour(along, from, to)
                        }
                    }
                }
            }
        }

        val hlSet = getIncident(hi.l)
        val hrSet = getIncident(hi.r)
        val vlSet = getIncident(vi.l)
        val vrSet = getIncident(vi.r)
        val e = hi.e

        propagate(hlSet, hi.l, ho.bl, e)
        propagate(hrSet, hi.r, ho.br,e)
        propagate(vlSet, vi.l, vo.bl,e)
        propagate(vrSet, vi.r, vo.br,e )

    }


    override fun propagateIntersectionsBetweenRoutableAndOuterRectangular(
        hi: RoutableSlideableSet,
        vi: RoutableSlideableSet,
        ho: RectangularSlideableSet,
        vo: RectangularSlideableSet,
    ) {

        fun getIncident(a: C2Slideable, b: C2Slideable?) : Set<C2Slideable> {
            return (
                    getNeighbourSetsOn(a).flatMap { it } +
                            if (b != null)
                                getNeighbourSetsOn(b).flatMap { it }
                            else emptySet()
                    ).toSet()
        }

        fun isInBounds(s: C2Slideable) : Boolean {
            val out = when (s.dimension) {
                Dimension.H -> (ho.l.minimumPosition <= s.minimumPosition) && (ho.r.minimumPosition >= s.minimumPosition)
                Dimension.V -> (vo.l.minimumPosition <= s.minimumPosition) && (vo.r.minimumPosition >= s.minimumPosition)
            }

            return out
        }

        fun propagate(incident: Set<C2Slideable>, from: C2Slideable?, to: C2Slideable?) {
            if ((from != null) && (to != null)) {
                incident.forEach { along ->
                    if (isInBounds(along) && isInBounds(to) && isInBounds(from)) {
                        addNeighbour(along, from, to)
                    }
                }
            }
        }

        val hl = getIncident(ho.l, hi.bl)
        val hr = getIncident(ho.r, hi.br)
        val vl = getIncident(vo.l, vi.bl)
        val vr = getIncident(vo.r, vi.br)

        propagate(hl,hi.bl, ho.l)
        propagate(hr, hi.br, ho.r)
        propagate(vl, vi.bl, vo.l)
        propagate(vr, vi.br, vo.r)
    }

    override fun setupRoutableIntersections(h: RoutableSlideableSet, v: RoutableSlideableSet) {
        if ((v.bl != null) && (v.br != null) && (h.bl != null) && (h.br != null)) {
            addNeighbour(v.bl!!, h.bl, h.br)
            addNeighbour(v.br!!, h.bl, h.br)
            addNeighbour(h.bl!!, v.bl, v.br)
            addNeighbour(h.br!!, v.bl, v.br)
        }
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
            val extents = oldGroups.map { calculateExtent(it) to it }.toMap()
            val newGroups = mutableMapOf<Pair<Int, Int>, Set<C2Slideable>>()
            extents.forEach { (e, ss) ->
                val overlapGroups = newGroups.filter { (k, v) -> overlaps(e, k) }
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

}