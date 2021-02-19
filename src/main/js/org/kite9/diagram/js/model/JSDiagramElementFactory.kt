package org.kite9.diagram.js.model

import org.kite9.diagram.common.elements.factory.TemporaryConnected
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.model.AbstractDiagramElementFactory
import org.kite9.diagram.dom.model.TemporaryConnectedImpl
import org.kite9.diagram.dom.painter.*
import org.kite9.diagram.js.painter.JSLeafPainter
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.DiagramElementType
import org.kite9.diagram.model.style.RectangularElementUsage
import org.w3c.dom.Element

class JSDiagramElementFactory(context: ElementContext?) : AbstractDiagramElementFactory<Element>() {

    init {
        this.context = context
    }

    override fun createDiagramElement(x: Element, parent: DiagramElement?): DiagramElement? {
        var type = context!!.getCSSStyleEnumProperty(CSSConstants.ELEMENT_TYPE_PROPERTY, x) as DiagramElementType
        var usage= context!!.getCSSStyleEnumProperty(CSSConstants.ELEMENT_USAGE_PROPERTY, x) as RectangularElementUsage
        var out = instantiateDiagramElement(parent, x, type, usage)
        return out
    }

    override fun createTemporaryConnected(parent: DiagramElement, idSuffix: String): TemporaryConnected {
        return TemporaryConnectedImpl(parent, idSuffix, SVGRectPainter("grid-temporary"))
    }

    override fun getDirectPainter(el: Element): Painter {
        return DirectSVGGroupPainter(el)
    }

    override fun getContainerPainter(el: Element): Painter {
        return SVGContainerRectangularPainter(el, context!!)
    }

    override fun getLeafPainter(el: Element): LeafPainter {
        return JSLeafPainter(el, context!!)
    }

    override fun getTextPainter(el: Element): LeafPainter {
        TODO("Not yet implemented")
    }

}