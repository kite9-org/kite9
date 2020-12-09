package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.elements.AbstractBiDirectional
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.RouteRenderingInformation
import org.kite9.diagram.model.position.RouteRenderingInformationImpl

/**
 * Used for enforcing container ordering.
 *
 * @author robmoffat
 */
class OrderingTemporaryConnection(private val f: Connected,
                                  private val t: Connected,
                                  private val dd: Direction,
                                  private val c: Container) : AbstractBiDirectional<Connected>(), Connection {

    private val id = f.getID() + ":"+t.getID();

    override fun getFrom(): Connected {
        return f
    }

    override fun getTo(): Connected {
        return t
    }

    override fun getDrawDirection(): Direction {
        return dd
    }

    override fun getID(): String {
        return id
    }

    fun getContainerBeingOrdered() : Container {
        return c
    }

    override fun getParent() : DiagramElement {
        return c
    }

    /**
     * Set required to false if we find a link that will do the same job
     */
    var isRequired = false

    override fun getFromDecoration(): Terminator? {
        return null
    }

    override fun getToDecoration(): Terminator? {
        return null
    }

    override fun getDecorationForEnd(end: DiagramElement): Terminator? {
        return null
    }

    override fun getFromLabel(): Label? {
        return null
    }

    override fun getToLabel(): Label? {
        return null
    }

    private val rri: RouteRenderingInformation = RouteRenderingInformationImpl()

    override fun getRenderingInformation(): RouteRenderingInformation {
        return rri;
    }

    override fun getRank(): Int { return 1}

    override fun getMargin(d: Direction): Double { return 0.0 }

    override fun getPadding(d: Direction): Double { return 0.0 }

    override fun getMinimumLength(): Double { return 0.0 }

    override fun getCornerRadius(): Double { return 0.0 }

    override fun getFromArrivalSide(): Direction? {
        return Direction.reverse(dd)
    }

    override fun getToArrivalSide(): Direction? {
        return dd
    }

    override fun getDepth(): Int {
        return 1
    }

    override operator fun compareTo(other: DiagramElement): Int {
        return if (other is AbstractBiDirectional<*>) {
            getID().compareTo((other as AbstractBiDirectional<*>).getID())
        } else {
            -1
        }
    }
}