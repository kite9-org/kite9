package org.kite9.diagram.common.algorithms.det


/**
 * This prevents any attempts to order the contents, as the order will always
 * be non-deterministic if we use objects that don't declare hashCode.
 *
 * Use this in preference to HashSet, unless ordering is required, and in which case use
 * LinkedHashSet or DetHashSet.
 *
 * @author robmoffat
 */
class UnorderedSet<K> : HashSet<K> {

    constructor() : super() {}
    constructor(c: Collection<K>) : super(c) {}
    constructor(initialCapacity: Int) : super(initialCapacity) {}


    /**
     * Use sparingly - provided for logging only.
     */
    override fun toArray(): Array<Any?> {
        val r = arrayOfNulls<Any>(size)
        var i = 0
        val iterator: Iterator<K> = super.iterator()
        while (iterator.hasNext()) {
            val type = iterator.next()
            r[i] = type
            i++
        }
        return r
    }

    override fun retainAll(elements: Collection<K>): Boolean {
        var modified = false
        val it = super.iterator()
        while (it.hasNext()) {
            if (!elements.contains(it.next())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}