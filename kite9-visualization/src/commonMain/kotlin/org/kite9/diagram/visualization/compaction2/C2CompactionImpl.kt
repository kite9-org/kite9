package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import kotlin.math.max
import kotlin.math.min

/**
 * Flyweight class that handles the state of the compaction as it goes along.
 * Contains lots of utility methods too.
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

    override fun addNeighbour(along: C2Slideable?, n1: C2Slideable?, n2: C2Slideable?) {
        if ((n1 == null) || (n2==null) || (along==null)) {
            // first guard - along, n1 and n2 must be non-null
            return
        }

        if ((along.dimension == n2.dimension) || (along.dimension == n1.dimension)) {
            throw LogicException("Dimensions are wrong")
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

    override fun invertNeighbours(perp: C2Slideable?) {
        if (perp == null) {
            throw LogicException("Was expecting perp to be set")
        }
        val all = neighbourDetails
            .filterValues { it.flatMap { it }.contains(perp) }
            .keys

        val superSet = neighbourDetails.getOrPut(perp) { mutableSetOf() }
        if (superSet.size == 1) {
            val newContents = all  + superSet.first()
            superSet.clear()
            superSet.add(newContents)
        }
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

    override fun copyNeighbourMap(from: C2Slideable?, to: C2Slideable?) {
        if ((from != null) && (to != null)) {
            val toIsOrbit = to.getOrbitAnchors().isNotEmpty()
            val fromIsOrbit = from.getOrbitAnchors().isNotEmpty()

            val toGroup = mutableSetOf<C2Slideable>()

            // add "to" to any neighbour set containing from
            this.neighbourDetails.entries.forEach { (k, ss) ->
                val addTo = ss.filter { it.contains(from) }
                ss.removeAll(addTo.toSet() )
                val afterAdd = addTo.map { it + to }
                ss.addAll(afterAdd)
                if (addTo.isNotEmpty()) {
                    toGroup.add(k)
                }
            }

            if (toIsOrbit && toGroup.isNotEmpty()) {
                val first = toGroup.first()
                val rest = toGroup - first
                rest.forEach { r ->
                    addNeighbour(to, first, r)
                }
            }
        }
    }

}