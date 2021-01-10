package org.kite9.diagram.visualization.orthogonalization

import org.kite9.diagram.common.elements.vertex.CompactionHelperVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection
import org.kite9.diagram.visualization.planarization.Planarization
import java.util.*
import java.util.function.Consumer

/**
 * This class manages the relationships of the [Dart]s to the [Edge]s, and keeps track of
 * any inserted temporary vertex objects.
 *
 * @author robmoffat
 */
class OrthogonalizationImpl(private val planarization: Planarization) : Orthogonalization {

    /**
     * Stores the list of darts for a diagram element
     */
    private val waypointMap: MutableMap<DiagramElement, MutableSet<Dart>> = HashMap()
    private val allDarts: MutableSet<Dart> = LinkedHashSet() // needed for compaction
    private val allVertices : MutableCollection<Vertex> = LinkedHashSet()
    private val faces: MutableList<DartFace> = mutableListOf() // needed for compaction
    private val facesRectangularMap: MutableMap<Rectangular?, MutableList<DartFace>> = HashMap()
    private val dartDartFacesMap: MutableMap<Dart, MutableList<DartFace>> = HashMap()
    private val existingDarts: MutableMap<Vertex, MutableMap<Vertex, MutableSet<Dart>>> = HashMap()

    private fun getDartsForEdge(e: DiagramElement): Set<Dart> {
        return waypointMap[e]!!
    }

    override fun toString(): String {
        return "ORTHOGONALIZATION: [ \n\n faces: ${displayFaces(faces, "\n\t", "\n\n")}"
    }

    private fun displayFaces(faces2: List<DartFace>, linesep: String, facesep: String): String {
        val sb = StringBuilder()
        for (x in faces2) {
            sb.append(facesep)
            sb.append(x.id)
            sb.append(":")
            sb.append(renderFaceDarts(x.dartsInFace, linesep))
        }
        return sb.toString()
    }

    override fun getAllDarts(): Set<Dart> {
        return allDarts
    }

    override fun getAllVertices(): Collection<Vertex> {
        return allVertices
    }

    override fun getFaces(): List<DartFace> {
        return faces
    }

    override fun getPlanarization(): Planarization {
        return planarization
    }

    // a list of darts with corners following them, used for flow orthogonalization
    var cornerDarts: Map<Vertex, List<Dart>> = HashMap()
    var nextDart = 0



    override fun createDart(
        from: Vertex,
        to: Vertex,
        partOf: DiagramElement,
        d: Direction,
        partOfSide: Direction
    ): Dart {
        return createDart(from, to, setOf(partOf), d, partOfSide)
    }

    override fun createDart(
        from: Vertex,
        to: Vertex,
        partOf: Set<DiagramElement>,
        d: Direction,
        partOfSide: Direction
    ): Dart {
        val next: MutableMap<DiagramElement, Direction> = HashMap()
        partOf.forEach(Consumer { a: DiagramElement -> next[a] = partOfSide })
        return createDart(from, to, next, d)
    }

    override fun createDart(from: Vertex, to: Vertex, partOf: Map<DiagramElement, Direction?>, d: Direction): Dart {
        val first = if (from.compareTo(to) > 0) from else to
        val second = if (first === from) to else from
        //partOf.remove(null)

        // first, check if dart already exists, or one has been created for this purpose already.
        var existing: MutableSet<Dart>?
        var secMap = existingDarts[first]
        if (secMap == null) {
            secMap = HashMap()
            existingDarts[first] = secMap
        }
        existing = secMap[second]
        if (existing == null) {
            existing = LinkedHashSet()
            secMap[second] = existing
        } else {
            // we potentially have some darts that could be used instead
            for (dart in existing) {
                if (dart.getDrawDirectionFrom(from) !== d) {
                    throw LogicException("Trying to create new dart in different direction to: $existing")
                }

                // add some new underlyings
                (dart as DartImpl).underlyings.putAll(partOf)
                addToWaypointMap(dart, partOf.keys)
                return dart
            }
        }

        // need to create the dart
        ensureNoDartInDirection(from, d)
        ensureNoDartInDirection(to, reverse(d))
        val out: Dart = DartImpl(from, to, HashMap(partOf), d, "d" + nextDart++, this)
        addToWaypointMap(out, partOf.keys)
        existing.add(out)
        allDarts.add(out)
        allVertices.add(from)
        allVertices.add(to)
        return out
    }

    private fun ensureNoDartInDirection(from: Vertex, d: Direction?) {
        val existing = getDartInDirection(from, d)
        if (existing != null) {
            throw LogicException("Already have a dart going $d from $from: $existing")
        }
    }

    private fun getDartInDirection(around: Vertex, d: Direction?): Dart? {
        for (e in around.getEdges()) {
            if (e is Dart) {
                if (e.getDrawDirectionFrom(around) === d) {
                    return e
                }
            }
        }
        return null
    }

    private fun addToWaypointMap(out: Dart, partsOf: Set<DiagramElement>) {
        for (partOf in partsOf) {
            var wpDarts = waypointMap[partOf]
            if (wpDarts == null) {
                wpDarts = LinkedHashSet()
                waypointMap[partOf] = wpDarts
            }
            wpDarts.add(out)
        }
    }

    private fun removeFromWaypointMap(d: Dart) {
        for (de in d.getDiagramElements().keys) {
            waypointMap[de]!!.remove(d)
        }
    }

    fun unlinkDartFromMap(d: Dart) {
        val from = d.getFrom()
        val to = d.getTo()
        val first = if (from.compareTo(to) > 0) from else to
        val second = if (first === from) to else from
        var existing: MutableSet<Dart>?
        val secMap: Map<Vertex, MutableSet<Dart>>? = existingDarts[first]
        if (secMap == null) {
            throw LogicException("Dart is not in map: $d")
        } else {
            existing = secMap[second]
            if (!existing!!.contains(d)) {
                throw LogicException("Dart is not in map: $d")
            } else {
                existing.remove(d)
            }
        }
    }

    fun relinkDartInMap(d: Dart) {
        val from = d.getFrom()
        val to = d.getTo()
        val first = if (from.compareTo(to) > 0) from else to
        val second = if (first === from) to else from
        var secMap = existingDarts[first]
        if (secMap == null) {
            secMap = HashMap()
            existingDarts[first] = secMap
        }
        var theSet = secMap[second]
        if (theSet == null) {
            theSet = LinkedHashSet()
            secMap[second] = theSet
        }
        theSet.add(d)
    }

    private var faceNo = 0
    override fun createDartFace(partOf: Rectangular?, outerFace: Boolean, darts: List<DartDirection>): DartFace {
        val df = DartFace(faceNo++, outerFace, darts, partOf)
        faces.add(df)
        var frl = facesRectangularMap[partOf]
        if (frl == null) {
            frl = ArrayList()
            facesRectangularMap[partOf] = frl
        }
        for (dd in darts) {
            var faces = dartDartFacesMap[dd.dart]
            if (faces == null) {
                faces = ArrayList(2)
                dartDartFacesMap[dd.dart] = faces
            }
            faces.add(df)
        }
        frl.add(df)
        return df
    }

    private var helper = 0
    fun createHelperVertex(): Vertex {
        val out: Vertex = CompactionHelperVertex("x" + helper++)
        allVertices.add(out)
        return out
    }

    /**
     * The list of vertices must be returned in the same order as the connection represented.
     */
    override fun getWaypointsForBiDirectional(c: Connection): List<Vertex>? {
        val darts = getDartsForEdge(c)
        if (darts == null) {
            return null
        } else {
            val vertexCounts: Map<Vertex, Int> = darts
                .flatMap { d: Dart -> listOf(d.getFrom(), d.getTo()) }
                .groupingBy { a: Vertex -> a }
                .eachCount()
            val ends = vertexCounts.keys.filter { a: Vertex -> vertexCounts[a] == 1 }
            if (ends.size != 2) {
                throw LogicException()
            }
            val it: Iterator<Vertex> = ends.iterator()
            val one = it.next()
            val two = it.next()
            var start: Vertex
            val end: Vertex
            if (one.getDiagramElements().contains(c.getFrom()) && two.getDiagramElements().contains(c.getTo())) {
                start = one
                end = two
            } else if (one.getDiagramElements().contains(c.getTo()) && two.getDiagramElements().contains(c.getFrom())) {
                start = two
                end = one
            } else {
                throw LogicException()
            }
            val out: MutableList<Vertex> = ArrayList(darts.size + 1)
            var next: Dart? = null
            do {
                out.add(start)
                if (start === end) {
                    return out
                }
                next = findNextDart(darts, start, next)
                start = next.otherEnd(start)
            } while (true)
        }
    }

    private fun findNextDart(darts: Set<Dart>, start: Vertex, lastDart: Dart?): Dart {
        for (e in start.getEdges()) {
            if (darts.contains(e) && e !== lastDart) {
                return e as Dart
            }
        }
        throw LogicException("Couldn't find next dart from $start")
    }

    override fun getDartsForDiagramElement(e: DiagramElement): Set<Dart>? {
        return waypointMap[e]
    }

    override fun getDartFacesForRectangular(r: Rectangular): List<DartFace> {
        return facesRectangularMap.getOrPut(r) { mutableListOf<DartFace>() }
    }

    override fun getDartFacesForDart(d: Dart): List<DartFace>? {
        return dartDartFacesMap[d]
    }

    override fun splitDart(dart: Dart, splitWithVertex: Vertex): Pair<Dart> {
        if (getDartFacesForDart(dart) != null) {
            throw LogicException("Can't split darts in faces")
        }
        if (allVertices.contains(splitWithVertex)) {
            throw LogicException("Can't split with $splitWithVertex it's already in the Orth")
        }
        val dart1: Dart = DartImpl(
            dart.getFrom(),
            splitWithVertex,
            HashMap(dart.getDiagramElements()),
            dart.getDrawDirection(),
            dart.getID() + "-1",
            this
        )
        val dart2: Dart = DartImpl(
            splitWithVertex,
            dart.getTo(),
            HashMap(dart.getDiagramElements()),
            dart.getDrawDirection(),
            dart.getID() + "-1",
            this
        )
        allDarts.remove(dart)
        allDarts.add(dart1)
        allDarts.add(dart2)
        unlinkDartFromMap(dart)
        relinkDartInMap(dart1)
        relinkDartInMap(dart2)
        removeFromWaypointMap(dart)
        addToWaypointMap(dart1, dart1.getDiagramElements().keys)
        addToWaypointMap(dart2, dart2.getDiagramElements().keys)
        dart.getFrom().removeEdge(dart)
        dart.getTo().removeEdge(dart)
        return Pair(dart1, dart2)
    }

    companion object {
        private const val serialVersionUID = 7718144529742941851L
        fun renderFaceDarts(x: List<DartDirection>, sep: String?): String {
            val sb = StringBuilder()
            for (i in x.indices) {
                val out = x[i]
                sb.append(out.toString())
                sb.append(sep)
            }
            return sb.toString()
        }
    }
}