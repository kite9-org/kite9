package org.kite9.diagram.common.objects

open class Pair<X>(var a: X, var b: X) {
    override fun equals(obj: Any?): Boolean {
        return if (obj is Pair<*>) {
            val p = obj
            safeEquals(p.a, a) && safeEquals(p.b, b) ||
                    safeEquals(p.b, a) && safeEquals(p.a, b)
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

    protected open fun elementEquals(a2: Any, a3: Any): Boolean {
        return a2 == a3
    }

    override fun hashCode(): Int {
        return a.hashCode() + b.hashCode()
    }

    override fun toString(): String {
        return "[$a,$b]"
    }
}