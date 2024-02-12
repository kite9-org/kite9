package org.kite9.diagram.visualization.compaction2.sets;

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.compaction2.*

data class RoutableSlideableSetImpl(val bs: Set<C2BufferSlideable>,
                                    override val c: C2BufferSlideable?,
                                    override val bl: C2OrbitSlideable,
                                    override val br: C2OrbitSlideable,
) : RoutableSlideableSet {

    constructor(c: C2IntersectionSlideable?, bl: C2OrbitSlideable, br: C2OrbitSlideable,) : this(setOfNotNull(c, bl, br), c, bl, br)

    override var done = false

    override fun mergeWithGutter(after: RoutableSlideableSet, c2: C2SlackOptimisation): RoutableSlideableSet {
        val newOrbit = c2.mergeSlideables(br, after.bl)
        val allBs =
            setOf(bs, after.getBufferSlideables()).asSequence().flatten().minus(br).minus(after.bl).plus(newOrbit)
                .toSet()
        done = true
        val new = RoutableSlideableSetImpl(allBs, newOrbit, bl, after.br)
        c2.contains(new, c2.getContents(this).plus(c2.getContents(after)))
        c2.compaction.replaceJunction(br, after.bl, newOrbit)
        return new
    }

    override fun mergeWithOverlap(over: RoutableSlideableSet, c2: C2SlackOptimisation): RoutableSlideableSet {
        val newL = c2.mergeSlideables(over.bl, bl)
        val newR = c2.mergeSlideables(over.br, br)
        done = true
        val out = RoutableSlideableSetImpl(setOfNotNull(newL, newR, c, over.c), null, newL, newR)
        c2.contains(out, c2.getContents(this).plus(c2.getContents(over)))
        c2.compaction.replaceJunction(br, over.br, newR)
        c2.compaction.replaceJunction(bl, over.bl, newL)
        return out
    }

    override fun getBufferSlideables(): Set<C2BufferSlideable> {
        return bs
    }

    override fun getAll(): Set<C2BufferSlideable> {
        return bs
    }

    override fun replaceRectangular(s: C2RectangularSlideable, with: C2RectangularSlideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            bs,
            c,
            bl,
            br)
    }

    override fun replaceOrbit(s: C2OrbitSlideable, with: C2OrbitSlideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            RectangularSlideableSetImpl.replaceIfPresent(bs, s, with),
            c,
            if (bl == s) with else bl,
            if (br == s) with else br)
    }

    override fun replaceIntersection(s: C2IntersectionSlideable, with: C2IntersectionSlideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            RectangularSlideableSetImpl.replaceIfPresent(bs, s, with),
            if (c == s) with else c,
            bl,
            br)
    }

    override val number = C2SlackOptimisation.nextNumber()

}