package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.Dimension.H
import org.kite9.diagram.common.elements.Dimension.V
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.anchors.AnchorType
import org.kite9.diagram.visualization.compaction2.anchors.ConnAnchor
import org.kite9.diagram.visualization.compaction2.anchors.FanAnchor
import org.kite9.diagram.visualization.compaction2.anchors.RectAnchor
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2ConnectionFanningCompactionStep(cd: CompleteDisplayer, gp: GridPositioner) :
    AbstractC2CompactionStep(cd) {

    data class ConnectionSection(val from: ConnAnchor, val to: ConnAnchor) {


    }



    enum class Placement {
        OUTSIDE, INSIDE, ON
    }

    override fun compact(c: C2Compaction, g: Group) {
        val soV = c.getSlackOptimisation(V)
        val soH = c.getSlackOptimisation(H)

        val allV = soV.getAllSlideables().toList()
        val allH = soH.getAllSlideables().toList()
        val hMap1 = prepareMap(soH)
        val vMap1 = prepareMap(soV)

        // keep track of splitters we create - we don't lane these
        val splitterSlideables = mutableSetOf<C2Slideable>()
        val branchInfo = mutableMapOf<C2Slideable, C2Slideable>()

        allV.forEach { handleFanning(it, soV, V, soH, hMap1, splitterSlideables, branchInfo) }
        allH.forEach { handleFanning(it, soH, H, soV, vMap1, splitterSlideables, branchInfo) }

        soV.updateTDMatrix()
        soH.updateTDMatrix()

        val hMap2 = prepareMap(soH)
        val vMap2 = prepareMap(soV)

        soV.getAllSlideables().toList().forEach { handleLaning(it, soV, V, soH, hMap2, create1DRouteMap(soV), splitterSlideables, branchInfo) }
        soH.getAllSlideables().toList().forEach { handleLaning(it, soH, H,  soV, vMap2, create1DRouteMap(soH), splitterSlideables, branchInfo) }
    }
    private fun prepareMap(so1: C2SlackOptimisation): Map<ConnAnchor, C2Slideable> {
        return so1.getAllSlideables()
            .flatMap { s -> s.getConnAnchors().map { it to s } }
            .groupBy { it.first }
            .mapValues { it.value.map { it -> it.second }.first() }
    }

    private fun create1DRouteMap(cso: C2SlackOptimisation) : Map<Connection,List<Float>> {
        cso.checkConsistency()
        val out = mutableMapOf<Connection, MutableSet<Float>>()

        cso.getAllSlideables()
            .forEach { rs -> rs.getConnAnchors()
                .filter { it.e.getRenderingInformation().rendered }
                .forEach { a ->
                    val conn = a.e
                    val idx = a.s
                    val list = out.getOrPut(conn) { mutableSetOf() }
                    list.add(idx)
                }
            }

        return out.map { (k, v) -> k to v.toList().sorted() }.toMap()
    }

    private fun handleLaning(s: C2Slideable, so: C2SlackOptimisation, d: Dimension, soPerp: C2SlackOptimisation, perpConnAnchorMap: Map<ConnAnchor, C2Slideable>, routeMap: Map<Connection,List<Float>>, splitters: Set<C2Slideable>, branches: Map<C2Slideable, C2Slideable>) {

        fun isFanning (a: ConnAnchor) : Boolean {
            return when (a.type) {
                AnchorType.PRE_FAN -> true
                AnchorType.TERMINAL -> false
                AnchorType.AFTER_FAN -> true
                AnchorType.REGULAR -> false
            }
        }

        fun isFanning (a: ConnectionSection) : Boolean {
            return isFanning(a.to) && isFanning(a.from)
        }

        fun okToLane(a: ConnAnchor) : Boolean {
            return when (a.type) {
                AnchorType.PRE_FAN -> false
                AnchorType.TERMINAL -> false
                AnchorType.AFTER_FAN -> true
                AnchorType.REGULAR -> true
            }
        }

        if (s.getRectangulars().isNotEmpty()) {
            // this is the edge of a glyph - don't process
            return
        }

        if (splitters.contains(s)) {
            // splitter contents overlap
            return
        }

        val connections = s.getConnAnchors().map { it.e }.toSet()
        val sections = mutableListOf<ConnectionSection>()
        val transitiveDistances = soPerp.getTransitiveDistanceMatrix()
        val fanningAnchorPairs = mutableListOf<Pair<ConnAnchor, ConnAnchor>>()

        connections.forEach { c ->
            val routeAnchors = routeMap[c]!!
            val anchorsForConnection = s.getConnAnchors().filter { it.e == c }.sortedBy { it.s }
            val indexes = anchorsForConnection.map { it.s }
            indexes.forEach { i ->
                var prevI = previousAnchor(routeAnchors, i)
                if (indexes.contains(prevI)) {
                    // we have a section
                    val from = anchorsForConnection[indexes.indexOf(prevI)]
                    val to = anchorsForConnection[indexes.indexOf(i)]
                    sections.add(ConnectionSection(from, to))
                    if (isFanning(from) && isFanning(to)) {
                        fanningAnchorPairs.add(from to to)
                    }
                }
            }
        }



        // remove sections that won't get laned
        val lanableSections = sections.filter { okToLane(it.from) && okToLane(it.to) }
        val nonLanableSections = sections - lanableSections
        val keptConnAnchorsOnS = mutableSetOf<ConnAnchor>()

        fun overlaps(toAdd: ConnectionSection, overlapGroup: Set<ConnectionSection>): Boolean {

            fun dist(p1: C2Slideable, p2: C2Slideable): Constraint? {
                return transitiveDistances[p1]?.get(p2)
            }

            fun dist(c1: ConnAnchor, c2: ConnAnchor): Int? {
                val s1 = perpConnAnchorMap[c1]
                val s2 = perpConnAnchorMap[c2]


                if (s1==s2) {
                    return 0
                }
                val c = dist(s1!!, s2!!)
                if (c == null) {
                    return null
                } else if (c.forward) {
                    return c.dist
                } else {
                    return -c.dist
                }
            }

            fun between(mid: ConnAnchor, a: ConnAnchor, b: ConnAnchor): Placement {
                val midToA = dist(mid, a)
                val midToB = dist(mid, b)

                if ((midToA == null) || (midToB == null)) {
                    return Placement.OUTSIDE
                }

                if ((midToA == 0) || (midToB == 0)) {
                    return Placement.ON
                }

                val midToAForward = midToA >0
                val midToBForward = midToB >0

                val midToABackward = midToB <0
                val midToBBackward = midToB <0

                if (midToAForward && midToBBackward) {
                    return Placement.INSIDE
                } else if (midToABackward && midToBForward) {
                    return Placement.INSIDE
                } else {
                    return Placement.OUTSIDE
                }
            }

            fun inside(inner: ConnectionSection, outer: ConnectionSection) : Boolean {
                val first = between(inner.from, outer.from, outer.to)
                val second = between(inner.to, outer.from, outer.to)
                return if ((first == Placement.INSIDE) || (second == Placement.INSIDE)) {
                    true
                } else (first == Placement.ON) && (second == Placement.ON)
            }

            val overlaps =
                overlapGroup.filter {
                    return inside(it, toAdd) || inside(toAdd, it)

                }
            return overlaps.isNotEmpty()
        }

        if ((lanableSections.size > 1) && (connections.size > 1)) {
            // ok, we have overlaps on this section.
            val overlapGroups = mutableListOf<MutableSet<ConnectionSection>>()

            lanableSections.forEach { si ->
                var overlapping = overlapGroups.filter { overlaps(si, it) }
                if (overlapping.isEmpty()) {
                    overlapGroups.add(mutableSetOf(si))
                } else if (overlapGroups.size == 1) {
                    overlapGroups.first().add(si)
                } else {
                    // merge overlap groups
                    overlapGroups.removeAll(overlapping)
                    val newOG = overlapping.flatMap { it }.toMutableSet()
                    newOG.add(si)
                    overlapGroups.add(newOG)
                }
            }

            overlapGroups.forEach { og ->
                val lanes = og.toList() // naive ordering

                // now map lanes to new slideables
                val oddLaneCount = lanes.size % 2 == 1
                val middleLane = if (oddLaneCount) {
                    (lanes.size / 2) - 1
                } else {
                    -1
                }

                val slideables = lanes.mapIndexed { i, l ->
                    val connAnchors = setOf(l.to, l.from)
                    if (i == middleLane) {
                        keptConnAnchorsOnS.addAll(connAnchors)
                        s.addAnchor(FanAnchor(l.to.e.getFrom(), 0))
                        s.addAnchor(FanAnchor(l.to.e.getTo(), 0))
                        s
                    } else {
                        val o = C2Slideable(so, d, connAnchors)
                        so.addSlideable(o)
                        o.addAnchor(FanAnchor(l.to.e.getFrom(), i - middleLane))
                        o.addAnchor(FanAnchor(l.to.e.getTo(), i - middleLane))
                        o
                    }
                }

                // ensure distance between each one
                slideables.forEachIndexed { i, s2 ->
                    if (i > 0) {
                        val s1 = slideables[i - 1]
                        so.ensureMinimumDistance(s1, s2, 4)
                    }
                }

                // make branches even each side of the original slideable
                if (slideables.size % 2 == 1) {
                    // odd number of branches, do nothing
                } else {
                    val originalSlideable = branches[s]
                    if (originalSlideable != null) {
                        val middleLaneBefore = slideables[(slideables.size / 2) - 1]
                        val middleLaneAfter = slideables[(slideables.size / 2)]
                        so.ensureMinimumDistance(middleLaneBefore, originalSlideable, 2)
                        so.ensureMinimumDistance(originalSlideable, middleLaneAfter, 2)
                    }
                }

            }


            // replace non-lanable sections
            val keptNonLanables = nonLanableSections.filter { !isFanning(it) }. flatMap { listOf(it.from, it.to) }.toSet()
            val remainingConnAnchors = keptConnAnchorsOnS + keptNonLanables
            s.replaceConnAnchors(remainingConnAnchors)
        }
    }

    private fun previousAnchor(routeAnchors: List<Float>, f: Float): Float {
        val idx = routeAnchors.indexOf(f)
        if (idx == 0) {
            return -1f;
        } else {
            return routeAnchors[idx-1]
        }
    }

    private fun getRectAnchor(perpConnAnchorMap: Map<ConnAnchor, C2Slideable>, ca: ConnAnchor, e: Positioned) : RectAnchor {
        val perpSlideable = perpConnAnchorMap.get(ca)!!
        val rectAnchor = perpSlideable.getRectangulars().find { it.e == e }
        return rectAnchor!!
    }

    /**
     * For a given slideable, sIn, observes if multiple ConnAnchors arrive at the same destination.
     * If they do, we move some of those onto separate slideables.
     *
     * sIn keeps as the original intersection slideable.  splitter is a perpendicular slideable
     * which accommodates the "doglegs" and branch is a new branch containing all the ConnAnchors
     * that will be laned later.
     */
    private fun handleFanning(sIn: C2Slideable, so: C2SlackOptimisation, d: Dimension, soPerp: C2SlackOptimisation, perpConnAnchorMap: Map<ConnAnchor, C2Slideable>, splitters: MutableSet<C2Slideable>, branches: MutableMap<C2Slideable, C2Slideable>): C2Slideable {
        if (sIn.getIntersectionAnchors().isEmpty()) {
            // fanning is only applied at intersections
            return sIn
        }

        // maps which terminal ConnAnchors are on the slideable, per connection
        val terminalConnAnchors = sIn.getConnAnchors()
            .filter { it.type == AnchorType.TERMINAL }
            .map { if (it.s.compareTo(0.0) == 0 ) { getRectAnchor(perpConnAnchorMap, it, it.e.getTo()) } else { getRectAnchor(perpConnAnchorMap, it, it.e.getFrom()) } to it }
            .groupBy { ks -> ks.first }
            .mapValues { it.value.map { e -> e.second } }

        val fanningConnections = terminalConnAnchors
            .filter { (ra, g) -> g.size > 1 }
            .flatMap { (ra, g) -> g }
            .map {  a -> a.e}

        // keep track of the ConnAnchors that remain on sIn
        val sInAnchors = sIn.getConnAnchors()
            .filter { fanningConnections.contains(it.e) && it.type == AnchorType.TERMINAL  }
            .toMutableSet()

        val branchAnchors = (sIn.getConnAnchors() - sInAnchors).toMutableSet()

        terminalConnAnchors.forEach { (ra, g) ->
            // keep track of which anchors go where here
            val splitterAnchors = mutableSetOf<ConnAnchor>()

            g.forEach {
                val newIndex1 = if (it.s.compareTo(0.0) == 0 )  0.2f else it.s - 0.2f
                val newIndex2 = if (it.s.compareTo(0.0) == 0 )  0.4f else it.s - 0.4f

                val ca1 = ConnAnchor(it.e, newIndex1, AnchorType.PRE_FAN)
                val ca2 = ConnAnchor(it.e, newIndex2, AnchorType.AFTER_FAN)

                sInAnchors.add(ca1)

                splitterAnchors.add(ca1)
                splitterAnchors.add(ca2)

                branchAnchors.add(ca2)
            }

            // create new slideables for the forking part.
            val splitter = C2Slideable(soPerp, d.other(), splitterAnchors)
            soPerp.addSlideable(splitter)
            splitters.add(splitter)

            // ensure that the new slideable is in the right position
            placeSplitter(splitter, soPerp, ra, g.toSet())
        }

        if (branchAnchors.isNotEmpty()) {
            // remove the old anchors from the original slideable
            sIn.replaceConnAnchors(sInAnchors + branchAnchors)
        }

        return sIn
    }

    private fun placeSplitter(
        splitter: C2Slideable,
        so: C2SlackOptimisation,
        ra: RectAnchor,
        toSet: Set<ConnAnchor>
    ) {

        val rectSS = so.getSlideablesFor(ra.e)!!
        var orbitSS = so.getContainer(rectSS)

        if (ra.s == Side.START) {
            // left side
            so.ensureMinimumDistance(orbitSS.bl!!, splitter, 10)
            so.ensureMinimumDistance(splitter, rectSS.l, 10)
        } else if (ra.s == Side.END) {
            // right side
            so.ensureMinimumDistance(rectSS.r, splitter, 10)
            so.ensureMinimumDistance(splitter, orbitSS.br!!, 10)
        } else {
            throw LogicException("Should only be one rectangular container at this point?! ")
        }

    }


    private fun orderSlideables(connections: Set<Connection>, so: C2SlackOptimisation, c: C2Compaction): List<Connection> {
        return connections.toList()
    }

    override val prefix = "C2CF"
    override val isLoggingEnabled = true


}