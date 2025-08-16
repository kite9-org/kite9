package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.anchors.OrbitAnchor
import org.kite9.diagram.visualization.compaction2.anchors.Purpose
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

data class RectangularSlideableSetImpl(
    override val e: Rectangular,
    override val l: C2Slideable,
    override val r: C2Slideable,
    override val number: Int = C2SlackOptimisation.nextNumber()) : RectangularSlideableSet {

    override var done = false

    override fun replace(s: C2Slideable, with: C2Slideable): RectangularSlideableSet {
        done = true
        return RectangularSlideableSetImpl(
            e,
            if (l == s) with else l,
            if (r == s) with else r,
        )
    }

    override fun getRectangularSlideables(): Collection<C2Slideable> {
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

    override fun wrapInRoutable(): RoutableSlideableSet? {
        val so = l.so as C2SlackOptimisation
        val existing = so.getContainer(this)
        if (existing != null) {
            return existing
        }

        if (this.e.getParent() == null) {
            return null
        }

        val bl = C2Slideable(so, l.dimension, setOf(OrbitAnchor(e, Side.START)).toMutableSet())
        val br = C2Slideable(so, l.dimension, setOf(OrbitAnchor(e, Side.END)).toMutableSet())

        var margin = AbstractC2CompactionStep.getMargin(l.dimension, e)

        val intersections = setOf(C2Slideable(so, l.dimension, e, Purpose.GLYPH_LAYOUT_MIDPOINT))
        val size = l.minimumDistanceTo(r)

        so.ensureMinimumDistance(bl, l, margin.first / 2)
        so.ensureMinimumDistance(r, br, margin.second / 2)
        intersections.forEach {
            so.ensureMinimumDistance(l, it, size / 2)
            so.ensureMinimumDistance(it, r, size / 2)
        }

        val out = RoutableSlideableSetImpl(
            intersections,
            bl,
            br)

        return out
    }

    override fun toString(): String {
        return "RectangularSlideableSetImpl(number=$number" +
                "\t d=$e,\n" +
                "\t l=${l.number},\n" +
                "\t r=${r.number},\n" +
                "\t done=$done)"
    }


}

