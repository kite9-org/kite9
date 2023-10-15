package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

/**
 * This turns the diagram's hierarchical structure of DiagramElement's into C2Slideables.
 *
 * @author robmoffat
 */
class C2ContainerBuilderCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override val prefix: String
        get() = "CBCS"

    override val isLoggingEnabled: Boolean
        get() = true

    override fun compact(c: C2Compaction, g: Group) {
        checkCreate(c.getDiagram(), Dimension.H, c.getSlackOptimisation(Dimension.H), null)
        checkCreate(c.getDiagram(), Dimension.V, c.getSlackOptimisation(Dimension.V), null)
    }

    private fun checkCreate(
        de: DiagramElement,
        d: Dimension,
        cso: C2SlackOptimisation,
        cExisting: C2Slideable?
    ): RectangularSlideableSet? {
        log.send("Creating $de")
        if (de !is Rectangular) {
            return null
        }

        var ss = cso.getSlideablesFor(de) as RectangularSlideableSet?

        if (ss == null) {
            // we need to create these then
            val ms = getMinimumDistanceBetween(de, Side.START, de, Side.END, d, null, false)

            val l = C2Slideable(cso, d, Purpose.EDGE, de, Side.START)
            val r = C2Slideable(cso, d, Purpose.EDGE, de, Side.END)
            val c = cExisting ?: C2Slideable(cso, d, Purpose.ROUTE, de, Side.NEITHER)

            // TODO: Add logic for line centering here.
            cso.ensureMinimumDistance(l, r, ms.toInt())
            cso.ensureMinimumDistance(l, c, (ms / 2.0).toInt())
            cso.ensureMinimumDistance(c, r, (ms / 2.0).toInt())

            ss = RectangularSlideableSetImpl(de, l, r, c)
            cso.add(de, ss)
        }

        if (de is Container) {
            checkCreateItems(cso, de, d, de.getLayout(), ss)
        }

        log.send("Created RectangularSlideableSetImpl: ${ss.d}", ss.getAll())
        return ss

    }

    private fun checkCreateItems(
        cso: C2SlackOptimisation,
        de: Container,
        d: Dimension,
        l: Layout?,
        container: RectangularSlideableSet
    ) {

        val contents = de.getContents()
            .filterIsInstance<ConnectedRectangular>()

        val anchors = contents.map { Anchor(it, Side.NEITHER) }.toSet()

        val centerLine = when (l) {
            Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> if (d == Dimension.V) C2Slideable(
                cso,
                d,
                Purpose.ROUTE,
                anchors
            ) else null

            Layout.UP, Layout.DOWN, Layout.VERTICAL -> if (d == Dimension.H) C2Slideable(
                cso,
                d,
                Purpose.ROUTE,
                anchors
            ) else null

            else -> null
        }

        val contentMap = contents
            .map { it to checkCreate(it, d, cso, centerLine) }

        // ensure within container
        contentMap
            .forEach { (k, v) ->
                val distL = getMinimumDistanceBetween(de, Side.START, k, Side.START, d, null, true)
                val distR = getMinimumDistanceBetween(de, Side.END, k, Side.END, d, null, true)
                separateRectangular(container, Side.START, v!!, Side.START, cso, distL)
                separateRectangular(v!!, Side.END, container, Side.END, cso, distR)
            }

        // ensure internal ordering
        when (l) {
            Layout.RIGHT, Layout.HORIZONTAL -> if (d == Dimension.H) setupInternalOrdering(contentMap, d, cso)
            Layout.LEFT -> if (d == Dimension.H) setupInternalOrdering(contentMap.reversed(), d, cso)
            Layout.DOWN, Layout.VERTICAL -> if (d == Dimension.V) setupInternalOrdering(contentMap, d, cso)
            Layout.UP -> if (d == Dimension.V) setupInternalOrdering(contentMap.reversed(), d, cso)
            else -> { /* do nothing */
            }
        }
    }

    private fun setupInternalOrdering(
        orderedContents: List<Pair<Rectangular, RectangularSlideableSet?>>,
        d: Dimension,
        cso: C2SlackOptimisation
    ) {
        for (i in 1 until orderedContents.size) {
            val prev = orderedContents[i - 1]
            val current = orderedContents[i]
            val dist = getMinimumDistanceBetween(prev.first, Side.END, current.first, Side.START, d, null, true)
            separateRectangular(prev.second!!, Side.END, current.second!!, Side.START, cso, dist)
        }
    }

    private fun separateRectangular(
        a: RectangularSlideableSet,
        aSide: Side,
        b: RectangularSlideableSet,
        bSide: Side,
        cso: C2SlackOptimisation,
        dist: Double
    ) {
        cso.ensureMinimumDistance(a.getRectangularOnSide(aSide), b.getRectangularOnSide(bSide), dist.toInt())
    }

}