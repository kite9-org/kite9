package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Decal
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Leaf
import org.kite9.diagram.model.position.RenderingInformation
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Element

class DecalLeafImpl(
    el: Element,
    parent: DiagramElement,
    ctx: ElementContext,
    lo: LeafPainter,
    t: ContentTransform
) : AbstractModelDiagramElement(
    el, parent, ctx, lo, t
), Decal, Leaf {

    override fun getRenderingInformation(): RenderingInformation {
        return getParent()!!.getRenderingInformation()
    }

    override fun getContainer(): Container? {
        return getParent() as? Container
    }

}