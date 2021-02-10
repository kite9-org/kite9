package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.dom.transform.SVGTransformer
import org.kite9.diagram.dom.transform.TransformFactory.initializeTransformer
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.CostedDimension2D
import org.kite9.diagram.model.position.CostedDimension2D.Companion.UNBOUNDED
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Represents [DiagramElement]s that contain SVG that will need rendering, and the method to render them
 * (delegating to Painter and Transform implementations).
 *
 * @author robmoffat
 */
abstract class AbstractModelDiagramElement(
    el: Element,
    parent: DiagramElement?,
    ctx: ElementContext,
    override val painter: Painter,
    t: ContentTransform
) : AbstractDOMDiagramElement(el, parent) {
    @JvmField
	protected var ctx: ElementContext
    private val defaultTransform: ContentTransform
    @JvmField
	protected var transformer: SVGTransformer? = null
    @JvmField
	protected var margin = DoubleArray(4)
    @JvmField
	protected var padding = DoubleArray(4)
    override fun initialize() {
        initializeDirectionalCssValues(padding, CSSConstants.KITE9_CSS_PADDING_PROPERTY_PREFIX)
        initializeDirectionalCssValues(margin, CSSConstants.KITE9_CSS_MARGIN_PROPERTY_PREFIX)
        initTransform()
    }

    protected fun getCssDoubleValue(prop: String?): Double {
        return ctx.getCssDoubleValue(prop, theElement)
    }

    override fun paintElementToDocument(d: Document?, postProcessor: XMLProcessor?): Element {
        return transformer!!.postProcess(painter, d!!, postProcessor!!)
    }

    private fun initTransform() {
        val t = ctx.getCSSStyleProperty(CSSConstants.CONTENT_TRANSFORM, theElement) as ContentTransform
        transformer = initializeTransformer(this, t, defaultTransform)
    }

    open fun getMargin(d: Direction): Double {
        ensureInitialized()
        return margin[d.ordinal]
    }

    open fun getPadding(d: Direction): Double {
        ensureInitialized()
        return padding[d.ordinal]
    }

    protected fun initializeDirectionalCssValues(vals: DoubleArray, prefix: String) {
        vals[Direction.UP.ordinal] = getCssDoubleValue(prefix + CSSConstants.TOP)
        vals[Direction.DOWN.ordinal] = getCssDoubleValue(prefix + CSSConstants.BOTTOM)
        vals[Direction.LEFT.ordinal] = getCssDoubleValue(prefix + CSSConstants.LEFT)
        vals[Direction.RIGHT.ordinal] = getCssDoubleValue(prefix + CSSConstants.RIGHT)
    }

    protected val sizeBasedOnPadding: CostedDimension2D
        protected get() {
            val left = getPadding(Direction.LEFT)
            val right = getPadding(Direction.RIGHT)
            val up = getPadding(Direction.UP)
            val down = getPadding(Direction.DOWN)
            return CostedDimension2D(left + right, up + down, UNBOUNDED)
        }

    override fun initContents(): List<DiagramElement?>? {
        val contents: MutableList<DiagramElement?> = ArrayList()
        for (de in ctx.getChildDiagramElements(theElement, this)!!) {
            if (de is Connection) {
                registerConnection((de as Connection?)!!)
            } else if (de != null) {
                contents.add(de)
            }
        }
        return contents
    }

    init {
        painter.setDiagramElement(this)
        this.ctx = ctx
        defaultTransform = t
    }
}