package org.kite9.diagram.dom.model

import org.kite9.diagram.common.elements.factory.AbstractTemporaryConnectedRectangular
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.position.*
import org.kite9.diagram.model.position.CostedDimension2D.Companion.ZERO
import org.kite9.diagram.model.style.*
import org.kite9.diagram.model.style.Placement.Companion.NONE
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A placeholder for spaces in a grid layout which are unoccupied.
 *
 * @author robmoffat
 */
class TemporaryConnectedRectangularImpl(parent: DiagramElement, id: String, val painter: Painter) :

    AbstractTemporaryConnectedRectangular(parent.getID() + "-g-" + id, parent), Container, HasSVGRepresentation, SizedRectangular {

    private var gcp: ContainerPosition? = null
    override fun toString(): String {
        return "[grid-temporary: " + getID() + "]"
    }

    private val rri: RectangleRenderingInformation = RectangleRenderingInformationImpl(null, null, false)
    override fun getRenderingInformation(): RectangleRenderingInformation {
        return rri
    }

    override fun getContainerPosition(): ContainerPosition? {
        return gcp
    }

    override fun setContainerPosition(cp: ContainerPosition) {
        gcp = cp
    }

    override fun getSizing(horiz: Boolean): DiagramElementSizing {
        return DiagramElementSizing.MINIMIZE
    }

    override fun deepContains(d: DiagramElement): Boolean {
        return false
    }

    override fun getConnectionsSeparationApproach(): ConnectionsSeparation {
        return ConnectionsSeparation.SEPARATE // irrelevant, won't have connections
    }

    override fun getContents(): MutableList<DiagramElement> {
        return mutableListOf()
    }

    override fun getLayout(): Layout? {
        return null
    }

    override fun getTraversalRule(d: Direction): BorderTraversal {
        return BorderTraversal.ALWAYS
    }

    override fun getGridColumns(): Int {
        return 1
    }

    override fun getGridRows(): Int {
        return 1
    }

    override fun getLinkGutter(): Double {
        return 0.0
    }

    override fun getLinkInset(): Double {
        return 0.0
    }

    override fun getConnectionAlignment(side: Direction): Placement {
        return NONE
    }

    override fun output(d: Document, p: XMLProcessor): Element? {
        painter.setDiagramElement(this)
        return painter.output(d, p)
    }

    override fun getMargin(d: Direction): Double {
        return 0.0
    }

    override fun getPadding(d: Direction): Double {
        return 0.0
    }

    override fun getSize(within: Dimension2D): CostedDimension2D {
        return ZERO
    }

    override fun getMinimumSize(): Dimension2D {
        return ZERO
    }
}