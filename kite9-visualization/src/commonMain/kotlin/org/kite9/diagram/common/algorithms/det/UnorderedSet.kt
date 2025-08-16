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

