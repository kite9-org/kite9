package org.kite9.diagram.visualization.orthogonalization.edge

import org.kite9.diagram.common.Collections
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.mapping.CornerVertices
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter
import org.kite9.diagram.visualization.orthogonalization.vertex.ContainerContentsArranger
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge

open class LabellingEdgeConverter(cc: ContentsConverter, val em: ElementMapper) : SimpleEdgeConverter(cc) {

    override fun convertPlanarizationEdge(
        e: PlanarizationEdge,
        o: Orthogonalization,
        incident: Direction,
        externalVertex: Vertex,
        sideVertex: Vertex,
        planVertex: Vertex,
        fan: Direction?
    ): IncidentDart {
        var l: Label? = null
        if (e is ConnectionEdge) {
            val ce = e
            val fromEnd = planVertex.isPartOf(ce.getFromConnected())
            val toEnd = planVertex.isPartOf(ce.getToConnected())
            if (fromEnd) {
                if (planVertex.getDiagramElements().contains(ce.getFromConnected())) {
                    // we have the actual end then
                    l = ce.getOriginalUnderlying().getFromLabel()
                }
            } else if (toEnd) {
                if (planVertex.getDiagramElements().contains(ce.getToConnected())) {
                    // we have the actual end then
                    l = ce.getOriginalUnderlying().getToLabel()
                }
            } else {
                // middle bit of an edge
                l = null
            }
        } else if (e is BorderEdge) {
            val labelSide = rotateAntiClockwise(incident)
            val de = e.getElementForSide(labelSide)
            if (de is Container) {
                l = findUnprocessedLabel(de, labelSide)
            }
        }
        return if (l != null) {
            val labelSide = getLabelDirection(l, incident, fan)
            convertWithLabel(e, o, incident, labelSide, externalVertex, sideVertex, l)
        } else {
            super.convertPlanarizationEdge(e, o, incident, externalVertex, sideVertex, planVertex, fan)
        }
    }

    fun getLabelDirection(
        l: Label,
        incident: Direction,
        fan: Direction?
    ): Direction {
        val lp = l.getLabelPlacement()
        val defaultDirection = getDefaultLabelDirection(incident, fan)
        val labelSide = lp?.connectionLabelPlacementDirection(incident, defaultDirection) ?: defaultDirection
        return labelSide
    }

    open protected fun getDefaultLabelDirection(incident: Direction, fan: Direction?): Direction {
        return if (!Direction.isHorizontal(incident)) Direction.LEFT else Direction.UP
    }

    private fun findUnprocessedLabel(c: Container, side: Direction?): Label? {
        for (de in c.getContents()) {
            if (de is Label) {
                if (de.getLabelPlacement()!!.containerLabelPlacement(side!!)) {
                    if (!em.hasOuterCornerVertices(de)) {
                        return de
                    }
                }
            }
        }
        return null
    }

    private fun hasLabels(con: Container, side: Direction?): Boolean {
        return con.getContents()
            .filterIsInstance<Label>()
            .filter { c: DiagramElement ->
                (c as Label).getLabelPlacement()!!
                    .containerLabelPlacement(side!!)
            }
            .count() > 0
    }

    private fun convertWithLabel(
        e: PlanarizationEdge,
        o: Orthogonalization,
        incident: Direction,
        labelJoinConnectionSide: Direction,
        externalVertex: Vertex,
        sideVertex: Vertex,
        l: Label
    ): IncidentDart {
        val side = reverse(incident)
        cc.convertDiagramElementToInnerFace(l, o)
        val cv = em.getOuterCornerVertices(l)
        val sideToLabel: Vertex
        val labelToExternal: Vertex
        when (labelJoinConnectionSide) {
            Direction.UP -> {
                sideToLabel = if (incident === Direction.LEFT) cv.getTopLeft() else cv.getTopRight()
                labelToExternal = if (incident === Direction.LEFT) cv.getTopRight() else cv.getTopLeft()
            }
            Direction.DOWN -> {
                sideToLabel = if (incident === Direction.LEFT) cv.getBottomLeft() else cv.getBottomRight()
                labelToExternal = if (incident === Direction.LEFT) cv.getBottomRight() else cv.getBottomLeft()
            }
            Direction.LEFT -> {
                sideToLabel = if (incident === Direction.UP) cv.getTopLeft() else cv.getBottomLeft()
                labelToExternal = if (incident === Direction.UP) cv.getBottomLeft() else cv.getTopLeft()
            }
            Direction.RIGHT -> {
                sideToLabel = if (incident === Direction.UP) cv.getTopRight() else cv.getBottomRight()
                labelToExternal = if (incident === Direction.UP) cv.getBottomRight() else cv.getTopRight()
            }
            else -> throw LogicException()
        }
        val map = createMap(e)
        o.createDart(sideVertex, sideToLabel, map, side!!)
        o.createDart(sideToLabel, labelToExternal, map, side)
        o.createDart(labelToExternal, externalVertex, map, side)
        if (e is ConnectionEdge) {
            handleLabelContainment(e.getOriginalUnderlying(), l)
        }
        return IncidentDart(externalVertex, sideVertex, side, e)
    }

    /**
     * This method alters the DiagramElements' containment hierarchy, so that Compaction works correctly.
     * We shouldn't be altering that at all.
     */
    @Deprecated("")
    private fun handleLabelContainment(c: Connection, l: Label) {
        if (c.getFromLabel() === l) {
            val cc = c.getFrom().getContainer()
            cc!!.getContents().add(l)
        } else if (c.getToLabel() === l) {
            val cc = c.getTo().getContainer()
            cc!!.getContents().add(l)
        } else {
            throw LogicException()
        }
    }

    /**
     * Adds labels to container edges, if the container has a label. We add the
     * label by splitting the first dart we find.
     */
    fun addLabelsToContainerDart(
        o: Orthogonalization,
        darts: MutableList<Dart>,
        from: Vertex,
        to: Vertex,
        going: Direction?
    ) {
        var hasLabels = false
        for (dart in darts) {
            for (de in dart.getDiagramElements().keys) {
                if (de is Container) {
                    val sideDirection = dart.getDiagramElements()[de]
                    hasLabels = hasLabels || hasLabels(de, sideDirection)

                    // clockwise direction around container
                    val d = rotateClockwise(
                        sideDirection!!
                    )
                    val end1 = if (dart.getDrawDirection() === d) dart.getFrom() else dart.getTo()

                    // greedily collect all possible labels
                    val toProcess: MutableMap<Label, CornerVertices> = LinkedHashMap()
                    var l = findUnprocessedLabel(de, sideDirection)
                    while (l != null) {
                        toProcess[l] = em.getOuterCornerVertices(l)
                        l = findUnprocessedLabel(de, sideDirection)
                    }

                    // split the darts up
                    var cdart = dart;
                    for ((key, value) in toProcess) {
                        val waypoints = rotateWaypointsCorrectly(value, d)
                        val p1 = o.splitDart(cdart, waypoints[0])
                        val p1keep = if (p1.a.meets(end1)) p1.a else p1.b
                        val p1change = if (p1.a.meets(end1)) p1.b else p1.a
                        o.splitDart(p1change, waypoints[3])
                        cc.convertDiagramElementToInnerFace(key, o)
                        cdart = p1keep
                    }
                }
            }
        }
        if (hasLabels) {
            darts.clear()
            ContainerContentsArranger.populateInnerFaceDarts(darts, from, to, going!!)
        }
    }

    /**
     * Waypoints is ordered if d is left (i.e. the label is at the bottom) However,
     * it could be in any direction.
     */
    private fun rotateWaypointsCorrectly(cv: CornerVertices, d: Direction): Array<Vertex> {
        var d = d
        var wp: Array<Vertex> = arrayOf(cv.getBottomRight(), cv.getTopRight(), cv.getTopLeft(), cv.getBottomLeft())
        while (d !== Direction.LEFT) {
            wp = Collections.leftShift(wp, 1)
            d = rotateClockwise(d)
        }
        return wp
    }

    override fun buildDartsBetweenVertices(
        underlyings: Map<DiagramElement, Direction?>, o: Orthogonalization, end1: Vertex,
        end2: Vertex, d: Direction
    ): MutableList<Dart> {
        val s = super.buildDartsBetweenVertices(underlyings, o, end1, end2, d)
        addLabelsToContainerDart(o, s, end1, end2, d)
        return s
    }
}