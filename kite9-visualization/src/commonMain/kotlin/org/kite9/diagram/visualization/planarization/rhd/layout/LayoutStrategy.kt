package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult

/**
 * Traverses a hierarchy of groups constructed in the Grouping phase, and works out the best
 * layout for them to minimize cost.
 * @author robmoffat
 */
interface LayoutStrategy {

    fun layout(mr: GroupResult, emptyQueue: LayoutQueue)
}