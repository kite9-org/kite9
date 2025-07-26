package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.anchors.IntersectAnchor
import org.kite9.diagram.visualization.compaction2.anchors.OrbitAnchor
import org.kite9.diagram.visualization.compaction2.anchors.RectAnchor
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSetImpl
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSetImpl
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

abstract class AbstractC2BuilderCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {


    /**
     * This is used to create a RectangularSlideableSet from a diagram element
     * where the element doesn't have a Group
     */
    protected fun checkCreateElement(
        de: DiagramElement,
        d: Dimension,
        cso: C2SlackOptimisation,
        cExisting: C2Slideable?,
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

            val l = C2Slideable(cso, d, de, Side.START)
            val r = C2Slideable(cso, d, de, Side.END)
            cso.ensureMinimumDistance(l, r, ms.toInt())

            ss = RectangularSlideableSetImpl(de, l, r)

            ensureCentreSlideablePosition(cso, ss, cExisting)
            cso.add(de, ss)

            if (de is Container) {
                checkCreateElementContentItems(cso, de, d, de.getLayout(), ss, topGroup)
            }

            log.send("Created RectangularSlideableSetImpl: ${ss.d}", ss.getAll())

            val lg = if (topGroup is LeafGroup) topGroup else null
            if (de.getParent() != null) {
                val ssr = ss!!.wrapInRoutable(cso.compaction, lg)
                cso.contains(ssr, ss)
            }
        }

        return ss
    }

    private fun checkCreateElementContentItems(
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
        val contentMap = contents.map { it to checkCreateElement(it, d, cso, null, topGroup) }

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
}