package org.kite9.diagram.common

import java.util.HashMap

/**
 * @deprecated
 */
class HintMap : HashMap<String?, Float?> {
    constructor() : super() {}
    constructor(initialCapacity: Int, loadFactor: Float) : super(initialCapacity, loadFactor) {}
    constructor(initialCapacity: Int) : super(initialCapacity) {}
    constructor(m: Map<out String?, Float?>?) : super(m) {}

    companion object {
        private const val serialVersionUID = 8622279690962224111L
    }
}