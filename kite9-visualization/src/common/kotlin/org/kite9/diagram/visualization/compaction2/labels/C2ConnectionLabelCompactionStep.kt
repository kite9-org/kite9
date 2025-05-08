package org.kite9.diagram.visualization.compaction2.labels

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.Side.*
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.anchors.AnchorType
import org.kite9.diagram.visualization.compaction2.hierarchy.AbstractC2BuilderCompactionStep
import org.kite9.diagram.visualization.compaction2.routing.C2Point
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2ConnectionLabelCompactionStep(cd: CompleteDisplayer, gp: GridPositioner) :
    AbstractC2BuilderCompactionStep(cd) {

    override val prefix = "C2LA"
    override val isLoggingEnabled = true

    private fun prepareConnectedMap(c2: C2Compaction) : Map<Pair<Connected, Side>, Set<Label>> {
        val out = mutableMapOf<Pair<Connected, Side>, MutableSet<Label>>()

        fun ensureMapping(ll: Label?, s: Side, c: Connected) {
            if (ll != null) {
                val p = Pair(c, s)
                val l = out.getOrElse(p) { mutableSetOf() }
                l.add(ll)
                out[p] = l
            }
        }

        fun getSide(e: Connected, c: Connection, so2: C2SlackOptimisation) : Side {
            val ss = so2.getSlideablesFor(e)

            if (ss != null) {
                if (ss.l.getConnAnchors().find { it.e == c } != null) {
                    return START
                } else if (ss.r.getConnAnchors().find { it.e == c } != null) {
                    return END
                }
            }

            return NEITHER
        }

        fun doDimension(so1: C2SlackOptimisation, so2: C2SlackOptimisation) {
            so1.getAllSlideables().forEach { s ->
                s.getConnAnchors().forEach { ca ->
                    val e = ca.e

                    val from = e.getFrom()
                    val fromSide = getSide(from, e, so2)
                    if (fromSide != NEITHER) {
                        ensureMapping(e.getFromLabel(), fromSide, from)
                    }

                    val to = e.getTo()
                    val toSide = getSide(to, e, so2)
                    if (toSide != NEITHER) {
                        ensureMapping(e.getToLabel(), toSide, to)
                    }
                }
            }
        }

        doDimension(c2.getSlackOptimisation(Dimension.V), c2.getSlackOptimisation(Dimension.H))
        doDimension(c2.getSlackOptimisation(Dimension.H), c2.getSlackOptimisation(Dimension.V))

        return out
    }

    private fun prepareLabelMap(so1: C2SlackOptimisation): Map<Label, C2Slideable> {
        val out = mutableMapOf<Label, C2Slideable>()
        so1.getAllSlideables().forEach { s -> s.getConnAnchors().forEach { ca ->
                val idx = ca.s
                if (ca.type == AnchorType.TERMINAL) {
                    val label = if (idx == 0.0f)  ca.e.getToLabel() else ca.e.getFromLabel()
                    if ((label != null) && (!out.containsKey(label))) {
                        out[label] = s
                    }
                } else if (ca.type == AnchorType.AFTER_FAN) {
                    // we're doing fanning- overrides terminal labelling
                    val label = if (idx == 0.4f) ca.e.getToLabel() else ca.e.getFromLabel()
                    if (label != null) {
                        out[label] = s
                    }
                }
            }
        }

        return out
    }


    private fun prepareArrivalSideMap(c2: C2Compaction) : Map<Label, Direction> {
        val out = mutableMapOf<Label, Direction>()

        fun storeArrivalSide(l: Label?, r: Connected, s: C2Slideable, hv: Dimension) {
            if (l == null) {
                return
            }

            s.getRectangulars().forEach {
                if (it.e == r) {
                    val d = if (it.s == START) {
                        if (hv == Dimension.V) {
                            Direction.UP
                        } else {
                            Direction.LEFT
                        }
                    } else {
                        if (hv == Dimension.V) {
                            Direction.DOWN
                        } else {
                            Direction.RIGHT
                        }
                    }
                    out[l] = d
                }
            }
        }

        fun doDimension(so1: C2SlackOptimisation, d: Dimension) {
            so1.getAllSlideables().forEach { s ->
                s.getConnAnchors()
                    .filter {it.type == AnchorType.TERMINAL}
                    .forEach { ca ->
                        val e = ca.e
                        storeArrivalSide(e.getFromLabel(), e.getFrom(), s, d)
                        storeArrivalSide(e.getToLabel(), e.getTo(), s, d)
                }
            }
        }

        doDimension(c2.getSlackOptimisation(Dimension.V), Dimension.V)
        doDimension(c2.getSlackOptimisation(Dimension.H), Dimension.H)

        return out
    }

    override fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group?) = false

    override fun compact(c: C2Compaction, g: Group) {

        val vLabelMap = prepareLabelMap(c.getSlackOptimisation(Dimension.V))
        val hLabelMap = prepareLabelMap(c.getSlackOptimisation(Dimension.H))

        val directionMap = prepareArrivalSideMap(c)
        val connectedMap = prepareConnectedMap(c)

        label1d(c, c.getSlackOptimisation(Dimension.V), c.getSlackOptimisation(Dimension.H), connectedMap, directionMap, hLabelMap, vLabelMap, Direction.LEFT)
        label1d(c, c.getSlackOptimisation(Dimension.V), c.getSlackOptimisation(Dimension.H), connectedMap, directionMap, hLabelMap, vLabelMap, Direction.RIGHT)
        label1d(c, c.getSlackOptimisation(Dimension.H), c.getSlackOptimisation(Dimension.V), connectedMap, directionMap, hLabelMap, vLabelMap, Direction.UP)
        label1d(c, c.getSlackOptimisation(Dimension.H), c.getSlackOptimisation(Dimension.V), connectedMap, directionMap, hLabelMap, vLabelMap, Direction.DOWN)

    }

    private fun getLabelOrder(c: Pair<Connected, Side>, allEdges: List<Pair<C2Slideable, C2Slideable>>) : List<Pair<C2Slideable, C2Slideable>> {

        fun compareLabelOrder(a: Pair<C2Slideable, C2Slideable>, b: Pair<C2Slideable, C2Slideable>) : Int {
            val aFanAnchor = (a.first.getFanAnchors() + a.second.getFanAnchors()).find { it.e == c.first }
            val bFanAnchor = (b.first.getFanAnchors() + b.second.getFanAnchors()).find { it.e == c.first }
            if ((aFanAnchor != null) && (bFanAnchor != null)) {
                return aFanAnchor.s.compareTo(bFanAnchor.s)
            } else {
                throw LogicException("Something went wrong")
            }

        }

        val out = allEdges.sortedWith {  a, b ->  compareLabelOrder(a, b) }
        return out
    }


    fun label1d(
        co: C2Compaction,
        so: C2SlackOptimisation,
        so2: C2SlackOptimisation,
        connectedMap: Map<Pair<Connected, Side>,  Set<Label>>,
        directionMap: Map<Label, Direction>,
        hLabelMap:  Map<Label, C2Slideable>,
        vLabelMap: Map<Label, C2Slideable>,
        d: Direction) {
        connectedMap.forEach { (c, labels) ->
            // collect all labels
            val toDo = labels.filter { directionMap.get(it) == d }

            if (toDo != null) {
                // the list of all labels on one side.

                val allEdges = toDo.map { label ->
                    val vSlideable = getNonDoneVersion(vLabelMap[label]!!)
                    val hSlideable = getNonDoneVersion(hLabelMap[label]!!)
                    handleLabel(label, C2Point(vSlideable, hSlideable, Direction.DOWN), co, d, c)
                }

                val sortedEdges = getLabelOrder(c, allEdges)

                // separate edges
                var prev : Pair<C2Slideable, C2Slideable>? = null
                sortedEdges.forEach { e ->
                    if (prev != null) {
                        so2.ensureMinimumDistance(prev!!.second, e.first, 5)
                    }
                    prev = e
                }
            }

        }
    }


    private fun getNonDoneVersion(c2Slideable: C2Slideable): C2Slideable {
        while (c2Slideable.isDone()) {
            return getNonDoneVersion(c2Slideable.mergedInto!!)
        }

        return c2Slideable
    }

    private fun getUpdatedSlideable(
        old: C2Slideable,
        so: C2SlackOptimisation,
        conn: Connection
    ): C2Slideable? {
        val connAnchor = old.getConnAnchors().first { it.e == conn }
        return so.getAllSlideables()
            .firstOrNull { it.getConnAnchors().contains(connAnchor) }
    }

    private fun handleLabel(l: Label, start: C2Point, c2: C2Compaction, d: Direction, p: Pair<Connected, Side>) : Pair<C2Slideable, C2Slideable> {
        val csoh = c2.getSlackOptimisation(Dimension.H)
        val csov = c2.getSlackOptimisation(Dimension.V)

        val rssh = checkCreateElement(l, Dimension.H, csoh, null, null)!!
        val rssv = checkCreateElement(l, Dimension.V, csov, null, null)!!

        val horiz = Direction.isHorizontal(d)
        val hc = (if (!horiz) start.getAlong() else start.getPerp())
        val vc = (if (!horiz) start.getPerp() else start.getAlong())
        val dest = p.first

        // really simple merge for now
        val leftBuffer = getOrbitSlideable(dest, START, csoh)!!
        val rightBuffer = getOrbitSlideable(dest, END, csoh)!!
        val topBuffer = getOrbitSlideable(dest, START, csov)!!
        val bottomBuffer = getOrbitSlideable(dest, END, csov)!!

        val out = when (d) {
            Direction.UP -> {
                ensureDistance(l, dest, Dimension.H, csov)
                ensureDistanceFromBuffer(topBuffer, l, Dimension.H, csov)
                val left = csoh.mergeSlideables(hc, rssh.l)!!
                csov.mergeSlideables(vc, rssv.r)
                Pair(left, rssh.r)
            }

            Direction.DOWN -> {
                ensureDistance(dest, l, Dimension.H, csov)
                ensureDistanceFromBuffer(l, bottomBuffer, Dimension.H, csov)
                val left = csoh.mergeSlideables(hc, rssh.l)!!
                csov.mergeSlideables(vc, rssv.l)
                Pair(left, rssh.r)
            }

            Direction.LEFT -> {
                ensureDistance(l, dest, Dimension.V, csoh)
                ensureDistanceFromBuffer(leftBuffer, l, Dimension.V, csoh)
                val upper = csov.mergeSlideables(vc, rssv.l)!!
                csoh.mergeSlideables(hc, rssh.r)
                Pair(upper, rssv.r)
            }

            Direction.RIGHT -> {
                ensureDistance(dest, l, Dimension.V, csoh)
                ensureDistanceFromBuffer(l, rightBuffer, Dimension.V, csoh)
                val upper = csov.mergeSlideables(vc, rssv.l)!!
                csoh.mergeSlideables(hc, rssh.l)
                Pair(upper, rssv.r)
            }
        }

        inside(l, dest, Dimension.H, csoh)
        inside(l, dest, Dimension.V, csov)
        return out
    }

    private fun ensureDistanceFromBuffer(bs: C2Slideable, to: Positioned, d: Dimension, so: C2SlackOptimisation) {
        val rs = getRectangularSlideable(to, START, so)!!
        so.ensureMinimumDistance(bs, rs, 0)
        bs.getForwardSlideables(false)
            .forEach { ls ->
                ensureMinimumDistanceBetweenRectangularSlideables(ls, rs, d, so)
            }
    }

    private fun ensureDistanceFromBuffer(
        from: Positioned,
        bs: C2Slideable,
        d: Dimension,
        so: C2SlackOptimisation
    ) {
        val ls = getRectangularSlideable(from, END, so)!!
        so.ensureMinimumDistance(ls, bs, 0)
        bs.getForwardSlideables(true)
            .forEach { rs ->
                ensureMinimumDistanceBetweenRectangularSlideables(ls, rs, d, so)
            }
    }

    private fun ensureMinimumDistanceBetweenRectangularSlideables(
        from: C2Slideable,
        to: C2Slideable,
        d: Dimension,
        so: C2SlackOptimisation
    ) {
        val dist = from.getRectangulars()
            .maxOfOrNull { fa ->
                to.getRectangulars()
                    .maxOfOrNull { ta -> getMinimumDistanceBetween(fa.e, fa.s, ta.e, ta.s, d, null, true).toInt() } ?: 0
            } ?: 0


        so.ensureMinimumDistance(from, to, dist)
    }


    private fun ensureDistance(from: Positioned, to: Positioned, d: Dimension, so: C2SlackOptimisation) {
        val ld = getMinimumDistanceBetween(from, END, to, START, d, null, true).toInt()
        val fromSS = so.getSlideablesFor(from)!!
        val toSS = so.getSlideablesFor(to)!!
        so.ensureMinimumDistance(fromSS.r, toSS.l, ld)
    }

    private fun getOrbitSlideable(de: Positioned, side: Side, so: C2SlackOptimisation): C2Slideable? {
        return so.getAllSlideables()
            .firstOrNull { os -> os.getOrbits().any { it.e == de && it.s == side } }
    }

    private fun getRectangularSlideable(de: Positioned, side: Side, so: C2SlackOptimisation): C2Slideable? {
        return so.getAllSlideables()
            .firstOrNull { os -> os.getRectangulars().any { it.e == de && it.s == side } }
    }

    private fun inside(innerDe: Label, outerDe: Connected, d: Dimension, so: C2SlackOptimisation) {

        val bl = getOrbitSlideable(outerDe, START, so)!!
        val br = getOrbitSlideable(outerDe, END, so)!!

        val inner = so.getSlideablesFor(innerDe)!!

        if (inner.l != bl) {
            ensureDistanceFromBuffer(bl, innerDe, d, so)
        }

        if (inner.r != br) {
            ensureDistanceFromBuffer(innerDe, br, d, so)
        }

    }

}