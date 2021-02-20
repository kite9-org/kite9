package org.kite9.diagram.visualization.orthogonalization.edge

import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

/**
 * Converts edges into darts.  In subclasses, takes account of fanning and labelling too.
 * @author robmoffat
 */
interface EdgeConverter {

    /**
     * This is used for converting planarization edges (i.e. elements in the planarization) into darts.
     *
     */
    fun convertPlanarizationEdge(
        e: PlanarizationEdge,
        o: Orthogonalization,
        incident: Direction,
        externalVertex: Vertex,
        sideVertex: Vertex,
        planVertex: Vertex,
        fanStep: Direction?
    ): IncidentDart

    /**
     * This is used for creating darts to represent the 2d shape of a vertex, which was a point in the planarization.
     */
    fun buildDartsBetweenVertices(
        underlyings: Map<DiagramElement, Direction?>,
        o: Orthogonalization,
        end1: Vertex,
        end2: Vertex,
        d: Direction
    ): List<Dart>
}