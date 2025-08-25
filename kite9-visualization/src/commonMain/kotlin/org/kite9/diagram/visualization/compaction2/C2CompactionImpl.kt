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

    private val intersections = mutableMapOf<C2Slideable, Map<C2Slideable, IntersectionType>>()

    private fun setIntersection(s1: C2Slideable, s2: C2Slideable, type: IntersectionType) {
        if (s1.dimension == s2.dimension) {
            throw LogicException("Oops")
        }

        var items = intersections.getOrElse(s1) { mutableMapOf() } + Pair(s2,type)
        intersections[s1] = items

        items = intersections.getOrElse(s2) { emptyMap() } + Pair(s1, type)
        intersections[s2] = items
        //println("Intersecting ${s1.number}: ${s1}\n        with ${s2.number}:  ${s2}")
    }

    override fun getIntersections(s1: C2Slideable): Set<C2Slideable> {
        return intersections[s1]?.keys ?: emptySet()
    }

    override fun getTypedIntersections(s1: C2Slideable): Map<C2Slideable, IntersectionType> {
        return intersections[s1] ?: emptyMap()
    }

    override fun setupRectangularIntersections(hr: RectangularSlideableSet, vr: RectangularSlideableSet) {
        setupContainerRectangularIntersections(hr)
        setupContainerRectangularIntersections(vr)
    }

    private fun propagateAllIntersections(from: C2Slideable?, to: C2Slideable?) {
        if ((from != null) &&  (to!= null)) {
            val toPropagate = intersections[from] ?: emptyMap()
            toPropagate.forEach { (slideable, _) ->
                // you can't route on rectangulars outside the rectangle itself.
                // but you can route on their intersections or internal buffer slideables
                val notRectangular = slideable.getRectAnchors().isEmpty()
                val theRectangulars = to.getRectAnchors().map { it.e }
                val theOrbits = slideable.getOrbitAnchors().map { it.e }
                val notOrbitForTheRectangular = theOrbits.intersect(theRectangulars.toSet()).isEmpty()
                if (notRectangular && notOrbitForTheRectangular) {
                    setIntersection(to, slideable, IntersectionType.PROPAGATED)
                }
            }
        }
    }

    private fun propagateElementIntersections(from: C2Slideable?, to: C2Slideable?) {
        if ((from != null) &&  (to!= null)) {
            val toPropagate = intersections[from] ?: emptyMap()
            toPropagate.forEach { (slideable, _) ->
                // you can't route on rectangulars outside the rectangle itself.
                // but you can route on their intersections or internal buffer slideables
                val notRectangular = slideable.getRectAnchors().isEmpty()
                val inElement = to.inElementIntersection(slideable) != null
                if (notRectangular && inElement) {
                    setIntersection(to, slideable, IntersectionType.PROPAGATED)
                }
            }
        }
    }

    override fun propagateIntersectionsFromRectangularToRoutable(
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
    override fun propagateIntersectionsFromRoutableToRectangular(
        hi: RoutableSlideableSet,
        vi: RoutableSlideableSet,
        ho: RectangularSlideableSet,
        vo: RectangularSlideableSet
    ) {
        propagateElementIntersections(hi.bl, ho.l)
        propagateElementIntersections(hi.br, ho.r)
        propagateElementIntersections(vi.bl, vo.l)
        propagateElementIntersections(vi.br, vo.r)
    }

    private fun setupContainerRectangularIntersections(rect: RectangularSlideableSet) {
        val sox = getSlackOptimisation(rect.l.dimension.other())
        val d = rect.e
        //println("Intersections for ${d}")
        val intersects = sox.getAllSlideables().filter { it.getIntersectAnchors().find { anc -> anc.e == d } != null }

        intersects.forEach {
            sox.compaction.setIntersection(rect.l, it, IntersectionType.INTERSECT)
            sox.compaction.setIntersection(rect.r, it, IntersectionType.INTERSECT)
        }
    }

    override fun setupRoutableIntersections(h: RoutableSlideableSet, v: RoutableSlideableSet) {
        h.getAll().forEach { ai ->
            v.getAll().forEach { bi ->
                if ((ai.getOrbitAnchors().isNotEmpty()) || (bi.getOrbitAnchors().isNotEmpty())) {
                    setIntersection(ai, bi, IntersectionType.BUFFER)
                }
            }
        }
   }

    override fun replaceIntersections(s1: C2Slideable?, s2: C2Slideable?, sNew: C2Slideable?) {
        if (intersections[sNew] != null) {
            throw LogicException("Calling replaceIntersections Twice!")
        }

        val k1 = intersections.remove(s1) ?: mutableMapOf()
        val k2 = intersections.remove(s2) ?: mutableMapOf()

        if (sNew != null) {
            intersections[sNew] = k1 + k2
            // now check all values
            val keys = intersections.keys
            keys.forEach { k ->
                var vals = intersections[k]!!
                vals = vals.map { (k,v) ->
                    if ((k == s1) || (k == s2)) {
                        Pair(sNew, v)
                    } else {
                        Pair(k ,v)
                    }
                }.toMap()
                intersections[k] = vals
            }
        }
    }

    override fun checkConsistency() {
        verticalSegmentSlackOptimisation.checkConsistency()
        horizontalSegmentSlackOptimisation.checkConsistency()
    }

}