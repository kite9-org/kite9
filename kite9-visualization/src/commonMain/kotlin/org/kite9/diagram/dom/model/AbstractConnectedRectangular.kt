package org.kite9.diagram.dom.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.CostedDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.style.ConnectionsSeparation
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.Measurement
import org.kite9.diagram.model.style.Placement
import org.w3c.dom.Element
import kotlin.math.max

/**
 * Handles DiagramElements which are also Connnected.
 *
 *
 * @author robmoffat
 */
abstract class AbstractConnectedRectangular(
    el: Element,
    parent: DiagramElement?,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) : AbstractCompactedRectangular(
    el, parent, ctx, rp, t
), ConnectedRectangular {
    /**
     * Call this method prior to using the functionality, so that we can ensure
     * all the members are set up correctly.
     */
    override fun initialize() {
        super.initialize()
        linkGutter = ctx.getCssStyleDoubleProperty(CSSConstants.LINK_GUTTER, theElement)
        linkInset = ctx.getCssStyleDoubleProperty(CSSConstants.LINK_INSET, theElement)
        initConnectionAlignment()
    }

    protected fun initConnectionAlignment() {
        alignments = arrayOf(
            ctx.getCssStylePlacementProperty(CSSConstants.HORIZONTAL_ALIGN_POSITION, theElement),
            ctx.getCssStylePlacementProperty(CSSConstants.VERTICAL_ALIGN_POSITION, theElement)
        )
    }

    private var links: Collection<Connection>? = null
    private var linkGutter = 0.0
    private var linkInset = 0.0
    private var alignments: Array<Placement> = emptyArray()

    override fun getLinks(): Collection<Connection> {
        ensureInitialized()
        if (links == null) {
            links = diagram.getConnectionsFor(this)
        }
        return links!!
    }

    override fun getConnectionsSeparationApproach(): ConnectionsSeparation {
        return ElementContext.getCssStyleEnumProperty<ConnectionsSeparation>(CSSConstants.CONNECTIONS_PROPERTY, theElement, ctx)!!
    }

    override fun getLinkGutter(): Double {
        return linkGutter
    }

    override fun getLinkInset(): Double {
        return linkInset
    }

    override fun getConnectionAlignment(d: Dimension): Placement {
        ensureInitialized()
        return alignments[d.ordinal]
    }

    /**
     * Connected elements might also have ports, which inform the size of the element
     */
    override fun ensureMinimumSize(c: Dimension2D, within: Dimension2D): CostedDimension2D {
        fun maxInPositionedDimension(d: Dimension, c: Container) : Double? {
            return c.getContents()
                .filterIsInstance<PlacementPositioned>()
                .map { it.getContainerPosition(d) }
                .filter { it.type == Measurement.PIXELS }
                .map { it.amount }
                .maxOrNull()
        }

        if (this is Container) {
            val maxH = maxInPositionedDimension(Dimension.H, this)
            val maxV = maxInPositionedDimension(Dimension.V, this)
            val c2 = CostedDimension2D(max(c.w, maxH ?: 0.0), max(c.h, maxV ?: 0.0))
            return super.ensureMinimumSize(c2, within)
        } else {
            return super.ensureMinimumSize(c, within)
        }
    }
}