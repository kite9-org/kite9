package org.kite9.diagram.dom.model

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.DiagramElementType
import org.kite9.diagram.model.style.RectangularElementUsage
import org.w3c.dom.Element

abstract class AbstractDiagramElementFactory<X>(val failOnUnspecified : Boolean = true) : DiagramElementFactory<X> {

    protected var context: ElementContext? = null

    override fun setElementContext(ec: ElementContext) {
        context = ec
    }

    protected fun instantiateDiagramElement(
        parent: DiagramElement?,
        el: Element,
        lt: DiagramElementType,
        usage: RectangularElementUsage
    ): DiagramElement? {
        return if (parent == null && lt !== DiagramElementType.DIAGRAM) {
            // all elements apart from diagram must have a parent
            null
        } else when (lt) {
            DiagramElementType.DIAGRAM -> {
                if (parent != null) {
                    throw context!!.contextualException("Can't nest type 'diagram' @ " + getId(el), el)
                }
                DiagramImpl(el, context!!, getContainerPainter(el)!!, ContentTransform.POSITION)
            }
            DiagramElementType.CONTAINER -> when (usage) {
                RectangularElementUsage.LABEL -> LabelContainerImpl(
                    el,
                    parent!!,
                    context!!,
                    getContainerPainter(el),
                    ContentTransform.POSITION
                )
                RectangularElementUsage.REGULAR -> ConnectedContainerImpl(
                    el,
                    parent,
                    context!!,
                    getContainerPainter(el),
                    ContentTransform.POSITION
                )
                RectangularElementUsage.DECAL -> throw context!!.contextualException(
                    "Decal containers not supported yet: @" + getId(
                        el
                    ), el
                )
                else -> throw context!!.contextualException("Decal containers not supported yet: @" + getId(el), el)
            }
            DiagramElementType.TEXT -> when (usage) {
                RectangularElementUsage.LABEL -> LabelLeafImpl(
                    el,
                    parent!!,
                    context!!,
                    getTextPainter(el),
                    ContentTransform.CROP
                )
                RectangularElementUsage.DECAL -> DecalLeafImpl(
                    el,
                    parent!!,
                    context!!,
                    getTextPainter(el),
                    ContentTransform.RESCALE
                )
                RectangularElementUsage.REGULAR -> ConnectedLeafImpl(
                    el,
                    parent!!,
                    context!!,
                    getTextPainter(el),
                    ContentTransform.CROP
                )
                else -> ConnectedLeafImpl(el, parent!!, context!!, getTextPainter(el)!!, ContentTransform.CROP)
            }
            DiagramElementType.SVG -> when (usage) {
                RectangularElementUsage.LABEL -> LabelLeafImpl(
                    el,
                    parent!!,
                    context!!,
                    getLeafPainter(el),
                    ContentTransform.CROP
                )
                RectangularElementUsage.DECAL -> DecalLeafImpl(
                    el,
                    parent!!,
                    context!!,
                    getLeafPainter(el),
                    ContentTransform.POSITION
                )
                RectangularElementUsage.REGULAR -> ConnectedLeafImpl(
                    el,
                    parent!!,
                    context!!,
                    getLeafPainter(el),
                    ContentTransform.CROP
                )
                else -> ConnectedLeafImpl(el, parent!!, context!!, getLeafPainter(el)!!, ContentTransform.CROP)
            }
            DiagramElementType.LINK -> ConnectionImpl(
                el,
                parent,
                context!!,
                getDirectPainter(el),
                ContentTransform.POSITION
            )
            DiagramElementType.LINK_END -> {
                if (!(parent is Connection)) {
                    throw context!!.contextualException("Terminators must be inside link' @ " + getId(el), el)
                }
                return TerminatorImpl(
                    el,
                    parent!!,
                    context!!,
                    getDirectPainter(el),
                    ContentTransform.POSITION
                )
            }
            DiagramElementType.NONE -> null
            DiagramElementType.PORT -> PortImpl(el, parent!!, context!!, getDirectPainter(el), ContentTransform.POSITION)
            DiagramElementType.UNSPECIFIED ->
                if (failOnUnspecified) {
                    throw context!!.contextualException(
                        "Don't know how to process element: " + el + "(" + el.tagName + ") with type " + lt + " and usage " + usage + " and parent " + parent,
                        el
                    )
                } else {
                    return null
                }
        }
    }

    protected abstract fun getDirectPainter(el: Element): Painter

    protected abstract fun getContainerPainter(el: Element): Painter

    protected abstract fun getLeafPainter(el: Element): LeafPainter

    protected abstract fun getTextPainter(el: Element): LeafPainter

    protected fun getId(el: Element): String {
        return el.getAttribute("id")!!
    }
}