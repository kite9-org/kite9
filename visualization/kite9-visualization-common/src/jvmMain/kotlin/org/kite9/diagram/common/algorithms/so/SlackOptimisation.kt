package org.kite9.diagram.common.algorithms.so

import org.kite9.diagram.logging.Kite9Log

interface SlackOptimisation {

    val log: Kite9Log
    var pushCount: Int

    fun getSize(): Int
    fun getAllSlideables(): Collection<Slideable>
    fun ensureMinimumDistance(left: Slideable, right: Slideable, minLength: Int)
    fun ensureMaximumDistance(left: Slideable, right: Slideable, maxLength: Int)
    fun updateMaps(s: Slideable)
}