package org.kite9.diagram.visualization.compaction2

/**
 * A step in which some part of the compaction process occurs.
 *
 *
 * @author robmoffat
 */
interface C2CompactionStep {

    fun compact(c: C2Compaction)
}