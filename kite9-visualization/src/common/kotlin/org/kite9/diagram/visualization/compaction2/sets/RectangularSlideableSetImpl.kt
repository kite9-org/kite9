package org.kite9.diagram.visualization.compaction2.sets;

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*

data class RectangularSlideableSetImpl(
    override val d: Rectangular,
    override val l: C2RectangularSlideable,
    override val r: C2RectangularSlideable,
    override val c: C2IntersectionSlideable?) : RectangularSlideableSet {

    override var done = false

    override fun replaceRectangular(s: C2RectangularSlideable, with: C2RectangularSlideable): RectangularSlideableSet {
        done = true
        return RectangularSlideableSetImpl(
            d,
            if (l == s) with else l,
            if (r == s) with else r,
            c
        )
    }

    override fun replaceIntersection(s: C2IntersectionSlideable, with: C2IntersectionSlideable): RectangularSlideableSet {
        done = true
        return RectangularSlideableSetImpl(
            d,
            if (l == s) with else l,
            if (r == s) with else r,
            if (c == s) with else c
        )
    }

    override fun getRectangularSlideables(): Collection<C2RectangularSlideable> {
        return setOf(l, r)
    }

    override fun getAll(): Set<C2Slideable> {
        return setOfNotNull(l, r, c)
    }

    companion object {
        fun <X> replaceIfPresent(s: Set<X>, o: X, n: X): Set<X> {
            return if (s.contains(o)) {
                s.minus(o).plus(n)
            } else {
                s
            }
        }
    }

    override fun wrapInRoutable(so: C2SlackOptimisation): RoutableSlideableSet {
        val bl = C2OrbitSlideable(so, l.dimension, setOf(RectAnchor(d, Side.START)))
        val br = C2OrbitSlideable(so, l.dimension, setOf(RectAnchor(d, Side.END)))

        so.ensureMinimumDistance(bl, l, 0)
        so.ensureMinimumDistance(r, br, 0)

        return RoutableSlideableSetImpl(
            setOfNotNull(bl, c, br),
            c,
            bl,
            br)
    }

    override val number = C2SlackOptimisation.nextNumber()
}

