package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Superclass for all segment-based compaction methods.   The process works by converting each face into
 * a set of segments, where segments represent horizontal or vertical positions.
 *
 *
 * @author robmoffat
 */
class C2PluggableCompactor(val steps: Array<C2CompactionStep>) {


    fun compactDiagram(d: Diagram, gr: GroupResult): C2Compaction {
        val horizontal = C2SlackOptimisation(d)
        val vertical = C2SlackOptimisation(d)
        val compaction = instantiateCompaction(
            d,
            horizontal,
            vertical,
        )
        compact(compaction, gr.groups().first())
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

    private fun compact(c: C2Compaction, g: Group) {
        if (g is CompoundGroup) {
            compact(c, g.a)
            compact(c, g.b)
        }

        for (step in steps) {
            step.compact(c, g)
        }
    }

    companion object {
        val VERTICAL = setOf(Direction.UP, Direction.DOWN)
        val HORIZONTAL = setOf(Direction.LEFT, Direction.RIGHT)
    }
}