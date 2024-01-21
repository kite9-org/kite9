package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Superclass for all segment-based compaction methods.   The process works by converting each face into
 * a set of segments, where segments represent horizontal or vertical positions.
 *
 *
 * @author robmoffat
 */
class C2PluggableCompactor(val steps: Array<C2CompactionStep>) : Logable {

    val log by lazy { Kite9Log.instance(this) }

    fun compactDiagram(d: Diagram, gr: GroupResult): C2Compaction {
        val compaction = C2CompactionImpl(d)
        compact(compaction, gr.groups().first())
        return compaction
    }

    private fun compact(c: C2Compaction, g: Group) {
        for (step in steps) {
            try {
                step.compact(c, g)
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    companion object {
        val VERTICAL = setOf(Direction.UP, Direction.DOWN)
        val HORIZONTAL = setOf(Direction.LEFT, Direction.RIGHT)
    }

    override val prefix: String
        get() = "C2PC"

    override val isLoggingEnabled: Boolean
        get() = true
}