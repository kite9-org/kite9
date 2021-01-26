package org.kite9.diagram.visualization.orthogonalization.vertex

import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger.TurnInformation
import java.util.*

/**
 * This mainly handles returning DartDirection objects which form the Boundary of a vertex in the diagram.
 * Actual conversion from Planarization vertices to darts is handled by subclasses.
 *
 * @author robmoffat
 */
abstract class AbstractVertexArranger(protected var em: ElementMapper) : VertexArranger, Logable, ContentsConverter {

    protected var log = Kite9Log(this)

    private val boundaries: MutableMap<Vertex, List<Boundary>> = HashMap()

    override fun returnDartsBetween(
        i: PlanarizationEdge,
        outDirection: Direction,
        v: Vertex,
        out: PlanarizationEdge,
        o: Orthogonalization,
        ti: TurnInformation
    ): List<DartDirection> {
        var relevantBoundaries = boundaries[v]
        if (relevantBoundaries == null) {
            convertVertex(o, v, ti)
            relevantBoundaries = boundaries[v]
        }
        return findDartsToInsert(relevantBoundaries!!, i, outDirection, out)
    }

    protected abstract fun convertVertex(o: Orthogonalization, v: Vertex, ti: TurnInformation): DartFace?

    /**
     * Works out which darts are needed from the vertex to fill a gap between in
     * and out in the face. Always proceeds in an anti-clockwise direction.
     *
     * @param outerFace
     */
    private fun findDartsToInsert(
        relevantBoundaries: List<Boundary>,
        `in`: PlanarizationEdge,
        outDirection: Direction,
        out: PlanarizationEdge?
    ): List<DartDirection> {
        for ((from, to, toInsert) in relevantBoundaries) {
            if (from.joins(`in`) && to.joins(out!!)) {
                return toInsert
            }
        }
        throw LogicException()
    }

    /**
     * Returns the dart for an external vertex.  There should only be one.
     */
    private fun getSingleExternalVertexDart(from: Vertex): Dart {
        if (from.getEdgeCount() != 1) {
            throw LogicException()
        }
        return from.getEdges().iterator().next() as Dart
    }

    /**
     * Divides up the darts around the vertex between the darts entering the vertex.
     * The arrangement of darts at this point should look something like a spider, with the ends of the legs
     * being the external vertices.
     *
     * Note:  because we are returning the vertex boundaries to include in other faces, we trace in an
     * anti-clockwise direction, as a face touching this boundary will proceed around it in an anti-clockwise
     * direction.
     */
    protected fun setupBoundaries(externalVertices: Set<Vertex>, forVertex: Vertex) {
        val made: MutableList<Boundary> = ArrayList()
        val toProcess: Set<Vertex> = HashSet(externalVertices)
        for (from in toProcess) {
            var dart: Dart? = null
            dart = getSingleExternalVertexDart(from)
            val dds: MutableList<DartDirection> = ArrayList()
            var to = dart.otherEnd(from)
            var d = dart.getDrawDirectionFrom(from)
            do {
                dds.add(DartDirection(dart!!, d!!))
                dart = getNextDartAntiClockwise(to, dart)
                d = dart!!.getDrawDirectionFrom(to)
                to = dart.otherEnd(to)
            } while (!externalVertices.contains(to))
            dds.add(DartDirection(dart!!, d!!))
            val b = Boundary((from as ExternalVertex), (to as ExternalVertex), dds)
            made.add(b)
            //from = to
            log.send("Vertex: " + forVertex + " has boundary: " + b + " with darts: " + b.toInsert)
        }
        boundaries[forVertex] = made
    }

    public var newVertexId = 0

    fun createExternalVertex(
        e: PlanarizationEdge?,
        end: Vertex
    ): ExternalVertex {
        return ExternalVertex(end.getID() + "-ve" + newVertexId++, e!!)
    }

    companion object {
        private fun antiClockwiseTurns(from: Direction?, to: Direction?): Int {
            var from = from
            var c = 0
            while (from !== to) {
                from = rotateAntiClockwise(from!!)
                c++
            }
            return c
        }

        fun getNextDartAntiClockwise(incidentVertex: Vertex, `in`: Dart?): Dart? {
            val directionToVertex = reverse(
                `in`!!.getDrawDirectionFrom(incidentVertex)
            )
            var out: Dart? = null
            var bestScore = 100
            for (e in incidentVertex.getEdges()) {
                if (e is Dart) {
                    val thisDirection = reverse(e.getDrawDirectionFrom(incidentVertex))
                    val turns = antiClockwiseTurns(directionToVertex, thisDirection)
                    if (turns > 0 && turns < bestScore) {
                        out = e
                        bestScore = turns
                    }
                }
            }
            return out ?: `in`
        }
    }

    override val prefix: String
        get() = "VA  "

    override val isLoggingEnabled: Boolean
        get() = false
}