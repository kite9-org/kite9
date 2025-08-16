package org.kite9.diagram.dom.transform

import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.model.position.Dimension2D

interface LeafTransformer : SVGTransformer {

    fun getBounds(p: LeafPainter): Dimension2D
}