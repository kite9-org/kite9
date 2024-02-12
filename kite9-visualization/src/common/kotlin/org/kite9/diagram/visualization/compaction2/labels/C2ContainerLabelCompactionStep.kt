package org.kite9.diagram.visualization.compaction2.labels

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.builders.AbstractC2BuilderCompactionStep
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
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

        val labelsBySide = allLabels
            .groupBy { it.getLabelPlacement() ?: Direction.DOWN }
            .map { (k, ll) ->
                k to when (k) {
                    Direction.LEFT, Direction.RIGHT -> ll.sortedBy { it.getHorizontalAlignment().ordinal }
                    Direction.DOWN, Direction.UP -> ll.sortedBy { it.getVerticalAlignment().ordinal }
                }
            }
            .toMap()

        val leftLabels = labelsBySide.get(Direction.LEFT).orEmpty()
        addLabelsToSide(Direction.LEFT, leftLabels, c, c2, allContent)

        val rightLabels = labelsBySide.get(Direction.RIGHT).orEmpty()
        addLabelsToSide(Direction.RIGHT, rightLabels, c, c2, allContent.plus(leftLabels))

        val upLabels = labelsBySide.get(Direction.UP).orEmpty()
        addLabelsToSide(Direction.UP, upLabels, c, c2, allContent.plus(leftLabels).plus(rightLabels))

        val downLabels = labelsBySide.get(Direction.DOWN).orEmpty()
        addLabelsToSide(Direction.DOWN, downLabels, c, c2, allContent.plus(leftLabels).plus(rightLabels).plus(upLabels))
    }

    private fun createSlideables(cso: C2SlackOptimisation, ll: Set<Label>, d: Dimension, dd: Direction) : List<RectangularSlideableSet> {
        val needsIntersection = d.isHoriz() == Direction.isHorizontal(dd)
        val c = if (needsIntersection) C2IntersectionSlideable(cso, d, ll) else null
        return ll.map {
            log.send("Creating $it")
            checkCreate(it, d, cso, c, null)!!
        }
    }

    private fun addLabelsToSide(d: Direction, ll: List<Label>, c: Container, c2: C2Compaction, allContent: List<Rectangular>) {
        if (ll.isEmpty()) {
            return
        }
        val csoh = c2.getSlackOptimisation(Dimension.H)
        val csov = c2.getSlackOptimisation(Dimension.V)

        // label slideable sets
        val hss = createSlideables(csoh, ll.toSet(), Dimension.H, d)
        val vss = createSlideables(csov, ll.toSet(), Dimension.V, d)

        // container slideable sets
        val cssh = csoh.getSlideablesFor(c)
        val cssv = csov.getSlideablesFor(c)

        // content slideable sets
        val hssContent = allContent.map { csoh.getSlideablesFor(it) }
        val vssContent = allContent.map { csov.getSlideablesFor(it) }

        if ((cssh == null) || (cssv == null)) {
            // can't place labels if container is invisible
            return
        }

        when (d) {
            Direction.RIGHT -> {
                log.send("Elements with direction RIGHT: ",ll)
                separateItems(vss, Dimension.V, csov, cssv)
                ensureSeparateInternal(hss, cssh, Dimension.H, csoh)
                ensureSeparated(hssContent, hss, Dimension.H, csoh)
            }
            Direction.LEFT -> {
                log.send("Elements with direction RIGHT: ",ll)
                separateItems(vss, Dimension.V, csov, cssv)
                ensureSeparateInternal(hss, cssh, Dimension.H, csoh)
                ensureSeparated(hss, hssContent, Dimension.H, csoh)
            }
            Direction.DOWN -> {
                log.send("Elements with direction DOWN: ",ll)
                separateItems(hss, Dimension.H, csoh, cssh)
                ensureSeparated(vssContent, vss, Dimension.V, csov)
                ensureSeparateInternal(vss, cssv, Dimension.V, csov)
            }
            Direction.UP -> {
                log.send("Elements with direction UP: ",ll)
                separateItems(hss, Dimension.H, csoh, cssh)
                ensureSeparateInternal(vss, cssv, Dimension.V, csov)
                ensureSeparated(vss, vssContent, Dimension.V, csov)
            }
        }


    }

    private fun ensureSeparated(above: Collection<RectangularSlideableSet?>, below: Collection<RectangularSlideableSet?>, d: Dimension, cso: C2SlackOptimisation) {
        above.filterNotNull().forEach {a ->
            val deA = a.d

            below.filterNotNull().forEach { b ->
                val deB = b.d
                val dist = getMinimumDistanceBetween(deA, Side.END, deB, Side.START, d, null, true)
                cso.ensureMinimumDistance(a.r, b.l, dist.toInt())
            }
        }
    }

    private fun ensureSeparateInternal(inside: Collection<RectangularSlideableSet?>, o: RectangularSlideableSet, d: Dimension, cso: C2SlackOptimisation) {
        val deOut = o.d

        inside.filterNotNull().forEach { i ->
            val deIn = i.d
            val distUp = getMinimumDistanceBetween(deOut, Side.START, deIn, Side.START, d, null, true)
            val distDown = getMinimumDistanceBetween(deIn, Side.END, deOut, Side.END, d, null, true)
            cso.ensureMinimumDistance(o.l, i.l, distUp.toInt())
            cso.ensureMinimumDistance(i.r, o.r, distDown.toInt())
        }
    }

//    private fun mergeSlideables(r: C2RectangularSlideable, map: List<C2RectangularSlideable>, cso: C2SlackOptimisation) {
//        var out = r
//
//        map.forEach {
//            out = cso.mergeSlideables(out, it)
//        }
//    }

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