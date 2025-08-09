package org.kite9.diagram.visualization.planarization.transform

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.mapping.GeneratedLayoutBiDirectional
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.Tools
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge

/**
 * Simplifies the layout of the planarization by looking for layout edges, and, if they are part of a small
 * square face with a connection and two container edges, remove the layout edge and use the connection for
 * enforcing the layout instead.
 *
 */
class LayoutSimplificationTransform : PlanarizationTransform {

    var t = Tools()

    override fun transform(pln: Planarization) {
//		if (true) 
//			return;
        val faces = pln.faces
        var currentFace = 0
        while (currentFace < faces.size) {
            val f = faces[currentFace]
            var removalDone = false
            if (f.size() == 4 && !f.isOuterFace) {
                // case of two containers directed against each other
                for (i in 0..3) {
                    val e = f.getBoundary(i)
                    if (e is BiDirectionalPlanarizationEdge) {
                        if (isGeneratedLayoutElement(e) && (e as PlanarizationEdge).isLayoutEnforcing()
                            && isContainerEdge(f.getBoundary(i + 1))
                            && isContainerEdge(f.getBoundary(i - 1))
                            && isConnectionEdge(f.getBoundary(i + 2))
                        ) {
                            val es = f.getCorner(i)
                            val c: Edge = f.getBoundary(i + 2)
                            val cs = f.getCorner(i + 2)
                            removalDone = performRemoval(pln, e, es, c, cs)
                            break
                        }
                    }
                }
            } else if (f.size() == 2 && !f.isOuterFace) {
                // case of 2 vertices directed against each other
                for (i in 0..1) {
                    val e = f.getBoundary(i)
                    if (e is BiDirectionalPlanarizationEdge) {
                        if (isGeneratedLayoutElement(e) && (e as PlanarizationEdge).isLayoutEnforcing()
                            && isConnectionEdge(f.getBoundary(i + 1))
                        ) {
                            val es = f.getCorner(i)
                            val c: Edge = f.getBoundary(i + 1)
                            val cs = f.getCorner(i + 1)
                            removalDone = performRemoval(pln, e, es, c, cs)
                            break
                        }
                    }
                }
            } else if (f.size() == 3 && !f.isOuterFace) {
                // case of one container, one dimensioned vertex directed against each other
                for (i in 0..2) {
                    val e = f.getBoundary(i)
                    if (isGeneratedLayoutElement(e) && e.isLayoutEnforcing() && hasDimensionedEnd(e)) {
                        if (isContainerEdge(f.getBoundary(i + 1)) && isConnectionEdge(f.getBoundary(i - 1))) {
                            removalDone = performRemoval(pln, f, e, i - 1, i)
                        } else if (isContainerEdge(f.getBoundary(i - 1)) && isConnectionEdge(f.getBoundary(i + 1))) {
                            removalDone = performRemoval(pln, f, e, i + 1, i)
                        }
                        break
                    }
                }
            }
            if (removalDone) {
                removalDone = false
            } else {
                currentFace++
            }
        }
    }

    private fun isGeneratedLayoutElement(e: PlanarizationEdge): Boolean {
        if (e.getDiagramElements().keys.size == 1) {
            for (de in e.getDiagramElements().keys) {
                if (de is GeneratedLayoutBiDirectional) {
                    return true
                }
            }
        }
        return false
    }

    private fun hasDimensionedEnd(e: Edge): Boolean {
        return e.getFrom().hasDimension() || e.getTo().hasDimension()
    }

    private fun performRemoval(
        pln: Planarization, f: Face, e: PlanarizationEdge, ci: Int,
        ei: Int
    ): Boolean {
        val removalDone: Boolean
        val es = f.getCorner(ei)
        val c: Edge = f.getBoundary(ci)
        val cs = f.getCorner(ci)
        removalDone = performRemoval(pln, e, es, c, cs)
        return removalDone
    }

    private fun performRemoval(
        pln: Planarization, e: PlanarizationEdge, es: Vertex, c: Edge,
        cs: Vertex
    ): Boolean {
        if (Tools.isUnderlyingContradicting(c)) {
            return false
        }
        val layoutDirection = e.getDrawDirectionFrom(es)
        val connectionDirection = c.getDrawDirectionFrom(cs)
        if (connectionDirection == null) {
            (c as PlanarizationEdge).setDrawDirectionFrom(reverse(layoutDirection), cs)
        }
        (c as PlanarizationEdge).setLayoutEnforcing(true)
        t.removeEdge(e, pln)
        return true
    }

    private fun isConnectionEdge(boundary: Edge): Boolean {
        return boundary is ConnectionEdge
    }

    private fun isContainerEdge(boundary: Edge): Boolean {
        return boundary is BorderEdge
    }
}