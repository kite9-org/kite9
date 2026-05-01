package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.common.hints.compareEitherXBounds
import org.kite9.diagram.common.hints.compareEitherYBounds
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.Layout.Companion.reverse
import org.kite9.diagram.model.position.Layout.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Layout.Companion.rotateClockwise
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * Provides the basic code for a top-down approach to laying out groups, but doesn't specify either
 * the algorithm used to choose the approach or the ordering of the groups.
 *
 * @author robmoffat
 */
abstract class AbstractTopDownLayoutStrategy(val rh: RoutableHandler2D) : LayoutStrategy, Logable {

    var log = Kite9Log.instance(this)

    private fun chooseBestCompoundGroupPlacement(gg: CompoundGroup) {
        val gt = gg.axis
        val ld = gg.getLayout()
        val canBeHoriz = gt.isHorizontal
        val canBeVert = gt.isVertical

        if (!canBeVert && !canBeHoriz) {
            // this is where we combine horizontal and vertical groups
            applyAxisGroupingRules(gg.b, gg.a)
            applyAxisGroupingRules(gg.a, gg.b)
            return
        }

        val horizLayoutUnknown = (ld == null || ld === Layout.HORIZONTAL) && canBeHoriz
        val vertLayoutUnknown = (ld == null || ld === Layout.VERTICAL) && canBeVert
        val canDecideLayout = horizLayoutUnknown || vertLayoutUnknown

        log.send("Group A: " + gg.a)
        log.send("Group B: " + gg.b)
        if (canDecideLayout) {
            val layoutNeeded =
                    groupsNeedLayout(gg.a, gg.b, horizLayoutUnknown, vertLayoutUnknown, ld)
            if (layoutNeeded) {
                // this is the expensive part - layout is required. Choose one
                val hintedLayout = getHintedLayout(gg, canBeHoriz, canBeVert, ld)
                var best: PlacementApproach? = null
                best =
                        tryPlacement(
                                gg,
                                best,
                                createDirectionOption(hintedLayout, horizLayoutUnknown, vertLayoutUnknown),
                                true
                        )
                best =
                        tryPlacement(
                                gg,
                                best,
                                createDirectionOption(
                                        reverse(hintedLayout),
                                        horizLayoutUnknown,
                                        vertLayoutUnknown
                                ),
                                false
                        )
                best =
                        tryPlacement(
                                gg,
                                best,
                                createDirectionOption(
                                        rotateClockwise(hintedLayout),
                                        horizLayoutUnknown,
                                        vertLayoutUnknown
                                ),
                                false
                        )
                best =
                        tryPlacement(
                                gg,
                                best,
                                createDirectionOption(
                                        rotateAntiClockwise(hintedLayout),
                                        horizLayoutUnknown,
                                        vertLayoutUnknown
                                ),
                                false
                        )
                best?.choose()
            }
        } else {
            val ld2 =
                    if ((ld == Layout.HORIZONTAL) || (ld == Layout.VERTICAL)) {
                        null
                    } else {
                        Layout.toDirection(ld)
                    }

            val pa = createPlacementApproach(gg, ld2, true)
            pa.choose()
            log.send("Group layout = $ld2")
        }
    }

    private fun applyAxisGroupingRules(setsGroup: Group, rulesGroup: Group) {

        fun effectiveLeaf(g: Group) : Boolean {
            if (g is LeafGroup) {
                return true
            } else {
                val axis = g.axis as DirectedGroupAxis
                val stillSingleAxis = axis.isVertical != axis.isHorizontal
                return !stillSingleAxis
            }
        }

        fun contentsShouldBeCombined(g: Group) : Boolean {
            val axis = g.axis as DirectedGroupAxis
            return axis.isAxisAligned
        }

        fun combineEffectiveLeaves(g: Group) : Set<Group> {
            return if (effectiveLeaf(g)) {
                setOf(g)
            } else if (g is CompoundGroup) {
                combineEffectiveLeaves(g.a) + combineEffectiveLeaves(g.b)
            } else {
                throw LogicException("Should be first if")
            }
        }

        fun separateEffectiveLeaves(g: Group) : Set<Set<Group>> {
            return if (contentsShouldBeCombined(g)) {
                setOf(combineEffectiveLeaves(g))
            } else if (g is CompoundGroup) {
                separateEffectiveLeaves(g.a) + separateEffectiveLeaves(g.b)
            } else {
                setOf(setOf(g))
            }
        }

        val leafGroups = separateEffectiveLeaves(setsGroup)
        val rules = mutableMapOf<Pair<Set<Group>, Set<Group>>, Layout>()

        fun scanRuleGroup(g: Group) : Set<Group> {
            if (!effectiveLeaf(g)) {
                val aLeaves = scanRuleGroup((g as CompoundGroup).a)
                val bLeaves = scanRuleGroup(g.b)
                val layout = g.getLayout()
                if (layout != null) {
                    val leafGroupsIntersectingA = leafGroups.filter { aLeaves.intersect(it).isNotEmpty() }
                    val leafGroupsIntersectingB = leafGroups.filter { bLeaves.intersect(it).isNotEmpty() }
                    leafGroupsIntersectingA.forEach { a ->
                        leafGroupsIntersectingB.forEach { b ->
                            rules[Pair(a, b)] = layout
                        }
                    }
                }
                return aLeaves+bLeaves
            } else {
                return setOf(g)
            }
        }

        scanRuleGroup(rulesGroup)

        fun <T, U> generatePairs(set1: Set<T>, set2: Set<U>): List<Pair<T, U>> {
            return set1.flatMap { element1 ->
                set2.map { element2 ->
                    Pair(element1, element2)
                }
            }
        }

        fun consolidateLayout(l: List<Layout>) : Layout? {
            return l.reduceOrNull { a, b ->
                return if (a == b) {
                    a
                } else {
                    when (a) {
                        Layout.HORIZONTAL -> when (b) {
                            Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> b
                            else -> null
                        }

                        Layout.VERTICAL -> when (b) {
                            Layout.UP, Layout.DOWN, Layout.VERTICAL -> b
                            else -> null
                        }

                        Layout.UP, Layout.DOWN -> when (b) {
                            Layout.VERTICAL -> a
                            else -> null
                        }

                        Layout.LEFT, Layout.RIGHT -> when (b) {
                            Layout.HORIZONTAL -> a
                            else -> null
                        }

                        else ->
                            throw LogicException("shouldn't be grid")
                    }
                }
            }
        }

        fun applyRuleGroup(g: Group) : Set<Group> {
            if (!effectiveLeaf(g)) {
                val aLeaves = applyRuleGroup((g as CompoundGroup).a)
                val bLeaves = applyRuleGroup(g.b)
                val layout = g.getLayout()
                if (Layout.toDirection(layout) == null) {
                    val leafGroupsIntersectingA = leafGroups.filter { aLeaves.intersect(it).isNotEmpty() }.toSet()
                    val leafGroupsIntersectingB = leafGroups.filter { bLeaves.intersect(it).isNotEmpty() }.toSet()
                    val overlap = leafGroupsIntersectingA.intersect(leafGroupsIntersectingB)

                    if (overlap.isEmpty()) {
                        val keys = generatePairs(leafGroupsIntersectingA, leafGroupsIntersectingB).toSet()
                        val rules = keys.map { rules[it] }.filterNotNull()
                        val consolidatedRule = consolidateLayout(rules)
                        if (consolidatedRule != null) {
                            g.setLayout(consolidatedRule)
                        }
                    }
                }
                return aLeaves + bLeaves
            } else {
                return setOf(g)
            }
        }

        applyRuleGroup(rulesGroup)
    }

    private fun createDirectionOption(naturalLayout: Layout?, horiz: Boolean, vert: Boolean): Direction? {
        return when (naturalLayout) {
            Layout.LEFT, Layout.RIGHT -> if (horiz) Layout.toDirection(naturalLayout) else null
            Layout.DOWN, Layout.UP -> if (vert) Layout.toDirection(naturalLayout) else null
            else ->
                    throw LogicException(
                            "Layout should be definite for an approach: $naturalLayout"
                    )
        }
    }

    /**
     * Layout can be hinted either by the [PositioningHints] of the groups, or by the ordinal order
     * of the elements in the container. If the layout of the container is HORIZONTAL or VERTICAL,
     * favour the ordinal, otherwise favour the [PositioningHints] where available.
     */
    private fun getHintedLayout(
            gg: CompoundGroup,
            setHoriz: Boolean,
            setVert: Boolean,
            prescribed: Layout?
    ): Layout? {
        var bx: Int? = null
        var by: Int? = null
        var out: Layout? = null
        val a = gg.a
        val b = gg.b
        if (setVert) {
            if (prescribed === Layout.VERTICAL) {
                return getVerticalOrdinalLayout(a, b)
            } else if (prescribed == null) {
                by = compareEitherYBounds(a.hints, b.hints)
                out = getVerticalOrdinalLayout(a, b)
            }
        }
        if (setHoriz) {
            if (prescribed === Layout.HORIZONTAL) {
                return getHorizontalOrdinalLayout(a, b)
            } else if (prescribed == null) {
                bx = compareEitherXBounds(a.hints, b.hints)
                out = getHorizontalOrdinalLayout(a, b)
            }
        }
        if (bx != null) {
            if (1 == bx) {
                return Layout.LEFT
            } else if (-1 == bx) {
                return Layout.RIGHT
            }
        }
        if (by != null) {
            if (1 == by) {
                return Layout.UP
            } else if (-1 == by) {
                return Layout.DOWN
            }
        }
        return out
    }

    private fun getHorizontalOrdinalLayout(a: Group, b: Group): Layout {
        return if (a.groupOrdinal < b.groupOrdinal) Layout.RIGHT else Layout.LEFT
    }

    private fun getVerticalOrdinalLayout(a: Group, b: Group): Layout {
        return if (a.groupOrdinal < b.groupOrdinal) Layout.DOWN else Layout.UP
    }

    private fun groupsNeedLayout(
            a: Group,
            b: Group,
            horizLayoutUnknown: Boolean,
            vertLayoutUnknown: Boolean,
            l: Layout?
    ): Boolean {
        if (groupsOverlap(a, b)) {
            return true
        }
//        val straightVerticals = groupsHaveStraightEdges(a, b, false)
//        if ((horizLayoutUnknown || l === Layout.VERTICAL) && straightVerticals) {
//            return true
//        }
//        val straightHorizontals = groupsHaveStraightEdges(a, b, true)
//        return if ((vertLayoutUnknown || l === Layout.HORIZONTAL) && straightHorizontals) {
//            true
//        } else false

        return false // TODO: decide if we need the above code.
    }

    private fun groupsHaveStraightEdges(a: Group, b: Group, horiz: Boolean): Boolean {
        val mask =
                DirectedLinkManager.createMask(
                        null,
                        false,
                        false,
                        if (horiz) Direction.LEFT else Direction.UP,
                        if (horiz) Direction.RIGHT else Direction.DOWN
                )
        val aHasLinks = a.linkManager.subset(mask).size > 0
        val bHasLinks = b.linkManager.subset(mask).size > 0
        return aHasLinks || bHasLinks
    }

    private fun groupsOverlap(a: Group, b: Group): Boolean {

        fun getLeafGroups(a: Group) : Set<LeafGroup> {
            if (a is LeafGroup) {
                return setOf(a)
            } else {
                return getLeafGroups((a as CompoundGroup).a) + getLeafGroups(a.b)
            }
        }

        val aLeaves = getLeafGroups(a)
        val bLeaves = getLeafGroups(b)

        val aPositions = aLeaves.map { rh.getPlacedPosition(it) }.toSet()
        val bPositions = bLeaves.map { rh.getPlacedPosition(it) }.toSet()
        val out= aPositions.intersect(bPositions).isNotEmpty()
        return out
    }

    private fun tryPlacement(
            gg: CompoundGroup,
            best: PlacementApproach?,
            d: Direction?,
            natural: Boolean
    ): PlacementApproach? {
        if (d != null && (best == null || best.score > 0)) {
            val newpl = createPlacementApproach(gg, d, natural)
            newpl.evaluate()
            log.send(if (log.go()) null else "${gg.groupNumber} going $d  score: ${newpl.score}")
            return if (best == null) {
                newpl
            } else if (best.score <= newpl.score + TOLERANCE) {
                best
            } else if (best.score >= newpl.score + TOLERANCE) {
                newpl
            } else {
                if (best.natural) {
                    best
                } else if (newpl.natural) {
                    newpl
                } else {
                    best
                }
            }
        }
        return best
    }

    protected abstract fun createPlacementApproach(
            gg: CompoundGroup,
            ld: Direction?,
            natural: Boolean
    ): PlacementApproach

    private fun chooseBestPlacement(lq: LayoutQueue) {
        var g = lq.poll()
        while (g != null) {
            log.send(
                    if (log.go()) null
                    else
                            "Ordering " +
                                    g.groupNumber +
                                    " size=" +
                                    g.size +
                                    " links=" +
                                    g.linkManager.linkCount
            )
            val out = StringBuilder(1000)
            log.send(out.toString())
            if (g is CompoundGroup) {
                val cg = g
                chooseBestCompoundGroupPlacement(cg)
                lq.complete(cg)
            }
            g = lq.poll()
        }
    }

    override val prefix: String
        get() = "TDLS"
    override val isLoggingEnabled: Boolean
        get() = true

    override fun layout(mr: GroupResult, lq: LayoutQueue) {

        fun offerAllGroups(g: Group) {
            if (g is CompoundGroup) {
                lq.offer(g)
                offerAllGroups(g.a)
                offerAllGroups(g.b)
            }
        }

        val g = mr.groups().iterator().next()
        offerAllGroups(g)
        chooseBestPlacement(lq)
    }

    companion object {
        private const val TOLERANCE = 0.0000001
    }
}
