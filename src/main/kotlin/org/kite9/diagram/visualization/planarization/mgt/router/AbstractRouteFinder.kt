package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.PathLocation
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization
import org.kite9.diagram.visualization.planarization.mgt.router.AbstractRouteFinder.LocatedEdgePath
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing

/**
 * This contains the EdgePath class structure, which is a way of traversing through an MGT planarization.
 *
 * It contains the logic for traversing the MGT and working out the best route to insert a given edge.
 *
 * This uses an A* algorithm (generalizing SSP) for choosing the route an edge will take through the planarization. This
 * means that potentially, the edge can flip sides from outsideEdge to below on the planarization as often as it likes to
 * achieve the shortest path.
 *
 *
 * @author robmoffat
 */
abstract class AbstractRouteFinder(
    protected val p: MGTPlanarization,
    val routeHandler: RoutableReader,
    protected val endZone: RoutingInfo,
    protected val expensive: Axis?,
    protected val bounded: Axis?,
    protected val e: PlanarizationEdge
) : AbstractSSP<LocatedEdgePath>(), Logable {

    override val prefix: String
        get() = "GTRF"
    override val isLoggingEnabled: Boolean
        get() = false

    @JvmField
	@Deprecated("")
    protected val tolerance = 1e-10
    @JvmField
	protected var log = Kite9Log(this)
    private var pathCount = 0

    enum class Going {
        FORWARDS, BACKWARDS
    }

    inner class Costing : Comparable<Costing> {
        var legalEdgeCrossCost = 0.0
        var totalEdgeCrossings = 0
        var totalPlanarizationCrossings = 0
        @JvmField
		var illegalEdgeCrossings = 0
        var minimumTotalDistance = 0.0
        @JvmField
		var minimumExpensiveAxisDistance = 0.0
        @JvmField
		var minimumBoundedAxisDistance = 0.0

        constructor() : super() {}
        constructor(c: Costing) : super() {
            legalEdgeCrossCost = c.legalEdgeCrossCost
            totalEdgeCrossings = c.totalEdgeCrossings
            totalPlanarizationCrossings = c.totalPlanarizationCrossings
            minimumTotalDistance = c.minimumTotalDistance
            minimumExpensiveAxisDistance = c.minimumExpensiveAxisDistance
            minimumBoundedAxisDistance = c.minimumBoundedAxisDistance
            illegalEdgeCrossings = c.illegalEdgeCrossings
        }

        override fun compareTo(o: Costing): Int {
            // expensive (i.e. more important than crossings)
            if (!equalWithinTolerance(minimumExpensiveAxisDistance, o.minimumExpensiveAxisDistance)) {
                return minimumExpensiveAxisDistance.compareTo(o.minimumExpensiveAxisDistance)
            }
            if (illegalEdgeCrossings != o.illegalEdgeCrossings) {
                return illegalEdgeCrossings.compareTo(o.illegalEdgeCrossings)
            }

            // route with minimum amount of crossing
            if (!equalWithinTolerance(legalEdgeCrossCost, o.legalEdgeCrossCost)) {
                return legalEdgeCrossCost.compareTo(o.legalEdgeCrossCost)
            }

            // minimum distance
            if (!equalWithinTolerance(minimumTotalDistance, o.minimumTotalDistance)) {
                return minimumTotalDistance.compareTo(o.minimumTotalDistance)
            }

            // some other things worth routing for
            if (totalEdgeCrossings != o.totalEdgeCrossings) {
                return totalEdgeCrossings.compareTo(o.totalEdgeCrossings)
            }
            return if (totalPlanarizationCrossings != o.totalPlanarizationCrossings) {
                totalPlanarizationCrossings.compareTo(o.totalPlanarizationCrossings)
            } else 0

            // adding this gives us some consistency
        }

        private fun equalWithinTolerance(a: Double, b: Double): Boolean {
            return Math.abs(a - b) < tolerance
        }

        override fun toString(): String {
            return "COST[el=" + legalEdgeCrossCost +
                    " et=" + totalEdgeCrossings +
                    " pc=" + totalPlanarizationCrossings +
                    " mtd=" + minimumTotalDistance +
                    " mbd=" + minimumBoundedAxisDistance +
                    " med=" + minimumExpensiveAxisDistance + "]"
        }
    }

    enum class PlanarizationSide {
        ENDING_ABOVE, ENDING_BELOW
    }

    fun move(
        current: LineRoutingInfo?,
        from: Int,
        to: Int,
        g: Going,
        pl: Place?,
        includeLocation: Boolean
    ): LineRoutingInfo? {
        var current = current
        var from = from
        while (Math.abs(from - to) > 1 && pl != null) {
            from += if (from < to) 1 else -1
            current = move(current, from, g, pl)
        }
        return if (includeLocation) {
            move(current, to, g, pl)
        } else {
            current
        }
    }

    private fun move(current: LineRoutingInfo?, to: Int, g: Going, pl: Place?): LineRoutingInfo? {
        val tov = p.vertexOrder[to]
        // System.out.println("-- moving to "+tov+" going "+g+" place "+pl);
        val past = tov.routingInfo
        return if (past != null) {
            val moveType = getRouting(g, pl)
            routeHandler.move(current, past, moveType)
        } else {
            current
        }
    }

    /**
     * A part of a path that does something, could be crossing a edge, the planarization, arriving somewhere.
     */
    abstract inner class EdgePath(g: Going, s: PlanarizationSide, prev: EdgePath?) {

        var pathNumber = pathCount++
        @JvmField
		var costing: Costing
        var going: Going

        /**
         * Which side of the planarization the path is travelling on.
         */
        var side: PlanarizationSide
        var pathParts: Int
        @JvmField
		var prev: EdgePath? = null
        abstract val trail: LineRoutingInfo?
        abstract val trailEndVertex: Int
        protected abstract fun initTrail(prev2: EdgePath?)
        abstract fun append(sb: StringBuilder)
        override fun toString(): String {
            val sb = StringBuilder(150)
            var c: EdgePath? = this
            while (c != null) {
                c.append(sb)
                c = c.prev
            }
            sb.append(
                "(" + pathNumber + ", $=" + costing +
                        " g=" + going +
                        " pos=" + trail
            )
            sb.append(")")
            return sb.toString()
        }

        abstract fun sameCrossing(other: EdgePath?): Boolean

        init {
            var prev = prev
            going = g
            side = s
            costing = if (prev == null) Costing() else Costing(prev.costing)
            if (prev != null) {
                while (prev is SimpleEdgePath) {
                    prev = prev.prev
                }
                this.prev = prev
            }
            pathParts = if (prev == null) 0 else this.prev!!.pathParts + 1
        }
    }

    /**
     * Located edge paths have a location set.   These are added to the queue for the dijkstra
     * routine, but aren't really of interest to the edge inserter.
     */
    abstract inner class LocatedEdgePath(l: Location, g: Going, side: PlanarizationSide, prev: EdgePath?) :
        EdgePath(g, side, prev), PathLocation<LocatedEdgePath> {

        @JvmField
		var l: Location
		override val trailEndVertex = l.trailEndVertex

        override var trail: LineRoutingInfo? = null

        override fun sameCrossing(other: EdgePath?): Boolean {
            return false
        }

        var _active = true
        override fun isActive(): Boolean {
            return _active
        }

        override fun setActive(a: Boolean) {
            _active = a
        }

        override fun getLocation(): Any {
            return l
        }

        override operator fun compareTo(o: LocatedEdgePath): Int {
            val out = costing.compareTo(o.costing)
            if (out == 0
                && pathParts != o.pathParts
            ) {
                return pathParts.compareTo(o.pathParts)
            }
            return if (out == 0 && side != o.side) {
                compareFavouredSide(side, o.side)
            } else out
        }

        /**
         * Because the distance-so-far calculation does not represent the full cost of getting to a node, we add on the
         * minimum remaining cost to get to the final node. Otherwise, SSP will not work correctly as we are not
         * considering the actual cost to the node at each step.
         *
         * By calculating cost to the final node, we are actually performing an A* calculation, and the SSP system will
         * follow the path with the lowest minimum total cost, which will be better.
         */
        protected open fun calculateRemainingCost() {
            // System.out.println("--remaining cost");
            val remaining = routeHandler.move(trail, endZone, null)
            //log.send("current pos: "+this.trail+" to: "+ remaining+ " cost: "+remaining.getRunningCost());
            calculateDistanceCosts(remaining)
        }

        protected fun calculateDistanceCosts(remaining: LineRoutingInfo) {
            costing.minimumTotalDistance = remaining.getRunningCost()
            if (expensive == Axis.VERTICAL) {
                costing.minimumExpensiveAxisDistance = remaining.getVerticalRunningCost()
            } else if (expensive == Axis.HORIZONTAL) {
                costing.minimumExpensiveAxisDistance = remaining.getHorizontalRunningCost()
            }
            if (bounded == Axis.VERTICAL) {
                costing.minimumBoundedAxisDistance = remaining.getVerticalRunningCost()
            } else if (bounded == Axis.HORIZONTAL) {
                costing.minimumBoundedAxisDistance = remaining.getHorizontalRunningCost()
            }
        }

        init {
            going = g
            this.l = l
            initTrail(prev)
            calculateRemainingCost()
        }
    }

    abstract inner class EdgeCrossPath(public var crossing: PlanarizationEdge, prev: EdgePath?, g: Going) :
        EdgePath(g, prev!!.side, prev) {

        override var trailEndVertex = 0
        override var trail: LineRoutingInfo? = null

        override fun initTrail(prev2: EdgePath?) {
            trailEndVertex = prev2!!.trailEndVertex
            trail = prev2.trail
        }

        override fun sameCrossing(other: EdgePath?): Boolean {
            return if (other is EdgeCrossPath) {
                crossing === other.crossing
            } else {
                false
            }
        }

        init {
            costing.legalEdgeCrossCost += crossing.getCrossCost().toDouble()
            costing.totalEdgeCrossings++
            if (illegalEdgeCross == Axis.HORIZONTAL) {
                if (crossing.getDrawDirection() === Direction.LEFT || crossing.getDrawDirection() === Direction.RIGHT) {
                    costing.illegalEdgeCrossings++
                }
            } else if (illegalEdgeCross == Axis.VERTICAL) {
                if (crossing.getDrawDirection() === Direction.UP || crossing.getDrawDirection() === Direction.DOWN) {
                    costing.illegalEdgeCrossings++
                }
            }
            initTrail(prev)
        }
    }

    inner class EndCrossEdgePath(e: PlanarizationEdge, prev: EdgePath, g: Going) : EdgeCrossPath(e, prev, g) {
        override fun append(sb: StringBuilder) {
            sb.append("-")
            sb.append("ece(")
            sb.append(crossing)
            sb.append(")")
        }
    }

    inner class FinishPath(vertex: Int, v: Vertex, prev: EdgePath?, g: Going, outsideEdge: PlanarizationEdge?) :
        TerminalPath(
            Location(null, vertex, v), g, prev!!.side, prev, outsideEdge
        ) {
        override fun append(sb: StringBuilder) {
            sb.append("finish(")
            sb.append(l)
            sb.append(",")
            sb.append(side)
            sb.append(")")
        }

        /**
         * Instead of calculating the rest of the distance to the endZone, we now need to
         * calculate the actual distance to the vertex of choice that we are attaching to.
         */
        override fun calculateRemainingCost() {
            calculateDistanceCosts(trail!!)
        }

        public override fun initTrail(prev: EdgePath?) {
            trail = move(prev!!.trail, prev.trailEndVertex, l.trailEndVertex, going, null, true)
        }
    }

    inner class PlanarizationCrossPath(after: Int, prev: EdgePath, g: Going, switchback: Boolean) :
        EdgePath(
            g,
            if (prev.side == PlanarizationSide.ENDING_ABOVE) PlanarizationSide.ENDING_BELOW else PlanarizationSide.ENDING_ABOVE,
            prev
        ) {
        var after: Int
        var crossingPoint: Vertex? = null
        override var trail: LineRoutingInfo? = null
        var switchback: Boolean
        override fun sameCrossing(other: EdgePath?): Boolean {
            return if (other is PlanarizationCrossPath) {
                after == other.after
            } else {
                false
            }
        }

        public override fun initTrail(prev: EdgePath?) {
            trail = move(
                prev!!.trail,
                prev.trailEndVertex,
                trailEndVertex,
                going,
                if (side == PlanarizationSide.ENDING_ABOVE) Place.BELOW else Place.ABOVE,
                true
            )
        }

        var afterV: Vertex
        var beforeV: Vertex
        override fun append(sb: StringBuilder) {
            sb.append("-")
            sb.append("cross(")
            if (crossingPoint == null) {
                sb.append(after.toString() + "," + (after + 1))
            } else {
                sb.append(crossingPoint)
            }
            if (switchback) {
                sb.append(",SB")
            }
            sb.append(")")
        }

        override val trailEndVertex: Int
            get() = if (going == Going.FORWARDS) {
                after
            } else {
                after + 1
            }

        init {
            beforeV = p.vertexOrder[after]
            afterV = p.vertexOrder[after + 1]
            costing.totalPlanarizationCrossings++
            this.after = after
            initTrail(prev)
            this.switchback = switchback
        }
    }

    inner class SimpleEdgePath(newVertex: Int, above: Boolean, vertex: Vertex, prev: EdgePath, g: Going) :
        LocatedEdgePath(
            Location(if (above) Place.ABOVE else Place.BELOW, newVertex, vertex),
            g,
            prev.side,
            prev
        ) {
        override fun append(sb: StringBuilder) {
            sb.append("-sep(")
            sb.append(l)
            sb.append(",")
            sb.append(side)
            sb.append(")")
        }

        public override fun initTrail(prev: EdgePath?) {
            trail = move(prev!!.trail, prev.trailEndVertex, l.trailEndVertex, going, l.p, true)
        }
    }

    inner class StartCrossEdgePath(e: PlanarizationEdge, prev: EdgePath, g: Going) : EdgeCrossPath(e, prev, g) {
        override fun append(sb: StringBuilder) {
            sb.append("-")
            sb.append("sce(")
            sb.append(crossing)
            sb.append(")")
        }
    }

    abstract inner class TerminalPath(
        l: Location, g: Going, side: PlanarizationSide, prev: EdgePath?,
        /**
         * Edge that this path terminates outside of
         */
        var outsideEdge: PlanarizationEdge?

    ) : LocatedEdgePath(l, g, side, prev)

    inner class StartPath(
        vertex: Int,
        v: Vertex,
        p: Place?,
        side: PlanarizationSide,
        position: RoutingInfo?,
        going: Going,
        outsideEdge: PlanarizationEdge?
    ) : TerminalPath(
        Location(p, vertex, v), going, side, null, outsideEdge
    ) {
        override fun append(sb: StringBuilder) {
            sb.append("-start(")
            sb.append(l)
            sb.append(",")
            sb.append(side)
            sb.append(")")
        }

        public override fun initTrail(unused: EdgePath?) {
            trail = routeHandler.move(null, l.vertex.routingInfo!!, null)
        }
    }

    enum class Place {
        ABOVE, BELOW
    }

    /**
     * This class holds the location of the ssp node, which can be either outsideEdge or below or arriving at any given vertex.
     */
    class Location(var p: Place?, val trailEndVertex: Int, val vertex: Vertex) {
        override fun toString(): String {
            return "LOC[p=" + p + ", vertex=" + trailEndVertex + ",v=" + trailEndVertex + "]"
        }

        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + if (p == null) 0 else p.hashCode()
            result = prime * result + trailEndVertex
            return result
        }

        override fun equals(obj: Any?): Boolean {
            if (this === obj) return true
            if (obj == null) return false
            if (javaClass != obj.javaClass) return false
            val other = obj as Location
            if (p != other.p) return false
            return if (trailEndVertex != other.trailEndVertex) false else true
        }
    }

    enum class Axis {
        HORIZONTAL, VERTICAL
    }

    @JvmField
	protected var illegalEdgeCross: Axis? = null
    @JvmField
	protected var entryDirection: Direction? = null
    @JvmField
	protected var exitDirection: Direction? = null
    @JvmField
	protected var pathDirection: Direction? = null
    fun addToQueue(pq: State<LocatedEdgePath>, ep: LocatedEdgePath?) {
        if (ep != null) {
            if (canAddToQueue(ep) && pq.add(ep)) {
                log.send(if (log.go()) null else "Adding: $ep")
            } else {
                log.send(if (log.go()) null else "Not Adding: $ep")
            }
        }
    }

    /**
     * Allows us to exclude a path based on some criteria after it has been generated.
     */
    protected abstract fun canAddToQueue(ep: LocatedEdgePath?): Boolean

    /**
     * Takes an EdgePath, and crosses some edges to move it either outsideEdge or below a the pathVertex, which turns it into a proper LocatedEdgePath.
     */
    private fun escape(
        outsideOf: PlanarizationEdge?,
        forwardIn: EdgePath?,
        inside: List<PlanarizationEdge?>,
        outside: List<PlanarizationEdge?>,
        pathAbove: Boolean,
        pathVertex: Int,
        startingDirection: Going,
        endingDirection: Going
    ): LocatedEdgePath? {
        var outsideOf = outsideOf
        if (forwardIn == null) {
            return null
        }
        if (!canTravel(pathVertex, endingDirection, forwardIn.side == PlanarizationSide.ENDING_ABOVE)) {
            return null
        }
        var forward: EdgePath = forwardIn

        // have to move to the bottom of the inside group
        var currentlyOutside = inside.indexOf(outsideOf)
        if (currentlyOutside > -1) {
            for (i in currentlyOutside downTo 0) {
                val edge = inside[i]
                val toi = meetsDestination(edge, startingDirection)
                if (toi != null) {
                    val toV = p.vertexOrder[toi]
                    if (canRouteToVertex(
                            toV,
                            edge,
                            forward.side == PlanarizationSide.ENDING_ABOVE,
                            startingDirection,
                            true
                        )
                    ) {
                        return FinishPath(toi, toV, forward, startingDirection, outsideOf)
                    }
                }
                if (!canCross(edge, forward, !pathAbove)) {
                    return null
                }
                forward = EndCrossEdgePath(edge!!, forward, startingDirection)
                outsideOf = edge
            }
        }

        // cross the planarization
        if (pathAbove && forward.side == PlanarizationSide.ENDING_BELOW ||
            !pathAbove && forward.side == PlanarizationSide.ENDING_ABOVE
        ) {
            val after = if (endingDirection == Going.FORWARDS) pathVertex - 1 else pathVertex
            forward = if (canSwitchSides(after)) {
                PlanarizationCrossPath(after, forward, startingDirection, startingDirection != endingDirection)
            } else {
                return null
            }
        }

        // now have to work outwards on the outside group
        currentlyOutside = if (outsideOf == null) -1 else outside.indexOf(outsideOf)
        for (i in currentlyOutside + 1 until outside.size) {
            val edge = outside[i]
            if (!canCross(edge, forward, !pathAbove)) {
                return null
            }
            forward = EndCrossEdgePath(edge!!, forward, endingDirection)
            val finish = meetsDestination(edge, endingDirection)
            if (finish != null) {
                val toV = p.vertexOrder[finish]
                if (canRouteToVertex(
                        toV,
                        edge,
                        forward.side == PlanarizationSide.ENDING_ABOVE,
                        endingDirection,
                        true
                    )
                ) {
                    return FinishPath(finish, toV, forward, endingDirection, edge)
                }
            }
        }
        return SimpleEdgePath(pathVertex, pathAbove, p.vertexOrder[pathVertex], forward, endingDirection)
    }

    /**
     * Prevents the path wending through vertices that are on top of each other
     */
    protected fun canSwitchSides(after: Int): Boolean {
        if (after + 1 == p.vertexOrder.size) {
            return false
        }
        val beforeV = p.vertexOrder[after]
        val afterV = p.vertexOrder[after + 1]
        val bri = beforeV.routingInfo
        val ari = afterV.routingInfo
        return if (bri != null && ari != null && routeHandler.overlaps(bri, ari)) {
            false
        } else {
            true
        }
    }

    /**
     * Determines whether the route can cross a given edge.
     */
    protected abstract fun canCross(edge: Edge?, forward: EdgePath?, above: Boolean): Boolean

    /**
     * Determines whether the route can continue above/below an existing vertex.
     */
    protected abstract fun canTravel(pathVertex: Int, endingDirection: Going?, b: Boolean): Boolean
    protected fun getPosition(v: Vertex): RoutingInfo? {
        //		if (out == null) {
//			DiagramElement und = v.getOriginalUnderlying();
//			out = rh.getPlacedPosition(und);
//		}
        return v.routingInfo
    }

    private fun meetsDestination(edge: Edge?, g: Going): Int? {
        val to = edge!!.getTo()
        var toi = p.getVertexIndex(to)
        val from = edge.getFrom()
        var fromi = p.getVertexIndex(from)
        if (fromi > toi) {
            val temp = fromi
            fromi = toi
            toi = temp
        }
        if (g == Going.FORWARDS) {
            if (isTerminationVertex(toi)) {
                return toi
            }
        } else if (g == Going.BACKWARDS) {
            if (isTerminationVertex(fromi)) {
                return fromi
            }
        }
        return null
    }

    protected override fun generateSuccessivePaths(r: LocatedEdgePath, pq: State<LocatedEdgePath>) {
        log.send(if (log.go()) null else "Extending path: $r")
        val from = p.vertexOrder[r.l.trailEndVertex]
        generatePaths(r, getLinkSet(r.going, from, r.side), pq, from, r.going, r.side)
        val backOk = r.l.trailEndVertex > 0 && r.l.trailEndVertex < p.vertexOrder.size - 1 && pathDirection == null
        if (backOk) {
            generateSwitchbackPaths(pq, r, from, r.going)
        }
    }

    private fun getLinkSet(g: Going, from: Vertex, s: PlanarizationSide): List<PlanarizationEdge?> {
        return if (s == PlanarizationSide.ENDING_ABOVE) {
            if (g == Going.FORWARDS) p.getAboveForwardLinks(from) else p.getAboveBackwardLinks(from)
        } else if (s == PlanarizationSide.ENDING_BELOW) {
            if (g == Going.FORWARDS) p.getBelowForwardLinks(from) else p.getBelowBackwardLinks(from)
        } else {
            throw LogicException("Was expecting a side$s")
        }
    }

    /**
     * This does a handbrake turn around a given vertex, leaving you facing back in the other direction, on the other side of the planarization line
     * Obviously this doesn't get used for directed edges, as they can't turn.
     */
    private fun generateSwitchbackPaths(
        pq: State<LocatedEdgePath>,
        r: LocatedEdgePath,
        from: Vertex,
        toStartWith: Going
    ) {
        val insideLinks: List<PlanarizationEdge?>
        val outsideLinks: List<PlanarizationEdge?>
        var first: PlanarizationEdge? = null
        val endingUp = if (toStartWith == Going.FORWARDS) Going.BACKWARDS else Going.FORWARDS
        val endingAbove = r.l.p == Place.ABOVE
        if (endingAbove) {
            insideLinks = getLinkSet(toStartWith, from, PlanarizationSide.ENDING_ABOVE)
            outsideLinks = getLinkSet(toStartWith, from, PlanarizationSide.ENDING_BELOW)
        } else {
            insideLinks = getLinkSet(toStartWith, from, PlanarizationSide.ENDING_BELOW)
            outsideLinks = getLinkSet(toStartWith, from, PlanarizationSide.ENDING_ABOVE)
        }
        if (insideLinks.size > 0) {
            first = insideLinks[insideLinks.size - 1]
        }

        //if (insideLinks.size() + outsideLinks.size()==0) {
        //if (getPosition(from)!=null) {
        val path = escape(first, r, insideLinks, outsideLinks, !endingAbove, r.l.trailEndVertex, toStartWith, endingUp)
        if (path != null) {
            log.send("Switchback:")
            addToQueue(pq, path)
        }
        //		} else {
//			return;
//		}
    }

    protected override fun pathComplete(r: LocatedEdgePath): Boolean {
        return r is FinishPath
    }

    private fun createStartPath(
        path: EdgePath?,
        from: Vertex,
        fromi: Int,
        g: Going,
        side: PlanarizationSide,
        outsideOf: PlanarizationEdge?
    ): EdgePath? {
        if (path != null) {
            return path
        }
        return if (canRouteToVertex(from, outsideOf, side == PlanarizationSide.ENDING_ABOVE, g, false)) {
            StartPath(fromi, from, null, side, from.routingInfo, g, outsideOf)
        } else {
            null
        }
    }

    protected abstract fun canRouteToVertex(
        from: Vertex?,
        outsideOf: PlanarizationEdge?,
        above: Boolean,
        g: Going?,
        arriving: Boolean
    ): Boolean

    protected fun generatePaths(
        r: LocatedEdgePath?,
        list: List<PlanarizationEdge?>,
        pq: State<LocatedEdgePath>,
        from: Vertex,
        g: Going,
        side: PlanarizationSide
    ) {
        var current: EdgePath? = r
        val s = p.getVertexIndex(from)
        for (i in list.indices.reversed()) {
            val edge = list[i]
            val to = edge!!.otherEnd(from)
            val e = p.getVertexIndex(to)
            if (isTerminationVertex(e)) {
                if (canRouteToVertex(to, edge, side == PlanarizationSide.ENDING_ABOVE, g, true) && canTravel(
                        e,
                        g,
                        side == PlanarizationSide.ENDING_ABOVE
                    )
                ) {
                    addToQueue(pq, FinishPath(e, to, createStartPath(current, from, s, g, side, edge), g, edge))
                }
            }
            addToQueue(
                pq,
                escape(
                    edge, createStartPath(current, from, s, g, side, edge), getCorrectEdgeSet(s, e, false, to),
                    getCorrectEdgeSet(s, e, true, to), true, e, g, g
                )
            )
            addToQueue(
                pq,
                escape(
                    edge, createStartPath(current, from, s, g, side, edge), getCorrectEdgeSet(s, e, true, to),
                    getCorrectEdgeSet(s, e, false, to), false, e, g, g
                )
            )
            if (r != null) {
                current = if (!canCross(edge, current, r.l.p == Place.ABOVE)) {
                    return
                } else {
                    StartCrossEdgePath(edge, current!!, g)
                }
            }
        }
        val nextItem = s + if (g == Going.FORWARDS) 1 else -1
        if (nextItem < p.vertexOrder.size && nextItem >= 0) {
            // generate the inside route
            val vnext = p.vertexOrder[nextItem]
            current = createStartPath(current, from, s, g, side, null)
            if (current == null) {
                return
            }

            // above paths
            var using = current
            if (side == PlanarizationSide.ENDING_BELOW) {
                using = PlanarizationCrossPath(nextItem + if (g == Going.FORWARDS) -1 else 0, current, g, false)
            }
            addToQueue(
                pq,
                escape(
                    null, using, getCorrectEdgeSet(s, nextItem, false, vnext),
                    getCorrectEdgeSet(s, nextItem, true, vnext), true, nextItem, g, g
                )
            )


            // below paths
            using = current
            if (side == PlanarizationSide.ENDING_ABOVE) {
                using = PlanarizationCrossPath(nextItem + if (g == Going.FORWARDS) -1 else 0, current, g, false)
            }
            addToQueue(
                pq,
                escape(
                    null, using, getCorrectEdgeSet(s, nextItem, true, vnext),
                    getCorrectEdgeSet(s, nextItem, false, vnext), false, nextItem,
                    g, g
                )
            )
            if (isTerminationVertex(nextItem)) {
                // generate some finish paths if we are in the right area
                if (canRouteToVertex(vnext, null, true, g, true)) {
                    addToQueue(pq, FinishPath(nextItem, p.vertexOrder[nextItem], current, g, null))
                }
            }
        }
    }

    /**
     * Returns true if the vertex is somewhere where the route can end.
     */
    protected abstract fun isTerminationVertex(v: Int): Boolean
    
    private fun getCorrectEdgeSet(start_pos: Int, ev_pos: Int, above: Boolean, ev: Vertex): List<PlanarizationEdge> {
        return getCorrectEdgeSet(start_pos, ev_pos, above, ev, p)
    }

    protected open fun compareFavouredSide(side: PlanarizationSide?, side2: PlanarizationSide?): Int {
        return 0
    }

    companion object {
        private fun getRouting(going: Going, p: Place?): Routing? {
            return if (p == null) {
                // start or end routing.
                null
            } else when (going) {
                Going.FORWARDS -> {
                    return when (p) {
                        Place.ABOVE -> Routing.OVER_FORWARDS
                        Place.BELOW -> Routing.UNDER_FORWARDS
                    }
                    when (p) {
                        Place.ABOVE -> Routing.OVER_BACKWARDS
                        Place.BELOW -> Routing.UNDER_BACKWARDS
                    }
                }
                Going.BACKWARDS -> when (p) {
                    Place.ABOVE -> Routing.OVER_BACKWARDS
                    Place.BELOW -> Routing.UNDER_BACKWARDS
                }
            }
            throw LogicException("Don't have a routing")
        }

        fun getCorrectEdgeSet(
            start_pos: Int,
            ev_pos: Int,
            above: Boolean,
            ev: Vertex,
            p: MGTPlanarization
        ): List<PlanarizationEdge> {
            return getCorrectEdgeSet(if (start_pos < ev_pos) Going.FORWARDS else Going.BACKWARDS, above, ev, p)
        }

        fun getCorrectEdgeSet(g: Going, above: Boolean, ev: Vertex, p: MGTPlanarization): List<PlanarizationEdge> {
            return if (g == Going.FORWARDS) {
                if (above) {
                    p.getAboveBackwardLinks(ev)
                } else {
                    p.getBelowBackwardLinks(ev)
                }
            } else {
                if (above) {
                    p.getAboveForwardLinks(ev)
                } else {
                    p.getBelowForwardLinks(ev)
                }
            }
        }
    }
}