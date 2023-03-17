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
typealias UnorderedSet<K> = HashSet<K>

class UnorderedSetCompanion {

    /**
     * Use sparingly - provided for logging only.
     */
    fun <K> toArray(a: Set<K>): Array<Any?> {
        val r = arrayOfNulls<Any>(a.size)
        var i = 0
        val iterator: Iterator<K> = a.iterator()
        while (iterator.hasNext()) {
            val type = iterator.next()
            r[i] = type
            i++
        }
        return r
    }

    fun <K> retainAll(elements: MutableCollection<K>): Boolean {
        var modified = false
        val it = elements.iterator()
        while (it.hasNext()) {
            if (!elements.contains(it.next())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}

