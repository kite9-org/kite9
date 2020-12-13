/**
 *
 */
package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.fraction.BigFraction
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * This automatically comes populated with the four corner vertices, but
 * by using the <pre>createVertex</pre> method, you can add extra ones.
 *
 * @author robmoffat
 */
interface CornerVertices {

    /**
     * Unordered collection of vertices around the container.
     */
    fun identifyPerimeterVertices()

    fun getPerimeterVertices(): Collection<MultiCornerVertex>

    /**
     * Creates or returns a vertex from within the rectangle of the container.
     *
     */
    fun createVertex(x: BigFraction, y: BigFraction): MultiCornerVertex

    /**
     * Returns all vertices in the container, and in any parent containers (if a gridded container).
     */
    fun getAllAscendentVertices(): MutableCollection<MultiCornerVertex>

    /**
     * Returns all vertices in the container, and in any child containers (if a gridded container).
     */
    fun getAllDescendentVertices(): MutableCollection<MultiCornerVertex>

    /**
     * Returns vertices uniquely declared by this later of the container vertices.
     */
    fun getVerticesAtThisLevel(): Collection<MultiCornerVertex>

    /**
     * Looks at the hierarchy of container vertices, and merges any that overlap before
     * they are added to the planarization.
     *
     * Returns null if there is already a vertex occupying the same place/position.
     *
     * This is potentially a costly operation, as we have to check every vertex.
     *
     */
    fun mergeDuplicates(cv: MultiCornerVertex, rh: RoutableHandler2D): MultiCornerVertex?

    fun getTopLeft(): MultiCornerVertex
    fun getTopRight(): MultiCornerVertex
    fun getBottomLeft(): MultiCornerVertex
    fun getBottomRight(): MultiCornerVertex
    fun getContainerDepth(): Int
}