package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.visualization.compaction2.*

data class RoutableSlideableSetImpl(
    override val c: Set<C2Slideable>,
    override val bl: C2Slideable?,
    override val br: C2Slideable?,
    override val number: Int = C2SlackOptimisation.nextNumber()
) : RoutableSlideableSet {

    val bs = c.plus(setOfNotNull(bl, br))

    override var done = false

    override fun mergeWithGutter(after: RoutableSlideableSet, c2: C2SlackOptimisation): RoutableSlideableSet {
        val con1 = c2.getContents(this)
        val con2 = c2.getContents(after)
        val newOrbit = c2.mergeCSlideables(br, after.bl)!!
        done = true
        val newC = this.c.plus(after.c).plus(newOrbit)
        val new = RoutableSlideableSetImpl(newC, bl, after.br)
        c2.contains(new, con1.plus(con2))
        return new
    }

    override fun mergeWithOverlap(over: RoutableSlideableSet, c2: C2SlackOptimisation): RoutableSlideableSet {
        val con1 = c2.getContents(this)
        val con2 = c2.getContents(over)
        val newL = c2.mergeCSlideables(over.bl, bl)
        val newR = c2.mergeCSlideables(over.br, br)
        val newC = c2.mergeCSlideables(over.c, c)

        done = true
        val out = RoutableSlideableSetImpl(newC, newL, newR)
        c2.contains(out, con1.plus(con2))
        return out
    }

    override fun getBufferSlideables(): Set<C2Slideable> {
        return bs
    }

    override fun getAll(): Set<C2Slideable> {
        return bs
    }

    override fun replace(s: C2Slideable, with: C2Slideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            RectangularSlideableSetImpl.replaceIfPresent(c, s, with),
            if (bl == s) with else bl,
            if (br == s) with else br)
    }


    override fun toString(): String {
        return "RoutableSlideableSetImpl(number=$number"
                "\t c=${c.map { it.number }.toString()},\n" +
                "\t` bs=$bs,\n" +
                "\t done=$done,\n)"
    }
}