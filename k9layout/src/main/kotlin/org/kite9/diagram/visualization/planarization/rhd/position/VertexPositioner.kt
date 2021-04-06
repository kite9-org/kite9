package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.elements.mapping.CornerVertices
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.DiagramElement

/**
 * This places vertices on the diagram, based on the [PositionRoutableHandler2D] position of them.
 *
 * @author robmoffat
 */
interface VertexPositioner {
    /**
     * Keeps track of the sizes of elements in the grid, so we can ensure we don't put things on that are too small.
     */
    fun checkMinimumGridSizes(ri: RoutingInfo)

    fun setPerimeterVertexPositions(
        before: Connected?,
        c: DiagramElement,
        after: Connected?,
        cvs: CornerVertices,
        out: MutableList<Vertex>
    )

    fun setCentralVertexPosition(c: DiagramElement, out: MutableList<Vertex>)
}