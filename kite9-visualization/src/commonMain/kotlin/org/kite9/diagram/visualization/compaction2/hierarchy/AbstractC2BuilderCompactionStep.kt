package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.AlignedRectangular
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.model.style.ContainerPosition
import org.kite9.diagram.model.style.GridContainerPosition
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.anchors.Permeability
import org.kite9.diagram.visualization.compaction2.anchors.RectAnchor
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSetImpl
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

abstract class AbstractC2BuilderCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    private val gridSlideables = mutableMapOf<Triple<Container, Int, Dimension>, C2Slideable>()

    /**
     * This is used to create a RectangularSlideableSet from a diagram element
     * where the element doesn't have a Group
     */
    protected fun checkCreateElement(
        de: Rectangular,
        d: Dimension,
        cso: C2SlackOptimisation,
        cExisting: C2Slideable?,
        topGroup: Group?,
    ): RectangularSlideableSet {

        fun getGridSlideable(
            cso: C2SlackOptimisation,
            c: DiagramElement?,
            d: Dimension,
            containerPosition: ContainerPosition?,
            de: Rectangular,
            s: Side,
            p: Permeability
        ): C2Slideable {
            val lineNumber = if (s == Side.START) {
                (containerPosition as GridContainerPosition).getFrom()
            } else {
                (containerPosition as GridContainerPosition).getTo() + 1
            }

            val key = Triple(c!! as Container, lineNumber, d)
            val existing = gridSlideables.get(key)
            if (existing == null) {
                val new = C2Slideable(cso, d, de, s, p)
                gridSlideables[key] = new
                return new
            } else {
                // just add the anchor
                existing.addRectAnchor(RectAnchor(de, s, p))
                return existing
            }
        }

        var ss = cso.getSlideablesFor(de)

        if (ss == null) {
            val parentLayoutIsGrid = (de.getParent() as? Container)?.getLayout() == Layout.GRID
            log.send("Creating $de")

            // we need to create these then
            val ms = getMinimumDistanceBetween(de, Side.START, de, Side.END, d, null, false)

            val l = if (parentLayoutIsGrid) {
                getGridSlideable(cso, de.getParent(), d, de.getContainerPosition(d), de,Side.START, getRectangularPermeability(de, d, false))
            } else {
                C2Slideable(cso, d, de, Side.START, getRectangularPermeability(de, d, false))
            }
            val r = if(parentLayoutIsGrid) {
                getGridSlideable(cso, de.getParent(), d, de.getContainerPosition(d), de,Side.END, getRectangularPermeability(de, d, true))
            } else {
                C2Slideable(cso, d, de, Side.END, getRectangularPermeability(de, d, true))
            }

            ss = RectangularSlideableSetImpl(de, l, r)

            val position = if (de is AlignedRectangular) de.getConnectionAlignment(d) else Placement.NONE

            ensureCentreSlideablePosition(cso, ss, cExisting, position)
            cso.add(de, ss)

            cso.ensureMinimumDistance(l, r, ms.toInt())

            if (de is Container) {
                checkCreateElementContentItems(cso, de, d, de.getLayout(), ss, topGroup)
            }

            log.send("Created RectangularSlideableSetImpl: ${ss.e}", ss.getAll())

        }

        return ss
    }



    /**
     * This works out whether we can route connections through this element (and in which direction).
     */
    fun getRectangularPermeability(de: Rectangular, d: Dimension, increasing: Boolean): Permeability {
        val direction = Direction.getDirection(d, increasing)
        val rule = if (de is Container) {
            val bt = de.getTraversalRule(direction)
            when (bt) {
                BorderTraversal.ALWAYS -> Permeability.ALL
                BorderTraversal.LEAVING ->
                    if (increasing) {
                        Permeability.INCREASING
                    } else {
                        Permeability.DECREASING
                    }
                BorderTraversal.PREVENT -> Permeability.NONE
            }
        } else {
            Permeability.NONE
        }

        return rule
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