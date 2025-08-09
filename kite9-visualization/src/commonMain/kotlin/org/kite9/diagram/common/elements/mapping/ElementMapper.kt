package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * Given original (user defined) diagram element, returns the planarization element(s) relating to it.
 */
interface ElementMapper {

    fun getGridPositioner(): GridPositioner

    fun hasOuterCornerVertices(c: DiagramElement): Boolean

    fun requiresPlanarizationCornerVertices(c: DiagramElement): Boolean

    fun getOuterCornerVertices(c: DiagramElement): CornerVertices

    fun getPlanarizationVertex(c: DiagramElement): Vertex

    fun getEdge(
        from: Connected,
        vfrom: Vertex,
        to: Connected,
        vto: Vertex,
        element: BiDirectional<Connected>
    ): PlanarizationEdge?

    /**
     * Used for debugging purposes
     */
    fun allVertices(): Collection<Vertex>

    /**
     * Works out the orderings (in terms of fractions) for ports on the side of the diagram element.
     * Also includes the element itself, for it's align mid-point.
     */
    fun getFractions(c: DiagramElement, d: Direction) : Map<DiagramElement, LongFraction>

}