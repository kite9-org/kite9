package org.kite9.diagram.visualization.planarization.rhd.grouping.basic

import org.kite9.diagram.visualization.planarization.rhd.GroupPhase

/**
 * A merge option is keyed on the two groups it merges.
 */
data class MergeKey(val a: GroupPhase.Group, val b: GroupPhase.Group) {

    override fun toString(): String {
        return "[MK: " + a + " (" + a.size + ")  " + b + "(" + b.size + ")]"
    }
}