package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet

data class NullableLocation(val first: C2Slideable?, val second: C2Slideable?) {

    fun toLocation() : Location {
        if (first == null || second == null) throw LogicException("Both should be non-null")
        if (first.dimension == second.dimension) throw LogicException("Should be different dimensions!")

        return if (first.dimension == Dimension.H) {
            Location(first!!, second!!)
        } else {
            Location(second!!, first!!)
        }
    }
}

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

    private val neighbourDetailsH = mutableMapOf<Location, MutableSet<C2Slideable>>()
    private val neighbourDetailsV = mutableMapOf<Location, MutableSet<C2Slideable>>()

    override fun getLocationsOn(s: C2Slideable): Set<C2Slideable> {
        return if (s.dimension == Dimension.H) {
            neighbourDetailsH.filter { it.key.first == s }.keys.map { it.second }.toSet()
        } else {
            neighbourDetailsV.filter { it.key.second == s }.keys.map { it.first }.toSet()
        }
    }

    private fun addNeighbour(l1: Location, l2: Location) {
        if ((l1.first.dimension != Dimension.H) || (l2.first.dimension != Dimension.H)){
            throw LogicException("First of pair should be horizontal")
        }

        if ((l1.second.dimension != Dimension.V) || (l2.second.dimension != Dimension.V)) {
            throw LogicException("Second of pair should be vertical")
        }

        if ((l1.first != l2.first) && (l2.second != l2.second)) {
            throw LogicException("Oops: going diagonal!")
        } else if (l1 == l2) {
            throw LogicException("Oops: going to the same place!")
        } else if (l1.first != l2.first) {
            val n1 = neighbourDetailsH.getOrPut(l1) { mutableSetOf() }
            val n2 = neighbourDetailsH.getOrPut(l2) { mutableSetOf() }
            n1.add(l2.first)
            n2.add(l1.first)
        } else {
            val n1 = neighbourDetailsV.getOrPut(l1) { mutableSetOf() }
            val n2 = neighbourDetailsV.getOrPut(l2) { mutableSetOf() }
            n1.add(l2.second)
            n2.add(l1.second)
        }
    }

    private fun addNullableNeighbour(nl1: NullableLocation, nl2: NullableLocation) {
        if ((nl1.first != null) && (nl1.second != null)) {
            if ((nl2.first != null) && (nl2.second != null)) {
                addNeighbour(nl1.toLocation(), nl2.toLocation())
            }
        }
    }


    override fun getNeighbours(from: Location, d: Dimension) : Set<C2Slideable> {
        return if (d == Dimension.H) {
            neighbourDetailsH[from] ?: emptySet()
        } else {
            neighbourDetailsV[from] ?: emptySet()
        }.toSet()
    }

    override fun getLocations() : Set<Location> {
        return (neighbourDetailsH.keys + neighbourDetailsV.keys)
    }



    override fun setupRectangularIntersections(hr: RectangularSlideableSet, vr: RectangularSlideableSet, ho: RoutableSlideableSet, vo: RoutableSlideableSet) {
        val tl = NullableLocation(ho.bl, vo.bl)
        val to = NullableLocation(ho.c, vo.bl)
        val tr = NullableLocation(ho.br, vo.bl)

        addNullableNeighbour(tl, to)
        addNullableNeighbour(to, tr)

        val bl = NullableLocation(ho.bl, vo.br)
        val bo = NullableLocation(ho.c, vo.br)
        val br = NullableLocation(ho.br, vo.br)

        addNullableNeighbour(bl, bo)
        addNullableNeighbour(bo, br)

        val lo = NullableLocation(ho.bl, vo.c)
        val ro = NullableLocation(ho.br, vo.c)

        addNullableNeighbour(tl, lo)
        addNullableNeighbour(tr, ro)
        addNullableNeighbour(bl, lo)
        addNullableNeighbour(br, ro)

        val ti = NullableLocation(ho.c, vr.l)
        val bi = NullableLocation(ho.c, vr.r)
        val li = NullableLocation(hr.l, vo.c)
        val ri = NullableLocation(hr.r, vo.c)

        addNullableNeighbour(ti, to)
        addNullableNeighbour(bi, bo)
        addNullableNeighbour(ri, ro)
        addNullableNeighbour(li, lo)
    }

    private fun propagateAllIntersections(from: C2Slideable?, to: C2Slideable?) {
        if ((from != null) && (to != null)) {
            getLocations().forEach { l ->
                if (l.first == from) {
                    val l2 = Location(to, l.second)
                    addNeighbour(l, l2)
                } else if (l.second == from) {
                    val l2 = Location(l.first, to)
                    addNeighbour(l, l2)
                }
            }
        }
    }

//    private fun propagateElementIntersections(from: C2Slideable?, to: C2Slideable?) {
//        if ((from != null) &&  (to!= null)) {
//            val toPropagate1 = intersections[from] ?: emptyMap()
//            val toPropagate2 = intersections[to] ?: emptyMap()
//            toPropagate1.forEach { (slideable, _) ->
//                // you can't route on rectangulars outside the rectangle itself.
//                // but you can route on their intersections or internal buffer slideables
//                val notRectangular = slideable.getRectAnchors().isEmpty()
//                if (notRectangular) {
//                    setIntersection(to, slideable, IntersectionType.PROPAGATED_OUTWARD)
//                }
//            }
//
//            toPropagate2.forEach { (slideable, _) ->
//                setIntersection(from, slideable, IntersectionType.PROPAGATED_INWARD)
//            }
//        }
//    }

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
//        propagateElementIntersections(hi.bl, ho.l)
//        propagateElementIntersections(hi.br, ho.r)
//        propagateElementIntersections(vi.bl, vo.l)
//        propagateElementIntersections(vi.br, vo.r)
        propagateAllIntersections(ho.l, hi.bl)
        propagateAllIntersections(ho.r, hi.br)
        propagateAllIntersections(vo.l, vi.bl)
        propagateAllIntersections(vo.r, vi.br)
    }

    override fun setupRoutableIntersections(ho: RoutableSlideableSet, vo: RoutableSlideableSet) {
        val tl = NullableLocation(ho.bl, vo.bl)
        val to = NullableLocation(ho.c, vo.bl)
        val tr = NullableLocation(ho.br, vo.bl)

        addNullableNeighbour(tl, to)
        addNullableNeighbour(to, tr)

        val bl = NullableLocation(ho.bl, vo.br)
        val bo = NullableLocation(ho.c, vo.br)
        val br = NullableLocation(ho.br, vo.br)

        addNullableNeighbour(bl, bo)
        addNullableNeighbour(bo, br)

        val lo = NullableLocation(ho.bl, vo.c)
        val ro = NullableLocation(ho.br, vo.c)

        addNullableNeighbour(tl, lo)
        addNullableNeighbour(tr, ro)
        addNullableNeighbour(bl, lo)
        addNullableNeighbour(br, ro)
   }

    override fun replaceIntersections(s1: C2Slideable, s2: C2Slideable, sNew: C2Slideable) {
        val locationsCopy = getLocations()
        locationsCopy.forEach { l ->
            // horizontal axis
            val nh = neighbourDetailsH[l]
            val nh2 = nh?.map {
                if ((it == s1) || (it == s2)) sNew else it
            }?.toMutableSet()
            if (nh2 != null) {
                neighbourDetailsH[l] = nh2
            }

            // vertical axis
            val nv = neighbourDetailsV[l]
            val nv2 = nv?.map {
                if ((it == s1) || (it == s2)) sNew else it
            }?.toMutableSet()
            if (nv2 != null) {
                neighbourDetailsV[l] = nv2
            }

            // now do keys
            val newH = if ((l.first == s1) || (l.first == s2)) sNew else l.first
            val newV = if ((l.second == s1) || (l.second == s2)) sNew else l.second
            val newL = Location(newH, newV)

            if (l != newL) {
                val hd = neighbourDetailsH.remove(l)
                if (hd != null) {
                    neighbourDetailsH[newL] = hd
                }

                val vd = neighbourDetailsV.remove(l)
                if (vd != null) {
                    neighbourDetailsV[newL] = vd
                }
            }
        }
    }

    override fun checkConsistency() {
        verticalSegmentSlackOptimisation.checkConsistency()
        horizontalSegmentSlackOptimisation.checkConsistency()

        neighbourDetailsH.forEach { (l, n) ->
            if (l.first.isDone() || l.second.isDone()) {
                throw LogicException("Done slideable")
            }

            n.forEach {
                if (it.isDone()) {
                    throw LogicException("Done slideable")
                }
            }
        }

        neighbourDetailsV.forEach { (l, n) ->
            if (l.first.isDone() || l.second.isDone()) {
                throw LogicException("Done slideable")
            }

            n.forEach {
                if (it.isDone()) {
                    throw LogicException("Done slideable")
                }
            }
        }
    }

}