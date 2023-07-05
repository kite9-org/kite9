package org.kite9.diagram.visualization.compaction

/**
 * A step in which some part of the compaction process occurs.
 *
 *
 * @author robmoffat
 */
interface CompactionStep {

    fun compact(c: Compaction, e: Embedding, rc: Compactor)
}