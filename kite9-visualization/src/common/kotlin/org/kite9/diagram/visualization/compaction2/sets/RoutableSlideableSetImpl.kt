package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.visualization.compaction2.*

data class RoutableSlideableSetImpl(override val c: Set<C2BufferSlideable>,
                                    override val bl: C2OrbitSlideable?,
                                    override val br: C2OrbitSlideable?,
) : RoutableSlideableSet {

    val bs = c.plus(setOfNotNull(bl, br))

    constructor(c: C2IntersectionSlideable?, bl: C2OrbitSlideable?, br: C2OrbitSlideable?) : this(setOfNotNull(c), bl, br)

    override var done = false

    override fun mergeWithGutter(after: RoutableSlideableSet, c2: C2SlackOptimisation): RoutableSlideableSet {
        val newOrbit = c2.mergeSlideables(br, after.bl)!!
        after.br?.addForeignOrbits(newOrbit.getOrbits())
        bl?.addForeignOrbits(newOrbit.getOrbits())
        done = true
        val newC = this.c.plus(after.c).plus(newOrbit)
        val new = RoutableSlideableSetImpl(newC, bl, after.br)
        c2.contains(new, c2.getContents(this).plus(c2.getContents(after)))
        return new
    }

    override fun mergeWithOverlap(over: RoutableSlideableSet, c2: C2SlackOptimisation): RoutableSlideableSet {
        val newL = c2.mergeSlideables(over.bl, bl)
        val newR = c2.mergeSlideables(over.br, br)
        val newC = c2.mergeSlideables(over.c, c)
        done = true
        val out = RoutableSlideableSetImpl(newC, newL, newR)
        c2.contains(out, c2.getContents(this).plus(c2.getContents(over)))
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
            c,
            bl,
            br)
    }

    override fun replaceOrbit(s: C2OrbitSlideable, with: C2OrbitSlideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            c,
            if (bl == s) with else bl,
            if (br == s) with else br)
    }

    override fun replaceIntersection(s: C2IntersectionSlideable, with: C2IntersectionSlideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            RectangularSlideableSetImpl.replaceIfPresent(c, s, with),
            bl,
            br)
    }

    override fun toString(): String {
        return "RoutableSlideableSetImpl(c=$c,\n" +
                "\t` bs=$bs,\n" +
                "\t done=$done,\n" +
                "\t number=$number)"
    }

    override val number = C2SlackOptimisation.nextNumber()

}