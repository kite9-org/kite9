package org.kite9.diagram.dom.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.*
import org.kite9.diagram.model.style.ConnectionsSeparation
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.MeasuredRectangularPosition
import org.w3c.dom.Element

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

    private var links: Collection<Connection>? = null
    private var linkGutter = 0.0
    private var linkInset = 0.0

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

    override fun getConnectionAlignment(d: Dimension): MeasuredRectangularPosition {
        ensureInitialized()
        return alignments[d.ordinal]
    }
}