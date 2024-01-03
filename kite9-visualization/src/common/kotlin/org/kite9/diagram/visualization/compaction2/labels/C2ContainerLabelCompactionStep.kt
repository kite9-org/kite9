package org.kite9.diagram.visualization.compaction2.labels

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.builders.AbstractC2BuilderCompactionStep
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2ContainerLabelCompactionStep(cd: CompleteDisplayer, gp: GridPositioner) : AbstractC2BuilderCompactionStep(cd, gp)  {

    private var firstGroup = true
    override fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group?): Boolean {
        return false
    }

    override fun compact(c: C2Compaction, g: Group) {
        if (firstGroup) {
            addLabels(c.getDiagram(), c)
            firstGroup = false
        }
    }

    private fun addLabels(c: Container, c2: C2Compaction) {
        // handle recursion
        c.getContents()
            .filterIsInstance<Container>()
            .forEach { addLabels(it, c2) }

        val allContent = c.getContents()
            .filterIsInstance<ConnectedRectangular>()

        val allLabels = c.getContents()
            .filterIsInstance<Label>()

        val labelsBySide = allLabels.groupBy { it.getLabelPlacement() ?: Direction.DOWN }

        val leftLabels = labelsBySide.get(Direction.LEFT).orEmpty()
        addLabelsToSide(Direction.LEFT, leftLabels, c, c2, allContent)

        val rightLabels = labelsBySide.get(Direction.RIGHT).orEmpty()
        addLabelsToSide(Direction.RIGHT, rightLabels, c, c2, allContent.plus(leftLabels))

        val upLabels = labelsBySide.get(Direction.UP).orEmpty()
        addLabelsToSide(Direction.UP, upLabels, c, c2, allContent.plus(leftLabels).plus(rightLabels))

        val downLabels = labelsBySide.get(Direction.DOWN).orEmpty()
        addLabelsToSide(Direction.DOWN, downLabels, c, c2, allContent.plus(leftLabels).plus(rightLabels).plus(upLabels))
    }

    private fun createSlideables(cso: C2SlackOptimisation, de: Label, d: Dimension) : RectangularSlideableSet {
        log.send("Creating $de")

        return checkCreate(de, d, cso, null, null)!!
    }

    private fun addLabelsToSide(d: Direction, ll: List<Label>, c: Container, c2: C2Compaction, allContent: List<Rectangular>) {
        if (ll.isEmpty()) {
            return
        }
        val csoh = c2.getSlackOptimisation(Dimension.H)
        val csov = c2.getSlackOptimisation(Dimension.V)

        // label slideable sets
        val hss = ll.associateWith { createSlideables(csoh, it, Dimension.H) }
        val vss = ll.associateWith { createSlideables(csov, it, Dimension.V) }

        // container slideable sets
        val cssh = csoh.getSlideablesFor(c)
        val cssv = csov.getSlideablesFor(c)

        // content slideable sets
        val hssContent = allContent.associateWith { csoh.getSlideablesFor(it) }
        val vssContent = allContent.associateWith { csov.getSlideablesFor(it) }

        if ((cssh == null) || (cssv == null)) {
            // can't place labels if container is invisible
            return
        }

        when (d) {
            Direction.RIGHT -> {
                log.send("Elements with direction RIGHT: ",ll)
                val ordered = vss.values.toList()
                separateItems(ordered, Dimension.V, csov, cssv)
                mergeSlideables(cssh.r, hss.values.map { it.r }, csoh)
                ensureSeparated(hssContent.values, hss.values, Dimension.H, csoh)
            }
            Direction.LEFT -> {
                log.send("Elements with direction RIGHT: ",ll)
                val ordered = vss.values.toList()
                separateItems(ordered, Dimension.V, csov, cssv)
                mergeSlideables(cssh.l, hss.values.map { it.l }, csoh)
                ensureSeparated(hss.values, hssContent.values, Dimension.H, csoh)
            }
            Direction.DOWN -> {
                log.send("Elements with direction DOWN: ",ll)
                val ordered = hss.values.toList()
                separateItems(ordered, Dimension.H, csoh, cssh)
                mergeSlideables(cssv.r, vss.values.map { it.r }, csov)
                ensureSeparated(vssContent.values, vss.values, Dimension.V, csov)
            }
            Direction.UP -> {
                log.send("Elements with direction UP: ",ll)
                val ordered = hss.values.toList()
                separateItems(ordered, Dimension.H, csoh, cssh)
                mergeSlideables(cssv.l, vss.values.map { it.l }, csov)
                ensureSeparated(vss.values, vssContent.values, Dimension.V, csov)
            }
            else -> {

            }
        }


    }

    private fun ensureSeparated(above: Collection<RectangularSlideableSet?>, below: Collection<RectangularSlideableSet?>, d: Dimension, cso: C2SlackOptimisation) {
        above.filterNotNull().forEach {a ->
            val deA = a.d

            below.filterNotNull().forEach { b ->
                val deB = b.d
                val dist = getMinimumDistanceBetween(deA, Side.END, deB, Side.START, d, null, false)
                cso.ensureMinimumDistance(a.r, b.l, dist.toInt())
            }
        }
    }

    private fun mergeSlideables(r: C2RectangularSlideable, map: List<C2RectangularSlideable>, cso: C2SlackOptimisation) {
        var out = r

        map.forEach {
            out = cso.mergeSlideables(out, it)
        }
    }

    private fun separateItems(values: List<RectangularSlideableSet>, d: Dimension, cso: C2SlackOptimisation, containerSS: RectangularSlideableSet) {
        var prev : RectangularSlideableSet? = null
        values.forEach {
            val left = prev
            if (left != null) {
                separateTwo(left, it, d, cso)
            } else {
                separateInternalStart(containerSS, it, d, cso)
            }

            prev = it
        }

        separateInternalEnd(containerSS, values.last(), d, cso)
    }

    private fun separateTwo(
        left: RectangularSlideableSet,
        right: RectangularSlideableSet,
        d: Dimension,
        cso: C2SlackOptimisation
    ) {
        val from = left.r
        val to = right.l
        val dist = getMinimumDistanceBetween(left.d, Side.END, right.d, Side.START, d, null, false)
        cso.ensureMinimumDistance(from, to, dist.toInt())
    }

    private fun separateInternalStart(
        outer: RectangularSlideableSet,
        inner: RectangularSlideableSet,
        d: Dimension,
        cso: C2SlackOptimisation
    ) {
        val from = outer.l
        val to = inner.l
        val dist = getMinimumDistanceBetween(outer.d, Side.START, inner.d, Side.START, d, null, true)
        cso.ensureMinimumDistance(from, to, dist.toInt())
    }

    private fun separateInternalEnd(
        outer: RectangularSlideableSet,
        inner: RectangularSlideableSet,
        d: Dimension,
        cso: C2SlackOptimisation
    ) {
        val from = inner.r
        val to = outer.r
        val dist = getMinimumDistanceBetween(inner.d, Side.END, outer.d, Side.START, d, null, true)
        cso.ensureMinimumDistance(from, to, dist.toInt())
    }

    override val prefix = "C2LB"

    override val isLoggingEnabled = true

}