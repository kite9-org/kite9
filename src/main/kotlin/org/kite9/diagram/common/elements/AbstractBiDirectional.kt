package org.kite9.diagram.common.elements

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse

abstract class AbstractBiDirectional<X> : BiDirectional<X> {

    override fun toString(): String {
        return "[" + getFrom() + "-" + getTo() + "]"
    }

    override fun otherEnd(end: X): X {
        if (end === getFrom()) return getTo()
        if (end === getTo()) return getFrom()
        throw LogicException("This is not an end: $end of $this")
    }

    override fun meets(e: BiDirectional<X>?): Boolean {
        return getFrom() == e!!.getTo() || getFrom() == e.getFrom() || getTo() == e.getTo() || getTo() == e.getFrom()
    }

    override fun meets(v: X): Boolean {
        return getFrom() == v || getTo() == v
    }

    override fun getDrawDirectionFrom(from: X): Direction? {
        if (getDrawDirection() == null) return null
        if (from == getFrom()) {
            return getDrawDirection()
        }
        if (from == getTo()) {
            return reverse(getDrawDirection())
        }
        throw RuntimeException(
            "Trying to get direction from an end that's not set: " + from
                    + " in " + this
        )
    }

    override fun hashCode(): Int {
        return getID().hashCode();
    }
}