package org.kite9.diagram.dom.painter

import org.kite9.diagram.model.position.Rectangle2D

interface LeafPainter : Painter {

    fun bounds(): Rectangle2D
}