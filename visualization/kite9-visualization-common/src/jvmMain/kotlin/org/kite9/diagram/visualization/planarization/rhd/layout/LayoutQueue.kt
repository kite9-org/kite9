package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

interface LayoutQueue {

    /**
     * Adds another group to the work queue
     */
    fun offer(item: Group)

    /**
     * Informs the queue that you have completed work on item.
     */
    fun complete(item: CompoundGroup)

    /**
     * Retrieves the next group to do, or null if we're finished
     */
    fun poll(): Group?
}