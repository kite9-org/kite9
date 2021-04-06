package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.ConnectionAlignment
import org.kite9.diagram.model.style.ConnectionsSeparation
import org.kite9.diagram.model.style.ContentTransform
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

    protected fun initConnectionAlignment() {
        alignments = arrayOf(
            ctx.getConnectionAlignment(CSSConstants.CONNECTION_ALIGN_TOP_PROPERTY, theElement),
            ctx.getConnectionAlignment(CSSConstants.CONNECTION_ALIGN_RIGHT_PROPERTY, theElement),
            ctx.getConnectionAlignment(CSSConstants.CONNECTION_ALIGN_BOTTOM_PROPERTY, theElement),
            ctx.getConnectionAlignment(CSSConstants.CONNECTION_ALIGN_LEFT_PROPERTY, theElement)
        )
    }

    private var links: Collection<Connection>? = null
    private var linkGutter = 0.0
    private var linkInset = 0.0
    private var alignments: Array<ConnectionAlignment> = emptyArray()

    override fun getLinks(): Collection<Connection> {
        ensureInitialized()
        if (links == null) {
            links = diagram.getConnectionsFor(this)
        }
        return links!!
    }

    override fun getConnectionsSeparationApproach(): ConnectionsSeparation {
        return ctx.getCSSStyleEnumProperty(CSSConstants.CONNECTIONS_PROPERTY, theElement, ConnectionsSeparation::class)!!
    }

    override fun getLinkGutter(): Double {
        return linkGutter
    }

    override fun getLinkInset(): Double {
        return linkInset
    }

    override fun getConnectionAlignment(d: Direction): ConnectionAlignment {
        ensureInitialized()
        return alignments[d.ordinal]
    }
}