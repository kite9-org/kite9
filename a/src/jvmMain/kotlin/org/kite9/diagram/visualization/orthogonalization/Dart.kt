package org.kite9.diagram.visualization.orthogonalization

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * Darts are created by the orthogonalization process.  They are directed edges within the diagram which
 * represent the borders of Retangular diagram elements, or Connections.
 *
 * @author robmoffat
 */
interface Dart : Edge {

    fun getDiagramElements(): MutableMap<DiagramElement, Direction?>

    /**
     * Darts always have a direction
     */
    override fun getDrawDirection(): Direction
}