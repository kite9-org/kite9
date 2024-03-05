package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSetImpl
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

abstract class AbstractC2BuilderCompactionStep(cd: CompleteDisplayer, gp: GridPositioner) : AbstractC2CompactionStep(cd) {


    fun checkCreate(
        de: DiagramElement,
        d: Dimension,
        cso: C2SlackOptimisation,
        cExisting: C2IntersectionSlideable?,
        topGroup: Group?
    ): RectangularSlideableSet? {
        log.send("Creating $de")
        if (de !is Rectangular) {
            return null
        }

        var ss = cso.getSlideablesFor(de)

        if (ss == null) {
            // we need to create these then
            val ms = getMinimumDistanceBetween(de, Side.START, de, Side.END, d, null, false)

            val l = C2RectangularSlideable(cso, d, de, Side.START)
            val r = C2RectangularSlideable(cso, d, de, Side.END)
            cso.ensureMinimumDistance(l, r, ms.toInt())

            ss = RectangularSlideableSetImpl(de, l, r)

            ensureCentreSlideablePosition(cso, ss, cExisting)
            cso.add(de, ss)

            if (de is Container) {
                checkCreateItems(cso, de, d, de.getLayout(), ss, topGroup)
            }

            log.send("Created RectangularSlideableSetImpl: ${ss.d}", ss.getAll())
        }

        return ss
    }

    fun checkCreateItems(
        cso: C2SlackOptimisation,
        de: Container,
        d: Dimension,
        l: Layout?,
        container: RectangularSlideableSet,
        topGroup: Group?
    ) {

        val contents = de.getContents()
            .filterIsInstance<ConnectedRectangular>()

        val relyOnGroupLayout = usingGroups(contents, topGroup)

//        var central = if (requiresSharedCentralSlideable(l, d) && !relyOnGroupLayout) {
//            C2IntersectionSlideable(cso, d, contents)
//        } else {
//            null
//        }

        val contentMap = contents.map { it to checkCreate(it, d, cso, null, topGroup) }

        // ensure within container
        contentMap.forEach { (e, v) -> embed(d, container, v, cso, e) }

        // ensure internal ordering
        if (!relyOnGroupLayout) {
            when (l) {
                Layout.RIGHT, Layout.HORIZONTAL, null -> if (d == Dimension.H) setupInternalOrdering(contentMap, d, cso)
                Layout.LEFT -> if (d == Dimension.H) setupInternalOrdering(contentMap.reversed(), d, cso)
                Layout.DOWN, Layout.VERTICAL -> if (d == Dimension.V) setupInternalOrdering(contentMap, d, cso)
                Layout.UP -> if (d == Dimension.V) setupInternalOrdering(contentMap.reversed(), d, cso)
                Layout.GRID -> {
                    // grid, don't handle this yet
                }
            }
        } else {
            log.send("Using groups: $de")
        }
    }

    abstract fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group?): Boolean


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


    private fun requiresSharedCentralSlideable(l: Layout?, d: Dimension): Boolean {
        return when (l) {
            null, Layout.GRID -> false
            Layout.DOWN, Layout.UP, Layout.VERTICAL -> return d == Dimension.V
            Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> return d== Dimension.H
        }
    }

}