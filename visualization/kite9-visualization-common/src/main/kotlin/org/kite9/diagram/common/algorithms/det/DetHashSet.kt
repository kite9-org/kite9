package org.kite9.diagram.common.algorithms.det

import kotlin.collections.HashSet

class DetHashSet<K : Deterministic> : HashSet<K> {

    constructor() : super() {}
    constructor(c: Collection<K>) : super(c) {}
    constructor(initialCapacity: Int) : super(initialCapacity) {}
}