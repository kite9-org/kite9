package org.kite9.diagram.common.elements

import org.kite9.diagram.model.position.Dimension2D

interface Positioned {
    var x: Double
    var y: Double
    val position: Dimension2D
}