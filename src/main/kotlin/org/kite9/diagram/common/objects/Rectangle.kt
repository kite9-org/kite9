package org.kite9.diagram.common.objects

import java.util.ArrayList

/**
 * Like a pair, but contains four items.
 * @author robmoffat
 *
 * @param <X>
</X> */
data class Rectangle<X>(val a: X, val b: X, val c: X, val d: X) {
    override fun toString(): String {
        return a.toString() + " " + b + " " + c + " " + d
    }

    val all: Collection<X?>
        get() {
            val out = ArrayList<X?>(4)
            out.add(a)
            out.add(b)
            out.add(c)
            out.add(d)
            return out
        }
}