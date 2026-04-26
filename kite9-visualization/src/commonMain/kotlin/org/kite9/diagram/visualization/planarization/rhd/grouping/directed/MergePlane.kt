package org.kite9.diagram.visualization.planarization.rhd.grouping.directed

/**
 * This enum makes sure that compound groups can only join correctly
 * with other compound groups. e.g. X or Y axis merging with their same
 * type.  "Unknown" means that there is no ongoing type of merge so you
 * can combine with any other group.
 */
enum class MergePlane {

    X_FIRST_MERGE, Y_FIRST_MERGE, UNKNOWN;

    fun matches(state: MergePlane): Boolean {
        if (this == X_FIRST_MERGE) {
            if (state == Y_FIRST_MERGE) {
                return false
            }
        } else if (this == Y_FIRST_MERGE) {
            if (state == X_FIRST_MERGE) {
                return false
            }
        }
        return true
    }

}