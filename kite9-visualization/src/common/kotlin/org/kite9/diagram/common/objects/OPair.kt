package org.kite9.diagram.common.objects

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.common.objects.OPair

/**
 * Pair, where values are ordered.  I.e. a tuple of length 2
 * @author robmoffat
 *
 * @param <X>
</X> */
data class OPair<X>(val a: X, val b: X) {

    fun oneOf(item: X): Boolean {
        return a === item || b === item
    }

    fun otherOne(`in`: Any): X? {
        return if (a === `in`) {
            b
        } else if (b === `in`) {
            a
        } else {
            throw LogicException()
        }
    }
}