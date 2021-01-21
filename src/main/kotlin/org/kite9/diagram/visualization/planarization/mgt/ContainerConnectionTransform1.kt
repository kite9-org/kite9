package org.kite9.diagram.visualization.planarization.mgt

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.planarization.EdgeMapping
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.Tools
import org.kite9.diagram.visualization.planarization.transform.PlanarizationTransform

/**
 * Edges connecting to a container either connect to either a container vertex,
 * or one of the vertices within the container.
 *
 * When connecting to one of the vertices within the container, we must remove the remainder of the edge
 * which is inside the container itself, leaving just the part connecting to the container edge.
 *
 * This transform is necessary when you allow connections to containers.
 *
 * @author robmoffat
 */
class ContainerConnectionTransform1(elementMapper: ElementMapper?) : PlanarizationTransform, Logable {
    private val log = Kite9Log(this)
    override fun transform(pln: Planarization) {
        modifyInternalEdges(pln)
    }

    var t = Tools()
    private fun modifyInternalEdges(pln: Planarization) {
        val toRemove: MutableCollection<PlanarizationEdge> = DetHashSet()
        for ((de, edgeMapping) in pln.edgeMappings) {
            if (de is BiDirectional<*>) {
                val from = de.getFrom() as Connected
                val to = de.getTo() as Connected
                if (from is Container || to is Container) {
                    val forwardList: List<PlanarizationEdge> = edgeMapping.edges
                    eraseEnds(edgeMapping, de, from, to, toRemove, forwardList, edgeMapping.startVertex)
                    if (toRemove.size > 0) {
                        edgeMapping.remove(toRemove)
                        for (edge in toRemove) {
                            t.removeEdge(edge, pln)
                        }
                        toRemove.clear()
                    }
                }
            }
        }
    }

    /**
     * Work along the edges (edgeMapping list) until you are no longer inside the diagram element
     */
    private fun eraseEnds(
        mapping: EdgeMapping,
        de: DiagramElement,
        from: DiagramElement?,
        to: DiagramElement?,
        toRemove: MutableCollection<PlanarizationEdge>,
        edges: List<PlanarizationEdge>,
        start: Vertex
    ) {
        var start = start
        var outside = true
        for (edge in edges) {
            val change = start.isPartOf(from) || start.isPartOf(to)
            if (change) {
                outside = !outside
                log.send("Changing to: outside=$outside")
            }
            if (outside) {
                log.send(if (log.go()) null else "Removing edge $edge as it's not part of $de")
                toRemove.add(edge)
            } else {
                // do nothing, good edge
            }
            start = edge.otherEnd(start)
        }
    }

    override val prefix: String
        get() = "CET1"
    override val isLoggingEnabled: Boolean
        get() = true
}