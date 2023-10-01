package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.position.Direction

/**
 * Superclass for all segment-based compaction methods.   The process works by converting each face into
 * a set of segments, where segments represent horizontal or vertical positions.
 *
 *
 * @author robmoffat
 */
class C2PluggableCompactor(val steps: Array<C2CompactionStep>) {

    private var sb = MergeableSlideableBuilder()

    fun compactDiagram(d: Diagram): C2Compaction {
        val horizontal = sb.buildSegmentList(d, HORIZONTAL)
        val vertical = sb.buildSegmentList(d, VERTICAL)
        val compaction = instantiateCompaction(
            d,
            horizontal,
            vertical,
        )
        compact(compaction)
        return compaction
    }


    protected fun instantiateCompaction(
        d: Diagram,
        horizontal: C2SlackOptimisation,
        vertical: C2SlackOptimisation,
    ): C2CompactionImpl {
        return C2CompactionImpl(
            d,
            horizontal,
            vertical
        )
    }

    private fun compact(c: C2Compaction) {
        for (step in steps) {
            step.compact(c)
        }
    }

    companion object {
        val VERTICAL = setOf(Direction.UP, Direction.DOWN)
        val HORIZONTAL = setOf(Direction.LEFT, Direction.RIGHT)
    }
}