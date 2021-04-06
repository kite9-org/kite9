package org.kite9.diagram.visualization.orthogonalization.vertex

import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.mapping.SubGridCornerVertices
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.isMin
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.edge.IncidentDart

/**
 * Handles the conversion of elements not in the planarization into Darts and DartFaces.  These could have been
 * ommitted from the planarization because they are text labels, or parts of the diagram that don't need
 * connections.
 *
 * @author robmoffat
 */
class ContainerContentsArranger(em: ElementMapper) : MultiElementVertexArranger(em) {

    override fun convertContainerContents(o: Orthogonalization, c: Container, inner: DartFace) {
        if (c.getLayout() === Layout.GRID) {
            val outer = createGridFaceForContainerContents(o, c)
            if (outer != null) {
                outer.setContainedBy(inner)
                log.send("Created container contents outer face: $outer")
            }
        } else {
            for (de in c.getContents()) {
                if (de is ConnectedRectangular) {
                    val df = convertDiagramElementToInnerFace(de, o)
                    val outerFace = convertToOuterFace(o, df.startVertex, de as Rectangular)
                    outerFace.setContainedBy(inner)
                    log.send("Created face: $df")
                    log.send("Created face: $outerFace")
                }
            }
        }
    }

    override fun convertToOuterFace(
        o: Orthogonalization,
        startVertex: Vertex,
        partOf: Rectangular
    ): DartFace {
        var current = startVertex
        val orig = current
        var d = Direction.DOWN
        val out: MutableList<DartDirection> = ArrayList()
        do {
            var dart = getDartInDirection(current, d)
            if (dart == null) {
                // turn the corner when we reach the end of the side
                d = rotateAntiClockwise(d)
                dart = getDartInDirection(current, d)
            }
            out.add(DartDirection(dart!!, d))
            if (dart == null) {
                throw LogicException("Can't follow perimeter !$current")
            }
            current = dart.otherEnd(current)
        } while (current !== orig)
        return o.createDartFace(partOf, true, out)
    }

    private fun getTopLeftVertex(createdVertices: Set<MultiCornerVertex>): Vertex {
        for (multiCornerVertex in createdVertices) {
            if (isMin(multiCornerVertex.xOrdinal) &&
                isMin(multiCornerVertex.yOrdinal)
            ) {
                return multiCornerVertex
            }
        }
        throw LogicException()
    }

    /**
     * This is a bit like duplication of the code in [RHDPlanarizationBuilder],
     * but I think I'll live with it for now.
     *
     * @return the outerface to embed in the container.
     */
    private fun createGridFaceForContainerContents(o: Orthogonalization, c: Container): DartFace? {
        val emptyMap: Map<Direction, List<IncidentDart>> = HashMap()
        val createdVertices: MutableSet<MultiCornerVertex> = LinkedHashSet()
        val stuffAdded = placeContainerContentsOntoGrid(o, c, emptyMap, createdVertices)
        return if (stuffAdded) {
            val startVertex = getTopLeftVertex(createdVertices)
            convertToOuterFace(o, startVertex, c)
        } else {
            null
        }
    }

    private fun placeContainerContentsOntoGrid(
        o: Orthogonalization, c: Container,
        emptyMap: Map<Direction, List<IncidentDart>>, createdVertices: MutableSet<MultiCornerVertex>
    ): Boolean {
        val elementArray = gp.placeOnGrid(c, true)
        val connectedElements: List<DiagramElement> = elementArray
            .flatMap { it.toList() }
            .distinct()

        if (connectedElements.isEmpty()) {
            return false
        }

        // set up vertices for each grid element
        for (de in connectedElements) {
            val cv = em.getOuterCornerVertices(de) as SubGridCornerVertices
            createdVertices.addAll(cv.getVerticesAtThisLevel())
        }
        val topLeftVertices: MutableMap<DiagramElement, MultiCornerVertex?> = LinkedHashMap()

        // link them together
        for (de in connectedElements) {
            val cv = em.getOuterCornerVertices(de) as SubGridCornerVertices

            // having created all the vertices, join them to form faces
            val perimeterVertices = gp.getClockwiseOrderedContainerVertices(cv)
            var prev: MultiCornerVertex? = null
            var start: MultiCornerVertex? = null
            for (current in perimeterVertices) {
                if (prev != null) {
                    // create a dart between prev and current
                    val d = getDirection(prev, current)
                    val underlyings = mutableMapOf(de to rotateAntiClockwise(d))
                    ec.buildDartsBetweenVertices(underlyings, o, prev, current, d)
                } else {
                    start = current
                    topLeftVertices[de] = start
                }
                prev = current
            }
            val d = getDirection(prev, start)
            val underlyings = mutableMapOf(de to rotateAntiClockwise(d))
            ec.buildDartsBetweenVertices(underlyings, o, prev!!, start!!, d)
        }

        // recurse
        for ((key, value) in topLeftVertices) {
            val s: MutableList<Dart> = ArrayList()
            populateInnerFaceDarts(s, value!!, value!!, Direction.RIGHT)
            val inner = createInnerFace(o, s, value, key)
            if (key is Container) {
                convertContainerContents(o, key, inner)
            }
        }
        return true
    }

    private fun getDirection(from: MultiCornerVertex?, to: MultiCornerVertex?): Direction {
        val horiz = from!!.xOrdinal.compareTo(to!!.xOrdinal)
        val vert = from.yOrdinal.compareTo(to.yOrdinal)
        return if (horiz == 0 && vert == 0) {
            throw LogicException("Vertex overlap")
        } else if (horiz != 0 && vert != 0) {
            throw LogicException("Vertices are diagonal")
        } else if (horiz == 0) {
            if (vert == -1) Direction.DOWN else Direction.UP
        } else {
            if (horiz == -1) Direction.RIGHT else Direction.LEFT
        }
    }

    companion object {
        fun populateInnerFaceDarts(darts: MutableList<Dart>, from: Vertex, to: Vertex, going: Direction) {
            var from = from
            var going = going
            var d = getDartInDirection(from, going)
            val minDarts = if (from === to) 2 else 1
            darts.add(d!!)
            while (!d!!.meets(to) || darts.size < minDarts) {
                going = d.getDrawDirectionFrom(from)!!
                from = d.otherEnd(from)
                d = getDartInDirection(
                    from, rotateClockwise(
                        going!!
                    )
                )
                d = d ?: getDartInDirection(from, going)
                d = d
                    ?: getDartInDirection(
                        from, rotateAntiClockwise(
                            going
                        )
                    )
                if (d == null) {
                    throw LogicException()
                }
                darts.add(d)
            }
        }

        fun getDartInDirection(current: Vertex, d: Direction): Dart? {
            for (e in current.getEdges()) {
                if (e is Dart && e.getDrawDirectionFrom(current) === d) {
                    return e
                }
            }
            return null
        }
    }
}