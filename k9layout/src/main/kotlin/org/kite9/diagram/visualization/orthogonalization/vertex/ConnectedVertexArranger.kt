package org.kite9.diagram.visualization.orthogonalization.vertex

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.ConnectedRectangularVertex
import org.kite9.diagram.common.elements.vertex.DartJunctionVertex
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter
import org.kite9.diagram.visualization.orthogonalization.edge.FanningEdgeConverter
import org.kite9.diagram.visualization.orthogonalization.edge.IncidentDart
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger.TurnInformation
import kotlin.math.ceil
import kotlin.math.floor

/**
 * This converts a ConnectedVertex to a face, and darts, which can then be added to the Orthogonalization.
 *
 * @author robmoffat
 */
open class ConnectedVertexArranger(em: ElementMapper) : AbstractVertexArranger(em) {

	val gp: GridPositioner = em.getGridPositioner()

	val ec: EdgeConverter by lazy {
       FanningEdgeConverter(this, em)
    }

    fun getContainerLabelConverter() : EdgeConverter {
        return ec;
    }

    /**
     * This implementation handles ConnectedVertex implementations embedded in the planarization.
     */
    override fun convertVertex(o: Orthogonalization, v: Vertex, ti: TurnInformation): DartFace? {
        if (v !is ConnectedRectangularVertex) throw LogicException()
        val dartDirections = getDartsInDirection(v, o, ti)
        val originalUnderlying = v.getOriginalUnderlying()
        val out = convertDiagramElementToInnerFace(originalUnderlying, o, dartDirections)
        val allIncidentDarts = convertToDartList(dartDirections)
        log.send("Converting vertex: $v", allIncidentDarts)
        setupBoundariesFromIncidentDarts(allIncidentDarts, v)
        return out
    }

    protected fun convertToDartList(dartDirections: Map<Direction, List<IncidentDart>>): List<IncidentDart> {
        val out: MutableList<IncidentDart> = ArrayList()
        out.addAll(dartDirections[Direction.UP]!!)
        out.addAll(dartDirections[Direction.RIGHT]!!)
        out.addAll(dartDirections[Direction.DOWN]!!)
        out.addAll(dartDirections[Direction.LEFT]!!)
        return out
    }

    override fun needsConversion(v: Vertex): Boolean {
        return v is ConnectedRectangularVertex
    }

    override fun returnAllDarts(v: Vertex, o: Orthogonalization): List<DartDirection> {
        if (v !is ConnectedRectangularVertex) throw LogicException()
        val c = v.getOriginalUnderlying()
        val faces = o.getDartFacesForRectangular(c)
        return if (faces.size == 1) {
            faces[0].dartsInFace
        } else if (faces.size > 1) {
            throw LogicException()
        } else {
            val out = convertVertex(o, v, object : TurnInformation {
                override fun getIncidentDartDirection(e: Edge): Direction {
                    throw LogicException("argh")
                }

                override fun getFirstEdgeClockwiseEdgeOnASide(): Edge? {
                    return null
                }

                override fun doesEdgeHaveTurns(e: Edge): Boolean {
                    return false
                }
            })
            out!!.dartsInFace
        }
    }

    protected fun setupBoundariesFromIncidentDarts(dartOrdering: List<IncidentDart?>, v: Vertex) {
        val externalVertices : Set<Vertex> = dartOrdering
            .map { id: IncidentDart? -> id!!.external }
            .toSet()
        setupBoundaries(externalVertices, v)
    }

    /**
     * Creates a dart or darts that arrives at the vertex.  Where more than one dart is created, return just the dart hitting
     * the vertex.
     */
    private fun convertEdgeToIncidentDart(
        e: PlanarizationEdge,
        cd: Set<DiagramElement>,
        o: Orthogonalization,
        incident: Direction,
        idx: Int,
        und: Vertex,
        straightCount: Int
    ): IncidentDart {
        val sideVertex = createSideVertex(cd, und)
        val externalVertex: Vertex = createExternalVertex(e, und)
        var fan: Direction? = null
        if (idx != -1 && straightCount > 1) {
            val lowerOrders = rotateClockwise(
                incident!!
            )
            val higherOrders = rotateAntiClockwise(
                incident
            )
            val lower = floor(straightCount.toDouble() / 2.0 - 1.0).toInt()
            val higher = ceil(straightCount.toDouble() / 2.0).toInt()
            fan = if (idx <= lower) lowerOrders else if (idx >= higher) higherOrders else null
        }
        return ec.convertPlanarizationEdge(e!!, o!!, incident!!, externalVertex, sideVertex, und, fan)
    }

    private fun createSideVertex(
        cd: Set<DiagramElement>,
        und: Vertex
    ): Vertex {
        val underlyings: MutableSet<DiagramElement> = HashSet()
        underlyings.addAll(cd)
        underlyings.addAll(und.getDiagramElements())
        return DartJunctionVertex(und.getID() + "-dv-" + newVertexId++, underlyings)
    }

    override fun convertDiagramElementToInnerFace(original: DiagramElement, o: Orthogonalization): DartFace {
        return convertDiagramElementToInnerFace(original, o, HashMap())
    }

    private fun convertDiagramElementToInnerFace(
        originalUnderlying: DiagramElement,
        o: Orthogonalization,
        dartDirections: Map<Direction, List<IncidentDart>>
    ): DartFace {
        log.send(if (log.go()) null else "Converting: $originalUnderlying with edges: ", dartDirections)
        val cv = em.getOuterCornerVertices(originalUnderlying)
        val perimeter = gp.getClockwiseOrderedContainerVertices(cv)
        return convertDiagramElementToInnerFaceWithCorners(originalUnderlying, o, dartDirections, perimeter)
    }

    protected fun convertDiagramElementToInnerFaceWithCorners(
        originalUnderlying: DiagramElement,
        o: Orthogonalization,
        dartDirections: Map<Direction, List<IncidentDart>>,
        perimeter: List<MultiCornerVertex>
    ): DartFace {
        val allSideDarts: MutableList<Dart> = ArrayList()
        var sideDirection = Direction.RIGHT // initial direction of perimeter
        val start = perimeter[0]
        var done = 0
        while (done < perimeter.size) {
            val fromv = perimeter[done]
            val tov = perimeter[(done + 1) % perimeter.size]
            val outwardsDirection = rotateAntiClockwise(
                sideDirection
            )
            var leavers = dartDirections[outwardsDirection]
            leavers = leavers ?: emptyList()
            val s = createSide(
                fromv,
                tov,
                leavers,
                o,
                sideDirection,
                mapOf(originalUnderlying to outwardsDirection)
            )
            allSideDarts.addAll(s)
            done++
            sideDirection = rotateClockwise(sideDirection)
        }
        val inner = createInnerFace(o, allSideDarts, start, originalUnderlying)
        log.send("Created face: $inner")

        // convert content
        if (originalUnderlying is Container) {
            convertContainerContents(o, originalUnderlying as Container, inner)
        }
        return inner
    }

    protected open fun convertContainerContents(
        o: Orthogonalization,
        originalUnderlying: Container,
        inner: DartFace
    ) {
        // does nothing in this implementation - see ContainerContentsArranger
    }

    override fun convertToOuterFace(o: Orthogonalization, startVertex: Vertex, partOf: Rectangular): DartFace {
        throw LogicException("Not implemented")
    }

    protected fun createInnerFace(
        o: Orthogonalization,
        allSideDarts: List<Dart>,
        start: Vertex,
        de: DiagramElement
    ): DartFace {
        val dd = dartsToDartDirections(allSideDarts, start, false)
        return o.createDartFace(de as Rectangular?, false, dd)
    }

    private fun dartsToDartDirections(allDarts: List<Dart>, vs: Vertex, reverse: Boolean): List<DartDirection> {
        var vs = vs
        val dartsInFace: MutableList<DartDirection> = ArrayList(allDarts.size)
        for (dart in allDarts) {
            var d = dart.getDrawDirectionFrom(vs)
            d = if (reverse) reverse(d) else d
            dartsInFace.add(DartDirection(dart, d!!))
            vs = dart.otherEnd(vs)
        }
        if (reverse) {
            dartsInFace.reverse()
        }
        return dartsInFace
    }

    /**
     * Note - we can't just group otherwise order gets broken.  We must ensure the sides are still in order
     */
    protected fun getDartsInDirection(
        from: Vertex,
        o: Orthogonalization,
        ti: TurnInformation
    ): Map<Direction, List<IncidentDart>> {
        val eo = o.getPlanarization().edgeOrderings[from]
        val cd: Set<DiagramElement> = from.getDiagramElements()
        val processOrder = eo!!.getEdgesAsList()
        val startEdge = ti.getFirstEdgeClockwiseEdgeOnASide()
        val startPoint = if (startEdge != null) processOrder.indexOf(startEdge) else 0
        val inMap: MutableMap<Direction, MutableList<PlanarizationEdge>> = LinkedHashMap()
        inMap[Direction.UP] = ArrayList()
        inMap[Direction.DOWN] = ArrayList()
        inMap[Direction.LEFT] = ArrayList()
        inMap[Direction.RIGHT] = ArrayList()
        for (i in processOrder.indices) {
            val planarizationEdge = processOrder[(i + startPoint) % processOrder.size]
            inMap[reverse(ti.getIncidentDartDirection(planarizationEdge))]!!
                .add(planarizationEdge)
        }
        val outMap: MutableMap<Direction, List<IncidentDart>> = HashMap()
        for (dir in inMap.keys) {
            val outList: List<IncidentDart> = createIncidentDarts(ti, cd, o, from, inMap[dir]!!)
            outMap[dir] = outList
        }
        return outMap
    }

    protected fun createIncidentDarts(
        ti: TurnInformation,
        cd: Set<DiagramElement>,
        o: Orthogonalization,
        from: Vertex,
        list: List<PlanarizationEdge>
    ): MutableList<IncidentDart> {
        val outList = mutableListOf<IncidentDart>()
        val fanBuckets = createFanBuckets(list, ti)
        val straightCount = fanBuckets.size.toLong()
        for (i in list.indices) {
            val current = list[i]
            val idx = getFanBucket(current, fanBuckets)
            val id = convertEdgeToIncidentDart(
                current,
                cd,
                o,
                ti.getIncidentDartDirection(current),
                idx,
                from,
                straightCount.toInt()
            )
            outList.add(id)
        }
        return outList
    }

    private fun getFanBucket(current: PlanarizationEdge, fanBuckets: List<Set<PlanarizationEdge>>): Int {
        for (i in fanBuckets.indices) {
            if (fanBuckets[i].contains(current)) {
                return i
            }
        }
        return -1
    }

    /**
     * Arranges the [PlanarizationEdge]s into buckets of same destination.   All elements to same destination
     * will get the same fan-style.
     */
    private fun createFanBuckets(inEdges: List<PlanarizationEdge>, ti: TurnInformation): List<Set<PlanarizationEdge>> {
        val out: MutableList<Set<PlanarizationEdge>> = ArrayList()
        var currentSet: MutableSet<PlanarizationEdge> = HashSet()
        var last: PlanarizationEdge? = null
        for (pe in inEdges) {
            if (!ti.doesEdgeHaveTurns(pe)) {
                if (last == null || !last.meets(pe.getFrom()) || !last.meets(pe.getTo())) {
                    // new bucket required
                    currentSet = HashSet()
                    out.add(currentSet)
                    currentSet.add(pe)
                    last = pe
                } else {
                    currentSet.add(pe)
                }
            }
        }
        return out
    }

    /**
     * Links corner elements with the incident darts into the whole side of the underlying.
     */
    protected fun createSide(
        from: Vertex,
        to: Vertex,
        onSide: List<IncidentDart>,
        o: Orthogonalization,
        goingIn: Direction,
        underlyings: Map<DiagramElement, Direction>
    ): List<Dart> {
        val out: MutableList<Dart> = ArrayList()
        var last = from
        var incidentDart: IncidentDart? = null
        if (onSide != null) {
            for (j in onSide.indices) {
                incidentDart = onSide[j]
                val vsv = incidentDart!!.internal
                out.addAll(ec.buildDartsBetweenVertices(underlyings, o!!, last!!, vsv, goingIn!!))
                last = vsv
            }
        }

        // finally, join to corner
        out.addAll(ec.buildDartsBetweenVertices(underlyings, o!!, last!!, to!!, goingIn!!))
        return out
    }

    companion object {
        const val INTER_EDGE_SEPARATION = 0
    }

}