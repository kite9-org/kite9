/**
 *
 */
package org.kite9.diagram.common.algorithms.ssp

interface PathLocation<X : PathLocation<X>> : Comparable<X> {

    fun getLocation(): Any

    fun isActive(): Boolean

    fun setActive(b: Boolean);

}