package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge

import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * A merge option is keyed on the two groups it merges.
 */
data class MergeKey(val a: Group, val b: Group) {

    override fun toString(): String {
        return "[MK: " + a + " (" + a.size + ")  " + b + "(" + b.size + ")]"
    }
}