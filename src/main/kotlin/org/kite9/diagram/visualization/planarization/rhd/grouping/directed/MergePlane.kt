package org.kite9.diagram.visualization.planarization.rhd.grouping.directed

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