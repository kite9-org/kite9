package org.kite9.diagram.visualization.planarization.mgt

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.AbstractAnchoringVertex
import org.kite9.diagram.common.elements.vertex.ContainerSideVertex
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.isMax
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.isMin
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.Tools
import org.kite9.diagram.visualization.planarization.ordering.BasicVertexEdgeOrdering
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering
import org.kite9.diagram.visualization.planarization.transform.PlanarizationTransform

/**
 * Edges connecting to a container can connect to either a container corner vertex or
 * a container side vertex (or a vertex within the container == to be deprecated)
 *
 * Where multiple edges connect to a container vertex, these need to be split out so that they connect
 * to individual vertices around the perimeter of the container.  This means we have to add some new perimeter
 * vertices and move the edges onto those.
 *
 * This transform is necessary when you allow connections to containers.  This performs the transform on DIRECTED, NOT CONTRADICTING
 * edges only.  ContainerCornerVertexArranger is used to perform the transform on undirected edges, as these
 * need to be transformed after orthogonalization.
 *
 * @author robmoffat
 */
class ContainerConnectionTransform2(elementMapper: ElementMapper?) : PlanarizationTransform, Logable {

    private val log = Kite9Log(this)

    override fun transform(pln: Planarization) {
        createContainerEdgeVertices(pln)
    }

    var t = Tools()
    private fun createContainerEdgeVertices(pln: Planarization) {
        var number = 0
        for (v in (pln as MGTPlanarization).vertexOrder) {
            if (v is MultiCornerVertex && sideOrd(v) && v.getEdgeCount() > 3) {
                val edgeDirectionToSplit = getDirectionForSideVertex(v)
                if (edgeDirectionToSplit != null) {
                    val startContainerEdgeDirection = rotateAntiClockwise(edgeDirectionToSplit)
                    number = splitEdgesGoing(edgeDirectionToSplit, startContainerEdgeDirection, true, v, pln, number)
                }
            } else if (v is MultiCornerVertex && v.getEdgeCount() >= 3) {
                val cv = v
                val ymin = isMin(cv.yOrdinal)
                val xmin = isMin(cv.xOrdinal)
                val ymax = isMax(cv.yOrdinal)
                val xmax = isMax(cv.xOrdinal)
                if (xmin && cornerOrd(cv.yOrdinal)) {
                    number = splitEdgesGoing(
                        Direction.LEFT, if (ymin) Direction.DOWN else Direction.UP,
                        ymin, cv, pln, number
                    )
                }
                if (xmax && cornerOrd(cv.yOrdinal)) {
                    number = splitEdgesGoing(
                        Direction.RIGHT, if (ymin) Direction.DOWN else Direction.UP,
                        ymax, cv, pln, number
                    )
                }
                if (ymin && cornerOrd(cv.xOrdinal)) {
                    number = splitEdgesGoing(
                        Direction.UP, if (xmin) Direction.RIGHT else Direction.LEFT,
                        xmax, cv, pln, number
                    )
                }
                if (ymax && cornerOrd(cv.xOrdinal)) {
                    number = splitEdgesGoing(
                        Direction.DOWN, if (xmin) Direction.RIGHT else Direction.LEFT,
                        xmin, cv, pln, number
                    )
                }
            }
        }
    }

    private fun getDirectionForSideVertex(v: MultiCornerVertex): Direction? {
        val xOrd = v.xOrdinal
        if (isMin(xOrd)) {
            return Direction.LEFT
        } else if (isMax(xOrd)) {
            return Direction.RIGHT
        }
        val yOrd = v.yOrdinal
        if (isMin(yOrd)) {
            return Direction.UP
        } else if (isMax(yOrd)) {
            return Direction.DOWN
        }
        return null // container vertex embedded within a grid
    }

    private fun sideOrd(v: MultiCornerVertex): Boolean {
        return !cornerOrd(v.xOrdinal) || !cornerOrd(v.yOrdinal)
    }

    private fun cornerOrd(ord: LongFraction): Boolean {
        return isMin(ord) || isMax(ord)
    }

    private fun splitEdgesGoing(
        edgeDirectionToSplit: Direction,
        startContainerEdgeDirection: Direction,
        turnClockwise: Boolean,
        v: MultiCornerVertex,
        pln: Planarization,
        n: Int
    ): Int {
        // find out the starting point for the turn, and how many edges go in the direction we want to split
        var n = n
        log.send("Fixing edges around vertex: $v going $edgeDirectionToSplit")
        val eo = pln.edgeOrderings[v] as VertexEdgeOrdering?
        val originalOrder = eo!!.getEdgesAsList()
        var startPoint = 0
        var edgesRequiringSplit = 0
        var done = 0
        var i = 0
        while (done < originalOrder.size) {
            done++
            val edge = getRot(originalOrder, i)
            if (edge is BorderEdge && getUsedEdgeDirection(v, edge) === startContainerEdgeDirection) {
                startPoint = i
            } else if (getUsedEdgeDirection(v, edge) === edgeDirectionToSplit) {
                edgesRequiringSplit++
            }
            i = i + if (turnClockwise) 1 else -1
        }
        var receivingEdge = getRot(originalOrder, startPoint)
        if (edgesRequiringSplit > 0) {
            var edgesDoneSplit = 0
            var i = startPoint + if (turnClockwise) 1 else -1
            while (edgesDoneSplit < edgesRequiringSplit) {
                val edgeMoving = getRot(originalOrder, i)
                if (getUsedEdgeDirection(v, edgeMoving) === edgeDirectionToSplit) {
                    edgesDoneSplit++
                }
                receivingEdge = splitEdgeFromVertex(
                    v.getID() + "-" + edgeDirectionToSplit + edgesDoneSplit + n++,
                    v,
                    pln,
                    receivingEdge,
                    edgeMoving,
                    getRot(originalOrder, i + if (turnClockwise) 1 else -1),
                    turnClockwise
                )
                i = i + if (turnClockwise) 1 else -1
            }
            log.send("Changed vertex $v order now: ", eo.getEdgesAsList())
        }
        return n
    }

    private fun getUsedEdgeDirection(v: MultiCornerVertex, edge: Edge): Direction? {
        if (edge is ConnectionEdge) {
            val und = edge.getOriginalUnderlying()
            if (und.getRenderingInformation().isContradicting) {
                return null
            }
        }
        return edge.getDrawDirectionFrom(v)
    }

    private fun getRot(originalOrder: List<PlanarizationEdge>, i: Int): PlanarizationEdge {
        return originalOrder[(i + originalOrder.size + originalOrder.size) % originalOrder.size]
    }

    /**
     * Splits the receivingEdge with a new vertex, and moves "mover" onto it.  "after" is the edge following mover in the current ordering
     */
    private fun splitEdgeFromVertex(
        vertexName: String,
        v: Vertex,
        pln: Planarization,
        receivingEdge: PlanarizationEdge,
        mover: PlanarizationEdge,
        after: PlanarizationEdge,
        clockwise: Boolean
    ): PlanarizationEdge {
        // ok, splitting time - create a new vertex for the 'next' edge
        var receivingEdge = receivingEdge
        val orig = v as MultiCornerVertex
        val newVertex = ContainerSideVertex(vertexName)
        orig.getAnchors().stream().forEach { a: AbstractAnchoringVertex.Anchor -> newVertex.addUnderlying(a.de) }
        t.breakEdge(receivingEdge, pln, newVertex)

        // need to move next to the new vertex
        if (mover.getFrom() === v) {
            mover.setFrom(newVertex)
            newVertex.addEdge(mover)
            v.removeEdge(mover)
        } else if (mover.getTo() === v) {
            mover.setTo(newVertex)
            newVertex.addEdge(mover)
            v.removeEdge(mover)
        } else {
            throw LogicException("Could not move next")
        }

        // fix the faces
        val newArc = getNewArc(newVertex, v)
        val oldArc = getOldArc(newVertex, newArc)
        val faces: MutableList<Face?>? = pln.edgeFaceMap[mover]
        val onFace = if (faces!![0]!!.contains(newArc)) faces[0]!! else faces[1]!!
        val offFace = if (!faces[0]!!.contains(newArc)) faces[0]!! else faces[1]!!
        val onList = onFace.edgesCopy
        val offList = if (offFace !== onFace) offFace.edgesCopy else onList
        onList.remove(newArc)
        insertBetween(offList, newArc, newVertex, mover, after, v, clockwise)
        onFace.reset(onList)
        if (offFace !== onFace) {
            offFace.reset(offList)
        }

        // fix edge face map.  new arc should not meet face shared between old arc and next
        val newArcFaces: MutableList<Face?>? = pln.edgeFaceMap[newArc]
        val ok = newArcFaces!!.remove(onFace)
        if (!ok) {
            throw LogicException("Setting up edge face map didn't work")
        }
        newArcFaces.add(offFace)

        // fix vertex face map
        t.updateVertexFaceMap(pln, v)
        t.fixVertexFaceMap(pln, onFace, onFace.cornerIterator(), false)
        t.fixVertexFaceMap(pln, offFace, offFace.cornerIterator(), false)


        // fix the vertex edge ordering for v
        val newOrder = pln.edgeOrderings[v] as VertexEdgeOrdering?
        newOrder!!.remove(mover)
        newOrder.replace(receivingEdge, if (newArc.meets(v)) newArc else oldArc)

        // fix vertex edge ordering for newVertex
        val eo = pln.edgeOrderings[newVertex]!! as BasicVertexEdgeOrdering
        val otherEndOrder: MutableList<PlanarizationEdge> = eo.getEdgesAsList()
        val oldArcI = otherEndOrder.indexOf(if (clockwise) oldArc else newArc)
        if (oldArcI == -1) {
            throw LogicException("Can't find old arc $oldArc")
        } else {
            otherEndOrder.add(oldArcI + 1 % 3, mover)
        }
        log.send("Order around new vertex $newVertex", otherEndOrder)

        // move on
        receivingEdge = newArc
        return receivingEdge
    }

    private fun insertBetween(
        offFace: MutableList<PlanarizationEdge>,
        newArc: PlanarizationEdge,
        newVertex: Vertex,
        incoming: PlanarizationEdge,
        outgoing: PlanarizationEdge,
        v: Vertex,
        clockwise: Boolean
    ) {
        for (i in offFace.indices) {
            val before: Edge = offFace[(i + offFace.size) % offFace.size]
            val afteri = (i + offFace.size + if (clockwise) -1 else 1) % offFace.size
            val after: Edge = getRot(offFace, afteri)
            if (before === incoming && after === outgoing) {
                if (clockwise) {
                    offFace.add(i, newArc)
                } else {
                    offFace.add(afteri, newArc)
                }
                return
            }
        }
        throw LogicException("Couldn't find point to insert in $offFace")
    }

    private fun getNewArc(newVertex: Vertex, v: Vertex): PlanarizationEdge {
        for (e in newVertex.getEdges()) {
            if (e.meets(v)) {
                return e as PlanarizationEdge
            }
        }
        throw LogicException("Could not find edge meeting $v")
    }

    private fun getOldArc(newVertex: Vertex, newArc: Edge): PlanarizationEdge {
        for (e in newVertex.getEdges()) {
            if (e !== newArc) {
                return e as PlanarizationEdge
            }
        }
        throw LogicException("Could not old arc")
    }

    override val prefix: String
        get() = "CET "
    override val isLoggingEnabled: Boolean
        get() = true
}