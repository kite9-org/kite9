package org.kite9.diagram.model.style

enum class BorderTraversal {
    ALWAYS, LEAVING, NONE;

    companion object {
        fun reduce(a: BorderTraversal?, b: BorderTraversal?): BorderTraversal? {
            return if (a == null) {
                b
            } else if (b == null) {
                a
            } else if (a != ALWAYS && b != ALWAYS) {
                NONE
            } else {
                ALWAYS
            }
        }
    }
}