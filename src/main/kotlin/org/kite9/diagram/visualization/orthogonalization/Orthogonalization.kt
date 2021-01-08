package org.kite9.diagram.visualization.orthogonalization

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection
import org.kite9.diagram.visualization.planarization.Planarization

interface Orthogonalization {

    /**
     * List of all darts constructing the Orthogonalization.
     */
    fun getAllDarts(): Set<Dart>

    /**
     * All vertices used in the Orthogonalization, including edge corner vertices and vertex boundary vertices.
     * i.e. vertices that meet darts.
     */
    fun getAllVertices(): Collection<Vertex>

    /**
     * Dart-perimeter faces of the Orthogonalization
     */
    fun getFaces(): List<DartFace>

    /**
     * Orthogonalization acts as a factory for darts. Always returns a dart, even if it
     * is an existing one
     */
    fun createDart(from: Vertex, to: Vertex, partOf: DiagramElement, d: Direction, partOfSide: Direction): Dart

    fun createDart(
        from: Vertex,
        to: Vertex,
        partOf: Set<DiagramElement>,
        d: Direction,
        partOfSide: Direction
    ): Dart

    fun createDart(from: Vertex, to: Vertex, partOf: Map<DiagramElement, Direction>, d: Direction): Dart

    /**
     * In the same way as a [Face] is a clockwise ordering of edges, a [DartFace] is a clockwise
     * ordering of darts, created in the orthogonalization process.
     *
     * Outer faces also still exist, in order to be embedded within other faces.  These are anti-clockwise
     * ordered (though it's irrelevant).
     */
    fun createDartFace(partOf: Rectangular?, outerFace: Boolean, darts: List<DartDirection>): DartFace

    /**
     * Gets the underlying planarization for this orthogonalization
     */
    fun getPlanarization(): Planarization
    fun getDartsForDiagramElement(e: DiagramElement): Set<Dart>?
    fun getWaypointsForBiDirectional(c: Connection): List<Vertex>?
    fun getDartFacesForRectangular(r: Rectangular): List<DartFace>
    fun getDartFacesForDart(d: Dart): List<DartFace>?
    fun splitDart(dart: Dart, splitWithVertex: Vertex): Pair<Dart>
}