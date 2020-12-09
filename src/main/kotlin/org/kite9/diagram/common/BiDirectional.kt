package org.kite9.diagram.common

import org.kite9.diagram.model.position.Direction

/**
 * This generic interface allows you to specify a bi-directional, optionally directed from-to relationships, where from and to are
 * both objects of generic class X.
 *
 * @author robmoffat
 *
 * @param <X>
</X> */
interface BiDirectional<X> {

    fun getFrom(): X
    fun getTo(): X

    /**
     * Returns from, if to is the argument, or to if from is the argument.
     * @param end
     * @return
     */
    fun otherEnd(end: X): X
    fun meets(e: BiDirectional<X>?): Boolean
    fun meets(v: X): Boolean

    /**
     * Indicates the layout of from/to for the bi-directional item.  If this is non-null, then it is describing
     * the single direction it needs to flow in for the diagram.
     */
    fun getDrawDirection(): Direction?
    fun getDrawDirectionFrom(from: X): Direction?

    /**
     * Every BiDirectional should have a unique ID.
     */
    fun getID(): String

}