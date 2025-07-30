package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction.Side
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

    private val intersections = mutableMapOf<C2Slideable, Set<C2Slideable>>()

    override fun setIntersection(s1: C2Slideable, s2: C2Slideable) {
        if (s1.dimension == s2.dimension) {
            throw LogicException("Oops")
        }

        var items = intersections.getOrElse(s1) { emptySet() } + s2
        intersections[s1] = items

        items = intersections.getOrElse(s2) { emptySet() } + s1
        intersections[s2] = items
        //println("Intersecting ${s1.number}: ${s1}\n        with ${s2.number}:  ${s2}")
    }

    override fun getIntersections(s1: C2Slideable): Set<C2Slideable>? {
        return intersections[s1]
    }

    override fun setupLeafRectangularIntersections(
        ho: RoutableSlideableSet,
        vo: RoutableSlideableSet,
        hi: RectangularSlideableSet,
        vi: RectangularSlideableSet
    ) {
        ho.c.forEach {
            setIntersection(it, vi.l)
            setIntersection(it, vi.r)
        }

        vo.c.forEach {
            setIntersection(it, hi.l)
            setIntersection(it, hi.r)
        }
    }

    override fun setupContainerRectangularIntersections(hr: RectangularSlideableSet, vr: RectangularSlideableSet) {
        setupContainerRectangularIntersections(hr)
        setupContainerRectangularIntersections(vr)
    }



    private fun propagateIntersections(from: C2Slideable?, to: C2Slideable?) {
        if ((from != null) &&  (to!= null)) {
            (intersections[from] ?: emptySet()).forEach {
                // you can't route on rectangulars outside the rectangle itself.
                // but you can route on their intersections or internal buffer slideables
                val notRectangular = it.getRectangulars().isEmpty()
                val theRectangulars = to.getRectangulars().map { it.e }
                val theOrbits = it.getOrbits().map { it.e }
                val notOrbitForTheRectangular = theOrbits.intersect(theRectangulars).isEmpty()
                if (notRectangular && notOrbitForTheRectangular) {
                    setIntersection(to, it)
                }
            }
        }
    }


    override fun propagateIntersectionsRoutableWithRectangular(
        hi: RoutableSlideableSet,
        vi: RoutableSlideableSet,
        ho: RectangularSlideableSet,
        vo: RectangularSlideableSet
    ) {
        propagateIntersections(hi.bl, ho.l)
        propagateIntersections(hi.br, ho.r)
        propagateIntersections(vi.bl, vo.l)
        propagateIntersections(vi.br, vo.r)

        propagateIntersections(ho.l, hi.bl)
        propagateIntersections(ho.r, hi.br)
        propagateIntersections(vo.l, vi.bl)
        propagateIntersections(vo.r, vi.br)
    }

    private fun setupContainerRectangularIntersections(rect: RectangularSlideableSet) {
        val sox = getSlackOptimisation(rect.l.dimension.other())
        val d = rect.d
        //println("Intersections for ${d}")
        val intersectsLeft = sox.getAllSlideables().filter { it.getIntersectionAnchors().find { anc -> (anc.e == d) && (anc.s.contains(Side.START)) } != null }
        val intersectsRight = sox.getAllSlideables().filter { it.getIntersectionAnchors().find { anc -> (anc.e == d) && (anc.s.contains(Side.END)) } != null }

        intersectsLeft.forEach {
            sox.compaction.setIntersection(rect.l, it)
        }

        intersectsRight.forEach {
            sox.compaction.setIntersection(rect.r, it)
        }
    }

//    override fun setupRoutableIntersections(a: RoutableSlideableSet, b: RoutableSlideableSet) {
//        a.getAll().forEach {
//            val buffer = it.getOrbits().isNotEmpty()
//            if (b.bl != null) {
//                val okIntersect = it.getIntersectionAnchors().filter { anc -> anc.s.contains(Side.START) }.isNotEmpty()
//                if (buffer || okIntersect) {
//                    setIntersection(it, b.bl!!)
//                }
//            }
//            if (b.br != null) {
//                val okIntersect = it.getIntersectionAnchors().filter { anc -> anc.s.contains(Side.END) }.isNotEmpty()
//                if (buffer || okIntersect) {
//                    setIntersection(it, b.br!!)
//                }
//            }
//        }
//
//        b.getAll().forEach {
//            val buffer = it.getOrbits().isNotEmpty()
//            if (a.bl != null) {
//                val okIntersect = it.getIntersectionAnchors().filter { anc -> anc.s.contains(Side.START) }.isNotEmpty()
//                if (buffer || okIntersect) {
//                    setIntersection(it, a.bl!!)
//                }
//            }
//            if (a.br != null) {
//                val okIntersect = it.getIntersectionAnchors().filter { anc -> anc.s.contains(Side.END) }.isNotEmpty()
//                if (buffer || okIntersect) {
//                    setIntersection(it, a.br!!)
//                }
//            }
//        }
//    }

    override fun setupRoutableIntersections(a: RoutableSlideableSet, b: RoutableSlideableSet) {
        a.getAll().forEach { ai ->
            b.getAll().forEach {bi ->
                if ((ai.getOrbits().isNotEmpty()) || (bi.getOrbits().isNotEmpty())) {
                    setIntersection(ai, bi)
                }
            }
        }
   }

    override fun replaceIntersections(s1: C2Slideable?, s2: C2Slideable?, sNew: C2Slideable?) {
        if (intersections[sNew] != null) {
            throw LogicException("Calling replaceIntersections Twice!")
        }

        val k1 = intersections.remove(s1) ?: emptySet()
        val k2 = intersections.remove(s2) ?: emptySet()

        if (sNew != null) {
            intersections[sNew] = k1 + k2
            // now check all values
            val keys = intersections.keys
            keys.forEach { k ->
                var vals = intersections[k]!!
                vals = vals.map {
                    if ((it == s1) || (it == s2)) {
                        sNew
                    } else {
                        it
                    }
                }.toSet()
                intersections[k] = vals
            }
        }
    }

    override fun checkConsistency() {
        verticalSegmentSlackOptimisation.checkConsistency()
        horizontalSegmentSlackOptimisation.checkConsistency()
    }

}