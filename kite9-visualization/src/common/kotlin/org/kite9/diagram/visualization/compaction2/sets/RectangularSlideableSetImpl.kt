package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

data class RectangularSlideableSetImpl(
    override val d: Rectangular,
    override val l: C2RectangularSlideable,
    override val r: C2RectangularSlideable) : RectangularSlideableSet {

    override var done = false

    override fun replaceRectangular(s: C2RectangularSlideable, with: C2RectangularSlideable): RectangularSlideableSet {
        done = true
        return RectangularSlideableSetImpl(
            d,
            if (l == s) with else l,
            if (r == s) with else r,
        )
    }

    override fun getRectangularSlideables(): Collection<C2RectangularSlideable> {
        return setOf(l, r)
    }

    override fun getAll(): Set<C2Slideable> {
        return setOf(l, r)
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

    override fun wrapInRoutable(so: C2SlackOptimisation, g: LeafGroup?): RoutableSlideableSet {
        val bl = C2OrbitSlideable(so, l.dimension, setOf(RectAnchor(d, Side.START)))
        val br = C2OrbitSlideable(so, l.dimension, setOf(RectAnchor(d, Side.END)))
        val c = if (d.getParent() != null) {
            C2IntersectionSlideable(so, l.dimension, g, setOf(d))
        } else {
            null
        }

        val size = l.minimumDistanceTo(r)

        so.ensureMinimumDistance(bl, l, 0)
        so.ensureMinimumDistance(r, br, 0)
        if (c!= null) {
            so.ensureMinimumDistance(l, c, size / 2)
            so.ensureMinimumDistance(c, r, size / 2)
        }


        val out = RoutableSlideableSetImpl(
            setOfNotNull(c),
            bl,
            br)

        so.add(g, out)
        so.compaction.propagateIntersections(this, out)
        return out
    }

    override fun toString(): String {
        return "RectangularSlideableSetImpl(d=$d,\n" +
                "\t l=$l,\n" +
                "\t r=$r,\n" +
                "\t done=$done,\n" +
                "\t number=$number)"
    }

    override val number = C2SlackOptimisation.nextNumber()


}

