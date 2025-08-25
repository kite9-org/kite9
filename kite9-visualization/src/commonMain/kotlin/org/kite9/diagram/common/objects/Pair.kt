package org.kite9.diagram.common.objects

open class Pair<X>(val a: X, val b: X) {
    override fun equals(other: Any?): Boolean {
        return if (other is Pair<*>) {
            safeEquals(other.a, a) && safeEquals(other.b, b) ||
                    safeEquals(other.b, a) && safeEquals(other.a, b)
        } else {
            false
        }
    }

    private fun safeEquals(a2: Any?, a3: Any?): Boolean {
        if (a2 === a3) {
            return true
        }
        return if (a2 == null || a3 == null) {
            false
        } else elementEquals(a2, a3)
    }

    open fun elementEquals(a2: Any, a3: Any): Boolean {
        return a2 == a3
    }

    override fun hashCode(): Int {
        return a.hashCode() + b.hashCode()
    }

    override fun toString(): String {
        return "[$a,$b]"
    }
}
