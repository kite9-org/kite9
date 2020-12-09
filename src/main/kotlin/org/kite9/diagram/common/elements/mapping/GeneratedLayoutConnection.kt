package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.AbstractBiDirectional
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.RouteRenderingInformation
import org.kite9.diagram.model.position.RouteRenderingInformationImpl

/**
 * This connection is used with a [ContainerLayoutEdge] and is used to create a layout between
 * two elements of a container.
 *
 * Also, with [BorderEdge], when two containers border each other.
 */
class GeneratedLayoutConnection(private val f: Connected, private val t: Connected, private val dd: Direction) :
    AbstractBiDirectional<Connected>(), Connection, Temporary {

    override fun toString(): String {
        return "glc-" + getID()
    }

    override operator fun compareTo(other: DiagramElement): Int {
        return if (other is AbstractBiDirectional<*>) {
            getID().compareTo((other as AbstractBiDirectional<*>).getID())
        } else {
            -1
        }
    }

    private val rri: RouteRenderingInformation = RouteRenderingInformationImpl()
    private val id = f.getID()+"-glc-"+t.getID();

    override fun getRenderingInformation(): RouteRenderingInformation {
        return rri
    }

    override fun getParent(): DiagramElement? {
        return null
    }

    val container: Container?
        get() = null

    override fun getDepth(): Int {
        return 1
    }

    override fun getFromDecoration(): Terminator? {
        return null
    }

    override fun getToDecoration(): Terminator? {
        return null
    }

    override fun getFromLabel(): Label? {
        return null
    }

    override fun getToLabel(): Label? {
        return null
    }

    override fun getRank(): Int {
        return 0
    }

    override fun getMargin(d: Direction): Double {
        return 0.0
    }

    override fun getPadding(d: Direction): Double {
        return 0.0
    }

    override fun getDecorationForEnd(end: DiagramElement): Terminator? {
        return null
    }

    override fun getMinimumLength(): Double {
        return 0.0
    }

    override fun getCornerRadius(): Double {
        return 0.0
    }

    override fun getFromArrivalSide(): Direction? {
        return reverse(dd)
    }

    override fun getToArrivalSide(): Direction? {
        return dd
    }

    override fun getFrom(): Connected {
        return f
    }

    override fun getTo(): Connected {
        return t
    }

    override fun getDrawDirection(): Direction? {
        return dd
    }

    override fun getID(): String {
        return id
    }


}