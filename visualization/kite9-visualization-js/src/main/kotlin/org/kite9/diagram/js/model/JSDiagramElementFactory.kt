package org.kite9.diagram.js.model

import org.kite9.diagram.common.elements.factory.TemporaryConnectedRectangular
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.model.AbstractDiagramElementFactory
import org.kite9.diagram.dom.model.TemporaryConnectedRectangularImpl
import org.kite9.diagram.dom.painter.*
import org.kite9.diagram.js.bridge.JSElementContext
import org.kite9.diagram.js.painter.JSLeafPainter
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.DiagramElementType
import org.kite9.diagram.model.style.RectangularElementUsage
import org.w3c.dom.Element

class JSDiagramElementFactory(context: JSElementContext) : AbstractDiagramElementFactory<Element>(false) {

    init {
        this.context = context
    }

    override fun createDiagramElement(x: Element, parent: DiagramElement?): DiagramElement? {
        val type = context!!.getCSSStyleEnumProperty(CSSConstants.ELEMENT_TYPE_PROPERTY, x, DiagramElementType::class)
        val usage= context!!.getCSSStyleEnumProperty(CSSConstants.ELEMENT_USAGE_PROPERTY, x, RectangularElementUsage::class)

        if (type == null || usage == null) {
            return null
        }

        val out = instantiateDiagramElement(parent, x, type, usage)

        if (out != null) {
            if (parent != null) {
                (context as JSElementContext).addChild(parent, out)
            }

            (context as JSElementContext).register(x, out);
        }

        return out
    }

    override fun createTemporaryConnected(parent: DiagramElement, idSuffix: String): TemporaryConnectedRectangular {
        return TemporaryConnectedRectangularImpl(parent, idSuffix, SVGRectPainter("grid-temporary"))
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
       return JSLeafPainter(el, context!!)
    }

}