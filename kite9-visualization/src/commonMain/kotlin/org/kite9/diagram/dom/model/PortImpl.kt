package org.kite9.diagram.dom.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Port
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl
import org.kite9.diagram.model.style.*
import org.w3c.dom.Element

class PortImpl(
    el: Element,
    parent: DiagramElement,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) : AbstractModelDiagramElement (el, parent, ctx, rp, t), Port {

    private val portSide : Direction by lazy {
        val out = ElementContext.getCssStyleEnumProperty<Direction>(CSSConstants.DIRECTION, theElement, ctx) ?: Direction.DOWN
        out
    }

    private val portPlacement : Placement by lazy {
        ctx.getCssStylePlacementProperty(CSSConstants.PORT_POSITION, theElement)
    }

    override fun getPortDirection(): Direction {
        ensureInitialized()
        return portSide
    }

    override fun getContainerPosition(d: Dimension): Placement {
        ensureInitialized()
        if (d == Dimension.H) {
            return when (portSide) {
                Direction.UP, Direction.DOWN -> portPlacement
                Direction.LEFT -> Placement(Measurement.PERCENTAGE, 0.0)
                Direction.RIGHT -> Placement(Measurement.PERCENTAGE, 100.0)
            }
        } else {
            return when (portSide) {
                Direction.LEFT, Direction.RIGHT -> portPlacement
                Direction.UP -> Placement(Measurement.PERCENTAGE, 0.0)
                Direction.DOWN -> Placement(Measurement.PERCENTAGE, 100.0)
            }
        }
    }

    private var ri: RectangleRenderingInformation = RectangleRenderingInformationImpl()

    override fun getRenderingInformation(): RectangleRenderingInformation {
        return ri
    }

    private val portLinks: Collection<Connection> by lazy {
        diagram.getConnectionsFor(this)
    }

    override fun getLinks(): Collection<Connection> {
        ensureInitialized()
        return portLinks
    }

    override fun getConnectionsSeparationApproach(): ConnectionsSeparation {
        return ElementContext.getCssStyleEnumProperty<ConnectionsSeparation>(CSSConstants.CONNECTIONS_PROPERTY, theElement, ctx)!!
    }

    override fun getContainer(): Container? {
        return getParent() as? Container
    }


}