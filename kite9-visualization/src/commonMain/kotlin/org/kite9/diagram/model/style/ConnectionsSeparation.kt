package org.kite9.diagram.model.style

enum class ConnectionsSeparation {
    SEPARATE,   // connections arriving at this container should arrive at different positions
    SAME_SIDE  // connections arriving at this container use the same position on a given side
}