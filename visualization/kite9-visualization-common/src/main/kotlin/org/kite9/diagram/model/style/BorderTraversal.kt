package org.kite9.diagram.model.style

enum class BorderTraversal {
    ALWAYS, LEAVING, PREVENT;

    companion object {
        fun reduce(a: BorderTraversal?, b: BorderTraversal?): BorderTraversal? {
            return if (a == null) {
                b
            } else if (b == null) {
                a
            } else if (a != ALWAYS && b != ALWAYS) {
                PREVENT
            } else {
                ALWAYS
            }
        }
    }
}