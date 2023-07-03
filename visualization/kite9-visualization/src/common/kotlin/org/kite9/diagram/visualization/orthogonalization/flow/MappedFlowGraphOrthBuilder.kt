package org.kite9.diagram.visualization.orthogonalization.flow

import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.vertex.ConnectedRectangularVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.orthogonalization.ConnectionEdgeBendVertex
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.OrthogonalizationImpl
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer.Companion.createEdgeVertex
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer.Companion.createFaceVertex
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer.Companion.isConstrained
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger.TurnInformation
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization
import kotlin.math.abs

/**
 * This creates an orthogonalisation using the definition contained in the flow
 * graph.
 *
 * @author robmoffat
 */
class MappedFlowGraphOrthBuilder(
    private val va: VertexArranger,
    private val fg: MappedFlowGraph,
    private val clc: EdgeConverter
) : Logable, OrthBuilder {

    private val log = Kite9Log.instance(this)
    private val turnInfoMap: MutableMap<Vertex, TurnInformation>

    internal enum class StartPointType {
        DIRECTED, INFERRED, NONE
    }

    internal data class StartPoint(
        val e: Edge,
        val v: Vertex,
        val d: Direction,
        val type: StartPointType,
        val f: Face,
        val tolerance : Double = 0.0
    ) : Comparable<StartPoint> {

        override fun compareTo(o: StartPoint): Int {
            return if (type.ordinal != o.type.ordinal) {
                type.ordinal.compareTo(o.type.ordinal)
            } else if (type == StartPointType.INFERRED) {
                tolerance.compareTo(o.tolerance)
            } else {
                0
            }
        }

        override fun toString(): String {
            return "f=" + f!!.getID() + ",t=" + type + ",s=" + tolerance + ",d=" + d
        }
    }

    override fun build(pln: Planarization): OrthogonalizationImpl {
        val o = OrthogonalizationImpl(pln)
        val doneFaces: MutableMap<Face?, DartFace?> = HashMap()
        val doneEdges: MutableMap<Edge, MutableList<Vertex?>> = HashMap()
        val startPoints = selectBestStartPoints(pln)
        for (startPoint in startPoints) {
            if (!doneFaces.containsKey(startPoint.f)) {
                log.send("Processing face: $startPoint")
                processFace(
                    startPoint.f,
                    pln,
                    o,
                    startPoint.d,
                    doneFaces,
                    startPoint.v,
                    startPoint.e as PlanarizationEdge?,
                    doneEdges
                )
            }
        }

        // handle single-vertex faces
        for (f in pln.faces) {
            val container = f.containedBy
            val dfContainer = if (container != null) doneFaces[container] else null
            if (!doneFaces.containsKey(f)) {
                // single-vertex face
                val corner = f.getCorner(0) as ConnectedRectangularVertex
                val innerFaceDarts = va.returnAllDarts(corner, o)
                val topLeft = innerFaceDarts[0].dart.getFrom()
                val df = va.convertToOuterFace(o, topLeft, corner.getOriginalUnderlying())
                df.setContainedBy(dfContainer)
            } else if (f.isOuterFace) {
                val df = doneFaces[f]
                df!!.setContainedBy(dfContainer)
            }
        }
        return o
    }

    private fun selectBestStartPoints(pln: Planarization): List<StartPoint> {
        // figure out somewhere to starts
        val out: MutableList<StartPoint> = ArrayList(pln.faces.size)

        // scans through the faces, stops when it finds a suitable directed edge.
        for (f in pln.faces) {
            var best: StartPoint? = null
            for (index in 0 until f.edgeCount()) {
                val e = f.getBoundary(index)
                val v = f.getCorner(index)
                var d = e.getDrawDirectionFrom(v)
                var type: StartPointType = StartPointType.NONE
                var tolerance = 0.0
                if (isConstrained((e as PlanarizationEdge?)!!)) {
                    type = StartPointType.DIRECTED
                } else {
                    d = Direction.RIGHT // in case nothing else gets set
                    val sri = v.routingInfo
                    val eri = e.otherEnd(v).routingInfo
                    if (sri != null && eri != null) {
                        if (sri.centerX() < eri.centerX()) {
                            tolerance = abs(sri.centerY() - eri.centerY())
                            type = StartPointType.INFERRED
                            d = Direction.RIGHT
                        } else {
                            tolerance = abs(sri.centerY() - eri.centerY())
                            type = StartPointType.INFERRED
                            d = Direction.LEFT
                        }
                    }
                }
                val sp = StartPoint(e,v,d!!, type, f, tolerance)
                if (best == null || best.compareTo(sp) == 1) {
                    best = sp
                }
            }
            if (best != null) out.add(best)
        }
        out.sort()
        return out
    }

    internal data class SingleVertexTurnInformation(
        val map: Map<Edge, Direction?>,
        val start: Edge?,
        val turns: Map<Edge, Boolean>
    ) : TurnInformation {

        override fun getIncidentDartDirection(e: Edge): Direction {
            return map[e]!!
        }

        override fun toString(): String {
            return "SingleVertexTurnInformation [map=$map]"
        }

        override fun getFirstEdgeClockwiseEdgeOnASide(): Edge? {
            return start
        }

        override fun doesEdgeHaveTurns(e: Edge): Boolean {
            return turns[e]!!
        }
    }

    /**
     * Given an incident edge, and a known direction, and vertex, works out the exit directions for all edges
     * surrounding the vertex.
     */
    private fun getTurnInformationFor(e1: Edge, sv: Vertex, d: Direction?, pln: Planarization): TurnInformation {
        var e1 = e1
        var d = d
        var out = turnInfoMap[sv]
        if (out != null) {
            return out
        }
        val directionMap: MutableMap<Edge, Direction?> = HashMap()
        val turnsMap: MutableMap<Edge, Boolean> = HashMap()
        val faces: List<Face>? = pln.vertexFaceMap[sv]
        val eo = pln.edgeOrderings[sv]
        val it = eo!!.getIterator(true, (e1 as PlanarizationEdge), e1, false)
        var startEdge: Edge? = null
        while (it.hasNext()) {
            val e2 = it.next()
            if (e2 === e1) {
                directionMap[e1] = d
            } else {
                val f = getCorrectFace(faces, sv, e1, e2)
                val outCap = calculateTurns(f, fg, sv, e2, e1)
                for (i in 0 until abs(outCap)) {
                    d = rotate90(d, outCap)
                }
                if (outCap != -2) {
                    startEdge = e2
                }
                d = reverse(d)
                directionMap[e2] = d
            }
            e1 = e2

            // now calculate turn map
            val faces2: List<Face?> = pln.edgeFaceMap[e2]!!
            val edgeBends = calculateEdgeBends(faces2[0]!!, fg, e2, sv)
            turnsMap[e2] = edgeBends != 0
        }
        out = SingleVertexTurnInformation(directionMap, startEdge, turnsMap)
        turnInfoMap[sv] = out
        return out
    }

    private fun getCorrectFace(faces: List<Face>?, v: Vertex, following: Edge, before: Edge): Face {
        for (face in faces!!) {
            if (face.indexOf(v, following) > -1
                && face.contains(before)
            ) {
                return face
            }
        }
        throw LogicException()
    }

    private fun processFace(
        f: Face?,
        pln: Planarization,
        o: OrthogonalizationImpl,
        processingEdgeStartDirection: Direction?,
        doneFaces: MutableMap<Face?, DartFace?>,
        start: Vertex?,
        leaving: PlanarizationEdge?,
        doneEdges: MutableMap<Edge, MutableList<Vertex?>>
    ) {
        var processingEdgeStartDirection = processingEdgeStartDirection
        if (doneFaces.containsKey(f)) {
            return
        }
        log.send(if (log.go()) null else "Processing face: " + f!!.getID())
        doneFaces[f] = null
        val dartsInFace: MutableList<DartDirection> = ArrayList()
        val startIndex = f!!.indexOf(start!!, leaving!!)
        var processingEdgeDartFromVertex: Vertex? = null
        var processingEdgeDartToVertex: Vertex? = null
        for (i in 0 until f.size() + 1) {
            // should be lastVertex - processingEdge - processingVertex - nextEdge
            val ei = startIndex + i
            val lastVertex = f.getCorner(ei)
            val processingEdge = f.getBoundary(ei)
            val processingVertex = f.getCorner(ei + 1)
            val nextEdge = f.getBoundary(ei + 1)
            val edgeBends = calculateEdgeBends(f, fg, processingEdge, lastVertex)
            val processingEdgeEndDirection = turn(processingEdgeStartDirection, edgeBends)
            val ti = getTurnInformationFor(processingEdge, processingVertex, processingEdgeEndDirection, pln)
            val nextEdgeStartDirection = reverse(ti.getIncidentDartDirection(nextEdge))
            var vertexDarts: List<DartDirection>
            if (va.needsConversion(processingVertex)) {
                vertexDarts =
                    va.returnDartsBetween(processingEdge, nextEdgeStartDirection!!, processingVertex, nextEdge, o, ti)
                processingEdgeDartToVertex = firstVertex(vertexDarts)
            } else {
                vertexDarts = emptyList()
                processingEdgeDartToVertex = processingVertex
            }
            if (i > 0) {
                // process the edge - we need to do this after processing the first vertex 
                val created = processEdge(
                    f,
                    pln,
                    o,
                    fg,
                    processingEdgeStartDirection,
                    processingEdgeDartFromVertex,
                    processingEdgeDartToVertex,
                    processingEdge,
                    doneFaces,
                    doneEdges,
                    lastVertex,
                    processingVertex,
                    edgeBends
                )
                dartsInFace.addAll(created)
                processingEdgeStartDirection = created[created.size - 1].direction
            }
            if (i < f.size()) {
                processingEdgeDartFromVertex = if (va.needsConversion(processingVertex)) {
                    lastVertex(vertexDarts)
                } else {
                    processingVertex
                }
                // prevents the vertex from being processed twice
                dartsInFace.addAll(vertexDarts)
            }


            // set for next round
            processingEdgeStartDirection = reverse(ti.getIncidentDartDirection(nextEdge))
            log.send("Face  " + f.getID() + " darts so far: " + dartsInFace)
        }
        val df = o.createDartFace(f.partOf, f.isOuterFace, dartsInFace)
        doneFaces[f] = df
        log.send(if (log.go()) null else "Done face: " + f.getID() + " " + df.dartsInFace)
    }

    private fun turn(d: Direction?, edgeBends: Int): Direction? {
        var d = d
        for (i in 0 until abs(edgeBends)) {
            d = rotate90(d, edgeBends)
        }
        return d
    }

    private fun firstVertex(toBefore: List<DartDirection>): Vertex {
        val d = toBefore[0].dart
        return if (d.getDrawDirection() === toBefore[0].direction) {
            d.getFrom()
        } else {
            d.getTo()
        }
    }

    private fun lastVertex(toBefore: List<DartDirection>): Vertex {
        val (d, direction) = toBefore[toBefore.size - 1]
        return if (d.getDrawDirection() === direction) {
            d.getTo()
        } else {
            d.getFrom()
        }
    }

    fun getFaceToVertexNode(fg: MappedFlowGraph, from: Face?, to: Vertex?, before: Edge?, after: Edge?): Node? {
        return fg.getNodeFor(
            createFaceVertex(
                from,
                to!!,
                (before as PlanarizationEdge?)!!,
                (after as PlanarizationEdge?)!!
            )
        )
    }

    /**
     * Returns the number of turns in this face for incoming last, outgoing next at vertex ev.
     * Positive numbers mean clockwise turns, negative mean anti-clockwise.
     *
     * The output number can vary between -2 and 2 inclusive.
     */
    private fun calculateTurns(f: Face, fg: MappedFlowGraph, ev: Vertex, last: Edge, next: Edge): Int {
        val helperNode = getFaceToVertexNode(fg, f, ev, last, next)
        var cap = 0
        var outCap = 0
        for (a in helperNode!!.arcs) {
            // helper node is always 'To' node
            if (a.from.type === AbstractFlowOrthogonalizer.PORTION_NODE) {
                outCap -= a.flow
            } else if (a.from.type === AbstractFlowOrthogonalizer.VERTEX_NODE) {
                cap -= a.flow
            } else {
                throw LogicException("This should be a face or vertex arc")
            }
        }
        if (cap + outCap != helperNode.supply) {
            throw LogicException("No parity between incoming and outgoing helper arcs")
        }
        if (outCap > 2 || outCap < -2) {
            throw LogicException("some strange maths: $outCap $cap")
        }
        log.send(if (log.go()) null else "turn on $ev is $outCap due to $helperNode")
        return outCap
    }

    private fun processEdge(
        f: Face,
        pln: Planarization,
        o: OrthogonalizationImpl,
        fg: MappedFlowGraph,
        nextDir: Direction?,
        startVertex: Vertex?,
        endVertex: Vertex?,
        e: PlanarizationEdge,
        doneFaces: MutableMap<Face?, DartFace?>,
        doneEdges: MutableMap<Edge, MutableList<Vertex?>>,
        startPlanVertex: Vertex,
        endPlanVertex: Vertex,
        arcCost: Int
    ): List<DartDirection> {
        var nextDir = nextDir
        log.send(if (log.go()) null else "Processing edge $e from: $startVertex to $endVertex in direction $nextDir")
        if (e.otherEnd(startPlanVertex) !== endPlanVertex) {
            throw LogicException("We have a problem")
        }
        var waypoints = doneEdges[e]
        val wpCount = abs(arcCost) + 1
        if (waypoints == null) {
            waypoints = ArrayList(wpCount)
            doneEdges[e] = waypoints
            waypoints.add(startVertex)
            for (i in 0 until wpCount - 1) {
                val bv = ConnectionEdgeBendVertex(
                    startVertex!!.getID() + "-" + i + "-" + endVertex!!.getID(),
                    (e as ConnectionEdge)
                )
                waypoints.add(bv)
            }
            waypoints.add(endVertex)
        } else if (waypoints[0] === endVertex) {
            waypoints = ArrayList(waypoints)
            waypoints.reverse()
        } else if (waypoints[0] !== startVertex) {
            throw LogicException("Waypoints don't match vertices")
        }
        val out: MutableList<DartDirection> = ArrayList()
        for (i in 0 until waypoints.size - 1) {
            val start = waypoints[i]
            val end = waypoints[i + 1]
            createEdgePart(o, nextDir, start!!, end, e.getDiagramElements(), out)
            if (i < wpCount - 1) {
                // rotate ready for next dart.
                nextDir = rotate90(nextDir, arcCost)
            }
        }
        val opposite = reverse(nextDir)
        val outerFace = getOppositeFace(e, f, pln)
        processFace(outerFace, pln, o, opposite, doneFaces, endPlanVertex, e, doneEdges)
        return out
    }

    private fun createEdgePart(
        o: Orthogonalization,
        direction: Direction?,
        start: Vertex,
        end: Vertex?,
        underlyings: Map<DiagramElement, Direction?>,
        out: MutableList<DartDirection>
    ) {
        var start = start
        val darts = clc.buildDartsBetweenVertices(underlyings, o, start, end!!, direction!!)

        // convert to dart directions
        for (d in darts) {
            out.add(DartDirection(d, d.getDrawDirectionFrom(start)!!))
            start = d.otherEnd(start)
        }
    }

    fun getEdgeVertexNode(fg: MappedFlowGraph, e: Edge?, v: Vertex?): Node? {
        return fg.getNodeFor(createEdgeVertex(e as PlanarizationEdge?, v))
    }

    /**
     * Returns the number of bends in the edge, where positive is concave bends,
     * and negative is convex, wrt to the face.
     *
     * @param endVertex
     * @param startVertex
     */
    private fun calculateEdgeBends(f: Face?, fg: MappedFlowGraph, e: Edge, startVertex: Vertex): Int {
        val edgeNodes: MutableList<Node> = ArrayList()
        val a = getEdgeVertexNode(fg, e, e.getFrom())
        val b = getEdgeVertexNode(fg, e, e.getTo())
        val c = getEdgeVertexNode(fg, e, null)
        if (a != null) edgeNodes.add(a)
        if (b != null) edgeNodes.add(b)
        if (c != null) edgeNodes.add(c)
        val ports = fg.getNodesForEdgePart(
            f!!, e, startVertex
        )

        // we are now going to sum up arcs leading to and from the edge to
        // figure out how many turns it has
        var arcConcaveCost = 0 // these are flow units pushed into the face
        var arcConvexCost = 0 // these are flow units pushed out of the Face
        for (edgeNode in edgeNodes) {
            for (r in edgeNode.arcs) {
                val faceEndNode = r.otherEnd(edgeNode)
                val out = if (r.from === edgeNode) true else false
                // we only care about nodes leading to the current portion
                if (ports.contains(faceEndNode)) {
                    if (out) {
                        arcConvexCost += r.flow
                    } else {
                        arcConcaveCost += r.flow
                    }
                }
            }
        }
        // if ((arcConcaveCost>0) && (arcConvexCost>0)) {
        // throw new LogicException("Don't know what to do here");
        // }
        val arcCost = arcConcaveCost - arcConvexCost
        log.send(if (log.go()) null else e.toString() + " " + f.getID() + (if (f.isOuterFace) "outer" else "inner") + " cost " + arcCost)
        return arcCost
    }

    private fun rotate90(d: Direction?, cost: Int): Direction? {
        var d = d
        if (cost > 0) {
            d = rotateClockwise(d!!)
        } else if (cost < 0) {
            d = rotateAntiClockwise(d!!)
        }
        return d
    }

    override val prefix: String
        get() = "FLOW"
    override val isLoggingEnabled: Boolean
        get() = true

    companion object {
        fun getOppositeFace(e: Edge, f: Face, pln: Planarization): Face {
            val faces: List<Face?> = pln.edgeFaceMap.get(e)!!
            if (faces.size != 2) {
                throw LogicException("Edge should only have 2 faces")
            }
            return if (faces[0]!! === f) {
                faces[1]!!
            } else if (faces[1]!! === f) {
                faces[0]!!
            } else {
                throw LogicException("Face map should contain f")
            }
        }
    }

    init {
        turnInfoMap = HashMap()
    }
}