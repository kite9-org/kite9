package org.kite9.diagram.common.objects


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

    val all: Collection<X>
        get() {
            return listOf(a, b, c, d)
        }
}