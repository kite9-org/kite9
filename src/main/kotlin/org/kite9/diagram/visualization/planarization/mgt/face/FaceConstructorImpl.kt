package org.kite9.diagram.visualization.planarization.mgt.face

import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization.vertexOrder
import org.kite9.diagram.visualization.planarization.Planarization.edgeFaceMap
import org.kite9.diagram.common.BiDirectional.getFrom
import org.kite9.diagram.common.BiDirectional.getTo
import org.kite9.diagram.visualization.planarization.Planarization.faces
import org.kite9.diagram.visualization.planarization.Face.checkFaceIntegrity
import org.kite9.diagram.visualization.planarization.Face.partOf
import org.kite9.diagram.logging.Kite9Log.send
import org.kite9.diagram.visualization.planarization.Face.getID
import org.kite9.diagram.visualization.planarization.Face.cornerIterator
import org.kite9.diagram.visualization.planarization.Face.edgeIterator
import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge.getFrom
import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge.getTo
import org.kite9.diagram.visualization.planarization.Face.indexOf
import org.kite9.diagram.visualization.planarization.Face.getBoundary
import org.kite9.diagram.common.BiDirectional.getDrawDirectionFrom
import org.kite9.diagram.visualization.planarization.mgt.face.TemporaryEdge.below
import org.kite9.diagram.visualization.planarization.mgt.face.TemporaryEdge.above
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge.getElementForSide
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.DiagramElement.getParent
import org.kite9.diagram.common.elements.vertex.Vertex.isLinkedDirectlyTo
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization.addEdge
import org.kite9.diagram.visualization.planarization.Planarization.createFace
import org.kite9.diagram.visualization.planarization.Face.add
import org.kite9.diagram.common.BiDirectional.otherEnd
import org.kite9.diagram.visualization.planarization.Planarization.vertexFaceMap
import org.kite9.diagram.visualization.planarization.Planarization.edgeOrderings
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering.getEdgesAsList
import org.kite9.diagram.common.BiDirectional.getDrawDirection
import org.kite9.diagram.visualization.planarization.Tools.removeEdge
import org.kite9.diagram.common.elements.vertex.Vertex.getEdges
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.removeBeforeOrthogonalization
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructor
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization
import org.kite9.diagram.visualization.planarization.mgt.face.TemporaryEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.model.Rectangular
import java.util.HashSet
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.planarization.Planarization
import java.util.LinkedList
import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.RemovalType
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.Tools
import java.util.ArrayList

/**
 * Turns the vertex-order planarization into a number of faces.  This is done by first joining everything in the diagram together, using temporary edges
 * then walking round the faces in an anti-clockwise direction.
 *
 * At this point, each face will have a temporary edge.  We can use this fact to determine which Rectangular each face is partOf, and set that.
 *
 * Then, removing the temporary edges inserted to make the diagram completely connected.
 *
 * @author robmoffat
 */
class FaceConstructorImpl : FaceConstructor, Logable {
    private val log = Kite9Log(this)
    override fun createFaces(pl: MGTPlanarization) {
        val temps = introduceTemporaryEdges(pl)
        traceFaces(pl)
        assignRectangulars1(temps, pl)
        removeTemporaries(pl)
        assignRectangulars2(pl)
    }

    private fun traceFaces(pl: MGTPlanarization) {
        // walk through nodes in turn
        for (v in pl.vertexOrder) {
            for (e in getEdgeOrdering(v, pl)) {
                // edges can only contribute to two faces, at most.
                // although they can contribute to the same face twice
                val map: List<Face?>? = pl.edgeFaceMap.get(e)
                if (map == null) {
                    tracePath(v, e, pl)
                } else if (map[0] == null) {
                    tracePath(e!!.getFrom(), e, pl)
                } else if (map[1] == null) {
                    tracePath(e!!.getTo(), e, pl)
                }
            }
        }
        for (face in pl.faces) {
            face.checkFaceIntegrity()
        }
    }

    /**
     * Works out the rectangular diagram element containing this face for *most* faces.
     *
     * This doesn't work if the face is completely surrounded by connections, we deal with that in assignRectangulars2
     */
    private fun assignRectangulars1(temps: List<TemporaryEdge>, pl: MGTPlanarization) {
        for (temporaryEdge in temps) {
            val faces: List<Face>? = pl.edgeFaceMap[temporaryEdge]
            for (f in faces!!) {
                if (f.partOf == null) {
                    val r = determineInsideElementFromTemporaryEdge(f, temporaryEdge, pl)
                    if (r != null) {
                        log.send("1. Face  " + f.getID() + " " + f.cornerIterator() + " is part of " + r)
                        f.partOf = r
                    }
                }
            }
        }
    }

    private fun assignRectangulars2(pl: MGTPlanarization) {
        for (f in pl.faces) {
            identifyRectangular(f, HashSet(), pl)
        }
    }

    private fun identifyRectangular(f: Face, visited: MutableSet<Face>, pl: MGTPlanarization): Rectangular? {
        return if (f.partOf == null) {
            visited.add(f)
            for (e in f.edgeIterator()) {
                if (e is BiDirectionalPlanarizationEdge) {
                    // the rectangular is the same on both sides
                    val faces: List<Face> = pl.edgeFaceMap[e]!!
                    for (face2 in faces) {
                        if (!visited.contains(face2)) {
                            val r = identifyRectangular(face2, visited, pl)
                            log.send("2. Face " + f.getID() + " " + f.cornerIterator() + " is part of " + r)
                            f.partOf = r
                            return r
                        }
                    }
                }
            }
            null
        } else {
            f.partOf
        }
    }

    private fun determineInsideElementFromTemporaryEdge(
        face: Face,
        leave: TemporaryEdge,
        pl: MGTPlanarization
    ): Rectangular? {
        val from = leave.getFrom()
        val to = leave.getTo()
        return if (face.indexOf(from, leave) > -1) {
            // face is below the planarization line
            val leaveStart = face.indexOf(from, leave)
            val next: Edge = face.getBoundary(leaveStart - 1)
            var out: Rectangular? = null
            if (next is BorderEdge) {
                val be = next
                out = when (next.getDrawDirectionFrom(from)) {
                    Direction.UP -> safeGetDiagramElement(
                        be,
                        Direction.RIGHT
                    )
                    Direction.DOWN -> safeGetDiagramElement(
                        be,
                        Direction.LEFT
                    )
                    Direction.LEFT -> safeGetDiagramElement(
                        be,
                        Direction.UP
                    )
                    Direction.RIGHT -> safeGetDiagramElement(
                        be,
                        Direction.DOWN
                    )
                    else -> throw LogicException()
                }
            }
            leave.below = out
            out
        } else if (face.indexOf(to, leave) > -1) {
            // above the planarization line
            val leaveStart = face.indexOf(to, leave)
            val next: Edge = face.getBoundary(leaveStart + 1)
            var out: Rectangular? = null
            if (next is BorderEdge) {
                val be = next
                out = when (next.getDrawDirectionFrom(from)) {
                    Direction.UP -> safeGetDiagramElement(
                        be,
                        Direction.LEFT
                    )
                    Direction.DOWN -> safeGetDiagramElement(
                        be,
                        Direction.RIGHT
                    )
                    Direction.LEFT -> safeGetDiagramElement(
                        be,
                        Direction.DOWN
                    )
                    Direction.RIGHT -> safeGetDiagramElement(
                        be,
                        Direction.UP
                    )
                    else -> throw LogicException()
                }
            }
            leave.above = out
            out
        } else {
            throw LogicException()
        }
    }

    private fun safeGetDiagramElement(be: BorderEdge, d: Direction): Rectangular? {
        var de = be.getElementForSide(d)
        if (de == null) {
            de = be.getElementForSide(reverse(d)!!)!!.getParent()
        }
        return de as Rectangular?
    }

    protected fun introduceTemporaryEdges(p: MGTPlanarization): List<TemporaryEdge> {
        val totalLength = p.vertexOrder.size
        val out: MutableList<TemporaryEdge> = ArrayList(totalLength + 1)
        for (pos in 0 until totalLength) {
            if (pos < totalLength - 1) {
                val v1 = p.vertexOrder[pos]
                val v2 = p.vertexOrder[pos + 1]
                if (!v1.isLinkedDirectlyTo(v2)) {
                    out.add(createTemporaryEdge(p, v1, v2))
                }
            }
        }
        return out
    }

    protected fun createTemporaryEdge(p: MGTPlanarization, from: Vertex?, to: Vertex?): TemporaryEdge {
        val e = TemporaryEdge(from!!, to!!)
        p.addEdge(e, true, null)
        return e
    }

    private fun tracePath(v: Vertex, e: PlanarizationEdge?, pl: Planarization): Face {
        var v = v
        var e = e
        val f = pl.createFace()
        //System.out.println("Creating face "+f);
        val startEdge = e
        val startVertex = v
        do {
            addToFaceMap(v, e, f, pl)
            f.add(v, e!!)
            //System.out.println("adding " + e);
            v = e.otherEnd(v)
            e = getLeftEdge(e, v, pl)
        } while (e !== startEdge || v !== startVertex)
        return f
    }

    private fun addToFaceMap(from: Vertex, e: Edge?, f: Face, pln: Planarization) {
        // edge face map
        var faces: MutableList<Face?>? = pln.edgeFaceMap.get(e)
        if (faces == null) {
            faces = ArrayList(2)
            pln.edgeFaceMap[e!!] = faces
            faces.add(null)
            faces.add(null)
        }
        if (from === e!!.getFrom()) {
            faces[0] = f
        } else if (from === e!!.getTo()) {
            faces[1] = f
        }

        // vertex face map
        faces = pln.vertexFaceMap[from]
        if (faces == null) {
            faces = LinkedList()
            pln.vertexFaceMap[from] = faces
        }
        faces.add(f)
    }

    private fun getLeftEdge(incident: PlanarizationEdge?, v: Vertex, pl: Planarization): PlanarizationEdge? {
        val ordering = getEdgeOrdering(v, pl)
        val startIndex = ordering.indexOf(incident)
        var index = startIndex
        index = if (index == 0) {
            ordering.size - 1
        } else {
            index - 1
        }
        var out: PlanarizationEdge? = null
        out = ordering[index]
        return out
    }

    private fun getEdgeOrdering(
        v: Vertex,
        pl: Planarization
    ): List<PlanarizationEdge?> {
        return pl.edgeOrderings[v]!!.getEdgesAsList()
    }

    private fun removeTemporaries(p: MGTPlanarization) {
        val toRemove: MutableSet<PlanarizationEdge> = DetHashSet()
        for (v in p.vertexOrder) {
            traverseAllLinks(v, toRemove)
        }
        if (toRemove.size > 0) {
            removeTemporaries(toRemove, p)
        }
    }

    var t = Tools()

    /**
     * Adds extra logic to say only remove the temporaries where they are not providing direction information for orth.
     * If they do, they must remain.
     */
    private fun removeTemporaries(toRemove: Set<PlanarizationEdge>, p: Planarization) {
        for (temporaryEdge in toRemove) {
            if (temporaryEdge.getDrawDirection() == null) {
                t.removeEdge(temporaryEdge, p)
            }
        }
    }

    private fun traverseAllLinks(vertex: Vertex, toRemove: MutableSet<PlanarizationEdge>) {
        for (edge in vertex.getEdges()) {
            if (edge is PlanarizationEdge
                && edge.removeBeforeOrthogonalization() === RemovalType.YES
            ) {
                //System.out.println("Removing edge: "+edge);
                toRemove.add(edge)
            }
        }
    }

    override val prefix: String
        get() = "FC  "
    override val isLoggingEnabled: Boolean
        get() = true
}