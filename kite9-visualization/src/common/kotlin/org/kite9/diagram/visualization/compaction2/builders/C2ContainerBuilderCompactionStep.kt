package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
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
        checkCreate(c.getDiagram(), Dimension.H, c.getSlackOptimisation(Dimension.H))
        checkCreate(c.getDiagram(), Dimension.V, c.getSlackOptimisation(Dimension.V))

    }

    private fun checkCreate(de: DiagramElement, d: Dimension, cso: C2SlackOptimisation): RectangularSlideableSet? {
        if (de !is Rectangular) {
            return null
        }

        var ss = cso.getSlideablesFor(de) as RectangularSlideableSet?

        if (ss == null) {
            // we need to create these then
            val ms = getMinimumDistanceBetween(de, Side.START, de, Side.END, d, null, false)

            val l = C2Slideable(cso, d, Purpose.EDGE, de, Side.START)
            val r = C2Slideable(cso, d, Purpose.EDGE, de, Side.END)
            cso.ensureMinimumDistance(l, r, ms.toInt())
            ss = RectangularSlideableSetImpl(listOf(l, r))
            cso.add(de, ss)
            log.send("Created RectangularSlideableSetImpl: ", ss.getAll())
        }

        if (de is Container) {
            checkCreateItems(cso, de, d, de.getLayout(), ss)
        }

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
            .filterIsInstance<Rectangular>()
            .map { it to checkCreate(it, d, cso) }

        val orderedContents = contents.filter { (k, _) -> k is Connected }

        // ensure within container
        orderedContents
            .forEach { (k, v) ->
                val distL = getMinimumDistanceBetween(de, Side.START, k, Side.START, d, null, true)
                val distR = getMinimumDistanceBetween(de, Side.END, k, Side.END, d, null, true)
                separateRectangular(container, Side.START, v!!, Side.START, cso, distL)
                separateRectangular(v!!, Side.END, container, Side.END, cso, distR)
            }

        // ensure internal ordering
        when (l) {
            Layout.RIGHT, Layout.HORIZONTAL -> if (d == Dimension.H) setupInternalOrdering(orderedContents, d, cso)
            Layout.LEFT -> if (d == Dimension.H) setupInternalOrdering(orderedContents.reversed(), d, cso)
            Layout.DOWN, Layout.VERTICAL -> if (d == Dimension.V) setupInternalOrdering(orderedContents, d, cso)
            Layout.UP -> if (d == Dimension.V) setupInternalOrdering(orderedContents.reversed(), d, cso)
            else -> { /* do nothing */ }
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
            a.getRectangularsOnSide(aSide).forEach { containerSlideable ->
                b.getRectangularsOnSide(bSide).forEach { elementSlideable ->
                    cso.ensureMinimumDistance(containerSlideable, elementSlideable, dist.toInt())
                }
            }
        }
    }