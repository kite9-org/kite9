package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable

data class RoutableSlideableSetImpl(
    override val c: C2Slideable?,
    override val bl: C2Slideable?,
    override val br: C2Slideable?,
    override val previous: RoutableSlideableSet? = null,
    override val number: Int = C2SlackOptimisation.nextNumber(),
) : RoutableSlideableSet {

    val bs = setOfNotNull(bl, br)
    val a = setOfNotNull(c, bl, br)

    override var done = false

    override fun getAll(): Set<C2Slideable> {
        return a
    }

    override fun replace(s: C2Slideable, with: C2Slideable): RoutableSlideableSetImpl {
        done = true
        return RoutableSlideableSetImpl(
            if (c == s) with else c,
            if (bl == s) with else bl,
            if (br == s) with else br,
            this)
    }


    override fun toString(): String {
        return "RoutableSlideableSetImpl(number=$number"
                "\t c=${c},\n" +
                "\t` bs=$bs,\n" +
                "\t done=$done,\n)"
    }

    override fun replaceSide(s: C2Slideable?, side: Side): RoutableSlideableSet {
        val out = if (side == Side.START) {
            if (s != bl) {
                this.done = true
                RoutableSlideableSetImpl(this.c, s, this.br, this)
            } else {
                return this
            }
        } else {
            if (s != br) {
                this.done = true
                RoutableSlideableSetImpl(this.c, this.bl, s, this)
            } else {
                return this
            }
        }
        return out
    }
}