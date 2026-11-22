package org.kite9.diagram.model.position

/**
 * This is compatible with the awt dimension class, which is used for a
 * lot of rendering.
 *
 * This has double precision though and has internal scaling operations, as well
 * as actions to allow you to apply operations to a specific direction.
 *
 * *Immutable*
 *
 * @author robmoffat
 */
data class BasicDimension2D(override val w: Double, override val h: Double) : Dimension2D {

    override fun height(): Double {
        return h
    }

    override fun width(): Double {
        return w
    }

    override fun size(): Dimension2D {
        return this
    }
}