package org.kite9.diagram.visualization.orthogonalization.vertex

import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.*
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.edge.IncidentDart
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger.TurnInformation
import org.kite9.diagram.visualization.planarization.Tools
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge

/**
 * Creates darts for edges arriving at corners or sides of a planarization.
 *
 * It is expected that multiple edges could arrive to a single point in the same direction.  This should split those out into
 * some side-edge arrangement.
 *
 * @author robmoffat
 */
open class MultiElementVertexArranger(em: ElementMapper) : ConnectedVertexArranger(em) {
    
    /**
     * This exclude [EdgeCrossingVertex]s and grid elements.
     */
    override fun needsConversion(v: Vertex): Boolean {
        return if ((v is MultiCornerVertex && v.getDiagramElements().size == 1)
            || (v is EdgeCrossingVertex && v.getDiagramElements().size == 1)
            || v is ContainerSideVertex
        ) {
            true
        } else {
            super.needsConversion(v)
        }
    }

    override fun convertVertex(o: Orthogonalization, v: Vertex, ti: TurnInformation): DartFace? {
        return if (v is MultiElementVertex) {
            val dartDirections: Map<Direction, List<IncidentDart>> =
                getDartsInDirection(v, o, ti)
            val allIncidentDarts: List<IncidentDart> = convertToDartList(dartDirections)
            createBorderEdges(allIncidentDarts, dartDirections, o, v)
            log.send("Converting vertex: $v", allIncidentDarts)
            setupBoundariesFromIncidentDarts(allIncidentDarts, v)
            null
        } else {
            super.convertVertex(o, v, ti)
        }
    }

    private fun createBorderEdges(
        dartDirections: List<IncidentDart>,
        map: Map<Direction, List<IncidentDart>>,
        o: Orthogonalization,
        v: MultiElementVertex
    ) {
        // create sides for any sides that have > 1 incident darts
        val (a, b) = identifyBorders(dartDirections)
        val inDirection = reverse(a.arrivalSide)
        val outDirection = b.arrivalSide
        if (inDirection === outDirection) {
            // single side
            if (a.internal != b.internal) {
                var sideDirection: Direction? = rotateClockwise(inDirection)
                val aUnderlying = (a.dueTo as BorderEdge).getDiagramElements() as Map<DiagramElement, Direction>
                val bUnderlying = (b.dueTo as BorderEdge).getDiagramElements() as Map<DiagramElement, Direction>
                if (aUnderlying != bUnderlying || aUnderlying == null) {
                    throw LogicException()
                }
                var dartsToUse = map[sideDirection]!!
                if (dartsToUse.size == 0) {
                    sideDirection = reverse(sideDirection)
                    dartsToUse = map[sideDirection]!!
                }
                createSide(a.internal, b.internal, dartsToUse, o, inDirection, aUnderlying)
            }
        } else {
            val midPoint: Vertex = DartJunctionVertex("mcv-" + newVertexId++, v.getDiagramElements())
            val aSide = reverse(outDirection)
            val aUnderlying = (a.dueTo as BorderEdge).getElementForSide(aSide!!)
            val bUnderlying = (b.dueTo as BorderEdge).getElementForSide(inDirection!!)
            if (aUnderlying !== bUnderlying) {
                throw LogicException()
            }
            val aUnderlyings = a.dueTo.getDiagramElements() as Map<DiagramElement, Direction>
            val bUnderlyings = b.dueTo.getDiagramElements() as Map<DiagramElement, Direction>
            val side1Darts = map[aSide]!!
            val side2Darts = map[inDirection]!!
            createSide(a.internal, midPoint, side1Darts, o, inDirection, aUnderlyings)
            createSide(midPoint, b.internal, side2Darts, o, outDirection, bUnderlyings)
        }
    }

    private fun identifyBorders(dartDirections: List<IncidentDart>): OPair<IncidentDart> {
        // there should be two IncidentDarts next to each other that represent the edge of the container
        val borders = dartDirections.filter { d -> d.dueTo is BorderEdge }

        if (borders.size != 2) {
            throw LogicException()
        }
        val to = borders[0]
        val from = borders[1]
        val idx1 = dartDirections.indexOf(to)
        val idx2 = dartDirections.indexOf(from)
        return if (idx2 - idx1 == 1) {
            OPair(from, to)
        } else if (idx1 == 0 && idx2 == dartDirections.size - 1) {
            OPair(to, from)
        } else {
            throw LogicException()
        }
    }

    private fun createPortTreeExternalVertex(
        e: PortVertex,
        list: List<PlanarizationEdge>
    ): ExternalVertex {
        return ExternalVertex(e.getID() + "-pve" + newVertexId++, list.toSet(), true)
    }

    override fun handleEdgeBucketing(
        ti: TurnInformation,
        cd: Set<DiagramElement>,
        o: Orthogonalization,
        from: Vertex,
        list: List<PlanarizationEdge>
    ): List<IncidentDart> {
        var incidentDirection : Direction = Direction.DOWN
        if ((from is PortVertex) && (list.size > 1)) {
            // in this case we need to _not_ separate out elements into different side vertices.
            var sideVertex = createSideVertex(cd, from)
            val portTreeVertex: Vertex = createPortTreeExternalVertex(from, list)

            val outList = mutableListOf<IncidentDart>()
            var lastStartVertex : Vertex? = null
            val midPoint : Float = calculateMidPoint(list)
            for (i in list.indices) {
                val current = list[i]
                incidentDirection = ti.getIncidentDartDirection(current)
                var startVertex = if (i.toFloat() == midPoint) portTreeVertex else createExternalVertex(current, from)
                var id = ec.convertPlanarizationEdge(current, o, incidentDirection, createExternalVertex(current, from), startVertex, from, null)

                outList.add(id)

                if (lastStartVertex != null) {
                    val subList = if ( i <= midPoint) {
                        list.subList(0, i)
                    } else {
                        list.subList(i, list.size)
                    }

                    val underlyings = subList
                        .flatMap { it.getDiagramElements().keys }
                        .map { it to null }
                        .toMap()

                    o.createDart(lastStartVertex, startVertex, underlyings, Direction.rotateAntiClockwise(incidentDirection))
                }

                lastStartVertex = startVertex

                if ((i < midPoint) && (i + 1 > midPoint)) {
                    // join fork up with portTreeVertex
                    val subList = list.subList(0, i+1)
                    val underlyings = subList
                        .flatMap { it.getDiagramElements().keys }
                        .map { it to null }
                        .toMap()

                    o.createDart(startVertex, portTreeVertex, underlyings, Direction.rotateAntiClockwise(incidentDirection))
                    lastStartVertex = portTreeVertex
                }
            }

            // join the portTree to the side
            o.createDart(portTreeVertex, sideVertex,
                list
                    .flatMap { it.getDiagramElements().keys }
                    .map { it to null}
                    .toMap(),
                incidentDirection
            )

            // rewrite incident darts using side
            return outList.map {
                IncidentDart(it.external, sideVertex, it.arrivalSide, it.dueTo)
            }
        } else {
            return super.handleEdgeBucketing(ti, cd, o, from, list)
        }
    }

    private fun calculateMidPoint(list: List<PlanarizationEdge>) : Float {
        // if we have a straight edge, use that
        val straightEdges = list
            .filter {
                it.isStraightInPlanarization() &&
                !Tools.isUnderlyingContradicting(it)
            }

        if (straightEdges.size > 1) {
            throw LogicException("Should only be one non-contradicting straight edge leaving a port: $straightEdges")
        } else if (straightEdges.isEmpty()) {
            return (list.size - 1.0f) / 2.0f
        } else {
            return list.indexOf(straightEdges.first()).toFloat()
        }
    }

}