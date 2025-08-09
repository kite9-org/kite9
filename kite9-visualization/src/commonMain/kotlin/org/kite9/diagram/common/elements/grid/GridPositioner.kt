package org.kite9.diagram.common.elements.grid

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.common.elements.mapping.CornerVertices
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.model.Container

/**
 * Handles positioning of elements for [Layout].GRID.
 *
 * @author robmoffat
 */
interface GridPositioner {

    /**
     * Works out the position of the elements within the grid for a given container.
     *
     * Should cache too.
     *
     * @param allowSpanning Set to true if we should consider the full span of the "occupies-x" and "occupies-y" directive, or just the lower bound.
     */
    fun placeOnGrid(gridContainer: Container, allowSpanning: Boolean): Array<Array<DiagramElement>>

    /**
     * Used for creating the perimeter of a grid.  Returns the perimeter vertices in clockwise
     * order.
     */
    fun getClockwiseOrderedContainerVertices(cv: CornerVertices): List<MultiCornerVertex>
}