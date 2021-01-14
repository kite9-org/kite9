package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.model.Container
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase

/**
 * Generates merges of a particular type, from a particular seed group.
 * Note that generators should not understand the precise logic of what constitutes a valid
 * merge, that is left to the priority rules.
 */
interface MergeGenerator {
    /**
     * With the given strategy, generate some merge options for the given group.
     */
    fun generate(poll: GroupPhase.Group)

    /**
     * Signals that a container has become live.
     */
    fun containerIsLive(c: Container)
}