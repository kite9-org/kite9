package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.algorithms.fg.LinearArc
import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.logging.Table
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer.Companion.removeArcs
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph
import org.kite9.diagram.visualization.planarization.Face

/**
 * Contains the basic code for testing nudges out, splitting the flow graph into halves, logging the results of a nudge and working out
 * how many corners are needed for a particular nudge.
 * @author robmoffat
 */
abstract class AbstractConstraintNudger(var facePortionMap: Map<Face, List<PortionNode>>) : Logable, ConstraintNudger {


	protected var log = Kite9Log.instance(this)

    protected fun getTableLogNodes(fg: MappedFlowGraph): Collection<Node> {
        val out: MutableList<Node> = ArrayList()
        for (n in fg.allNodes) {
            if (n is PortionNode) {
                out.add(n)
            }
        }
        return out
    }

    protected fun createRouteList(constraintGroup: ConstraintGroup, fg: MappedFlowGraph): MutableCollection<NudgeItem> {
        val out: MutableCollection<NudgeItem> = sortedSetOf(Comparator { o1, o2 ->
            if (o1.type !== o2.type) {
                o1.type.compareTo(o2.type)
            } else {
                o1.id.compareTo(o2.id)
            }
        })
        //		Collection<NudgeItem> out = new LinkedList<NudgeItem>();
        var id = 0
        for (r in constraintGroup.getRequiredRoutes()) {
            try {
                val pc = getPortionsForConstraint(fg, r)
                val ni = NudgeItem(id++, r, r.size(), pc, getOppositePortions(fg, pc, r))
                out.add(ni)
            } catch (e: Exception) {
                throw LogicException("Could not convert route to nudge item: $r", e)
            }
        }
        return out
    }

    protected fun getNextNudgeItem(routes: MutableCollection<NudgeItem>): NudgeItem? {
        val i = routes.iterator()
        val out = i.next()
        i.remove()
        return out
    }

    /**
     * Where a subdivision node meets a portion in clockwise and anti-clockwise,
     * split the subdivision node so that this is no longer the case
     */
    protected fun subdivideNodes(
        subdivisions: MutableCollection<SubdivisionNode>,
        portionsClockwise: List<PortionNode>,
        portionsAntiClockwise: List<PortionNode>,
        splits: MutableList<Pair<SubdivisionNode>>,
        constraintNo: Int,
        fg: MappedFlowGraph
    ) {
        val `in`: Collection<SubdivisionNode> = ArrayList(subdivisions)
        for (sn in `in`) {
            val meetsClock = sn.meets(portionsClockwise)
            val meetsAnti = sn.meets(portionsAntiClockwise)
            val subdivision = sn.subdivision
            if (meetsClock && meetsAnti) {
                val bPart = sn.split(portionsClockwise, portionsAntiClockwise, constraintNo)
                val newPair = Pair(sn, bPart)
                splits.add(newPair)
                subdivisions.add(bPart)
                sn.subdivision = subdivision + "(" + constraintNo + "A)"
                bPart.subdivision = subdivision + "(" + constraintNo + "B)"
            } else if (meetsAnti) {
                sn.subdivision = subdivision + "(" + constraintNo + "B)"
            } else if (meetsClock) {
                sn.subdivision = subdivision + "(" + constraintNo + "A)"
            }
        }
    }

    fun introduceConstraints(
        fg: MappedFlowGraph, ni: NudgeItem, constraintNumber: Int, corners: Int, note: String,
        source: Node, sink: Node, subs: Collection<SubdivisionNode>, ssp: ConstrainedSSP
    ): Int {

        // this will contain constraints for clockwise and anticlockwise
        // portions respectively
        return try {
            var cost = 0
            if (corners != 0) {
                addSourceAndSink(ni.portionsClockwise, source, ni.portionsAntiClockwise, sink, fg, subs)
                getReachable(source, sink)
                log.send(if (log.go()) null else "Nudge Number: $note")
                initializePortionSupplies(source, sink, corners)
                cost = ssp.maximiseFlow(fg)
                removeSourceAndSink(fg, source, sink)
            }
            cost
        } catch (e: LogicException) {
            removeSourceAndSink(fg, source, sink)
            Int.MAX_VALUE
        }
    }

    protected fun checkFlowGraphIntegrity(fg: MappedFlowGraph, source: Node, sink: Node) {
        for (n in fg.allNodes) {
            if (n.supply != -n.flow && n !== source && n !== sink) {
                throw LogicException("Flow graph in inconsistent state! $n")
            }
        }
    }

    protected fun displaySubdivisions(subs: Collection<SubdivisionNode>): Map<String, String> {
        val out: MutableMap<String, String> = HashMap()
        for (subdivisionNode in subs) {
            var sns = out[subdivisionNode.subdivision]
            if (sns == null) {
                sns = "\n"
            }
            sns += "\t\t" + subdivisionNode.getID() + " --- "
            val to: MutableSet<Node> = DetHashSet()
            for (a in subdivisionNode.arcs) {
                val otherEnd = a.otherEnd(subdivisionNode)
                to.add(otherEnd)
            }
            sns += """
                $to
                
                """.trimIndent()
            out[subdivisionNode.subdivision] = sns
        }
        return out
    }

    private fun removeSourceAndSink(fg: MappedFlowGraph, source: Node, sink: Node) {
        removeArcs(fg, source)
        removeArcs(fg, sink)
        fg.allNodes.remove(source)
        fg.allNodes.remove(sink)
    }

    /**
     * Sets up clockwise portions as sources and anticlockwise as sinks. All
     * other portions get set to zero.
     */
    private fun addSourceAndSink(
        clock: List<PortionNode>,
        source: Node,
        anti: List<PortionNode>,
        sink: Node,
        fg: MappedFlowGraph,
        subs: Collection<SubdivisionNode>
    ) {
        for (subdivisionNode in subs) {
            var done = false
            if (subdivisionNode.meets(anti)) {
                createSourceArc(sink, fg, subdivisionNode)
                done = true
            }
            if (subdivisionNode.meets(clock)) {
                if (done) {
                    throw LogicException("Should not meet clock and anti!")
                } else {
                    createSourceArc(source, fg, subdivisionNode)
                }
            }
        }
        fg.allNodes.add(source)
        fg.allNodes.add(sink)
    }

    private fun initializePortionSupplies(source: Node, sink: Node, corners: Int) {
        source.flow = 0
        source.supply = corners
        sink.flow = 0
        sink.supply = -corners
    }

    protected fun logSizes(
        logNodes: Collection<Node>,
        nudges: Table,
        note: String?,
        bestCost: String?,
        worstCost: String?
    ) {
        val portionSizes = IntArray(logNodes.size)
        var i = 0
        for (p in logNodes) {
            portionSizes[i++] = countPortionCorners(p)
        }
        nudges.addRow("", portionSizes, note, bestCost, worstCost)
        // log.send(log.go() ? null : "Nudges: ",nudges);
    }

    protected fun unlogSizes(nudges: Table) {
        nudges.removeLastRow()
    }

    protected fun calculateCornersRequired(ni: NudgeItem, bestDirection: Boolean, logs: Boolean): Int {
        val startFace = ni.getFirstFace()
        val startEdge = ni.portionsClockwise[0].edgeStartPosition
        val endFace = ni.lastFace
        val endEdge = ni.portionsClockwise[ni.portionsClockwise.size - 1].edgeEndPosition
        val firstEdge = getClockwiseDirection(startEdge, startFace)
        val lastEdge = getClockwiseDirection(endEdge, endFace)
        var cornersClockwise = countRequiredCorners(firstEdge, lastEdge, true)

        // add on face-crossing costs
        val faceCost = (ni.faceCount - 1) * 2
        cornersClockwise += faceCost

        // work out what the corner count is
        val actualClockwise = countActualCorners(ni.portionsClockwise)

        // work out how we require it to change to meet the constraint
        val clockChange = cornerChange(cornersClockwise, actualClockwise, bestDirection)
        val antiChange = -clockChange
        if (logs) {
            log.send(
                if (log.go()) null else ni.id.toString() + " starts " + startFace.getID() + "/" + startEdge + "/" + firstEdge + " ends "
                        + endFace.getID() + "/" + endEdge + "/" + lastEdge + ", requires portions: " + ni.portionsClockwise
                        + " corners " + clockChange + " ( currently : " + actualClockwise + ", needed: " + cornersClockwise
                        + " )"
            )
            log.send(if (log.go()) null else "Opposite route requires portions: " + ni.portionsAntiClockwise + " corners " + antiChange)
        }
        return clockChange
    }

    /**
     * Works out what the number of corners should be, given the actual.
     */
    private fun cornerChange(requiredMod: Int, actualClockwise: Int, bestDirection: Boolean): Int {
        var requiredMod = requiredMod
        val actualMod = (actualClockwise + 4) % 4
        requiredMod = (requiredMod + 4) % 4
        val c1: Int
        val c2: Int
        if (requiredMod == actualMod) {
            return 0
        }
        if (requiredMod < actualMod) {
            c1 = requiredMod - actualMod
            c2 = requiredMod + 4 - actualMod
        } else {
            c1 = requiredMod - 4 - actualMod
            c2 = requiredMod - actualMod
        }
        return if (bestDirection) {
            if (Math.abs(c1) < Math.abs(c2)) c1 else c2
        } else {
            if (Math.abs(c1) >= Math.abs(c2)) c1 else c2
        }
    }

    fun countPortionCorners(n: Node): Int {
        var count = 0
        for (a in n.arcs) {
            val otherEnd = a.otherEnd(n)
            if (!(otherEnd.type === ConstrainedFaceFlowOrthogonalizer.FACE_SUBDIVISION_NODE)) {
                val flow = a.getFlowFrom(n)
                count += flow
                //log.send(log.go() ? null : "Flow on "+a+" is "+flow);
            }
        }

        //log.send(log.go() ? null : "Total Flow on "+n+" = "+count);
        return count
    }

    private fun countActualCorners(portionsClockwise: List<PortionNode>): Int {
        var count = 0
        for (variable in portionsClockwise) {
            count += countPortionCorners(variable)
        }
        return count
    }

    private fun getPortionsForConstraint(fg: MappedFlowGraph, r: Route): List<PortionNode> {
        var r: Route? = r
        val portionsInvolved: MutableList<PortionNode> = ArrayList()
        while (r != null) {
            val `in` = r._in
            val out = r._out
            val f = r.face
            val p = getMatchingPortions(facePortionMap[f]!!, out, `in`, f.isOuterFace)
            portionsInvolved.addAll(p)
            r = r.rest
        }
        return portionsInvolved
    }

    private fun getMatchingPortions(
        list: List<PortionNode>,
        start: Int,
        end: Int,
        outerFace: Boolean
    ): List<PortionNode> {
        val out: MutableList<PortionNode> = ArrayList(list.size)
        for (i in list.indices) {
            val portionNode = list[i]
            if (portionNode.edgeStartPosition == start) {
                // found starting point
                for (j in list.indices) {
                    val toAdd = list[(i + j) % list.size]
                    out.add(toAdd)
                    if (toAdd.edgeEndPosition == end) {
                        return out
                    }
                }
            }
        }
        throw LogicException("Could not find portion between $start and $end")
    }

    /**
     * Returns the portions not included in portionsClockwise from the
     * implicated faces. This is then used as the anti-clockwise route
     */
    private fun getOppositePortions(
        fg: MappedFlowGraph,
        portionsClockwise: List<PortionNode>,
        r: Route
    ): List<PortionNode> {
        var r: Route? = r
        val portionsInvolved: MutableList<PortionNode> = ArrayList()
        while (r != null) {
            val f = r.face
            r = r.rest
            portionsInvolved.addAll(facePortionMap[f]!!)
        }
        portionsInvolved.removeAll(portionsClockwise)
        return portionsInvolved
    }

    /**
     * Returns the direction of an edge wrt a face.
     */
    private fun getClockwiseDirection(index: Int, f: Face): Direction {
        val constrainedEdge: Edge = f.getBoundary(index)
        val fromVertex = f.getCorner(index)
        return constrainedEdge.getDrawDirectionFrom(fromVertex)
            ?: throw LogicException("Was expecting a constrained edge: $constrainedEdge")
    }

    /**
     * Counts number of corners in a clockwise direction needed to get from da
     * to db.
     */
    private fun countRequiredCorners(da: Direction, db: Direction, clockwise: Boolean): Int {
        var da = da
        var corners = 0
        while (da !== db) {
            corners++
            da =
                if (clockwise) rotateClockwise(da) else rotateAntiClockwise(
                    da
                )
        }
        return corners
    }

    override val prefix: String
        get() = "NUDG"
    override val isLoggingEnabled: Boolean
        get() = false

    fun getReachable(source: Node, sink: Node) {
        val foundSource: MutableSet<Node> = DetHashSet()
        val foundSink: MutableSet<Node> = DetHashSet()
        reach(source, foundSource, true)
        reach(sink, foundSink, false)
        val common: MutableSet<Node> = DetHashSet<Node>(foundSink)
        common.retainAll(foundSource)
        log.send(
            "Total Nodes.  source=" + foundSource.size + " sink=" + foundSink.size + " coincindent=" + common.size,
            sort(
                ArrayList(common)
            )
        )
        log.send("Source Reachable: ", sort(ArrayList(foundSource)))
        log.send("Sink Reachable: ", sort(ArrayList(foundSink)))
    }

    private fun sort(out: List<Node>): List<Node> {
        return out.sortedWith(Comparator { o1, o2 -> o1.toString().compareTo(o2.toString()) })
    }

    private fun reach(node: Node, found: MutableSet<Node>, pushing: Boolean) {
        if (!found.contains(node)) {
            found.add(node)
            for (a in node.arcs) {
                val reversed = a.from !== node
                val capacity = a.hasCapacity(reversed == pushing)
                if (capacity) {
                    val otherEnd = if (a.from === node) a.to else a.from
                    reach(otherEnd, found, pushing)
                }
            }
        }
    }

    companion object {
        fun createSourceArc(source: Node, fg: MappedFlowGraph, c: Node) {
            var a = LinearArc(
                AbstractFlowOrthogonalizer.TRACE, Int.MAX_VALUE, 0, source, c, source
                    .getID()
                        + "-" + c.getID()
            )
            fg.allArcs.add(a)
            a = LinearArc(
                AbstractFlowOrthogonalizer.TRACE, Int.MAX_VALUE, 0, c, source, c.getID() + "-"
                        + source.getID()
            )
            fg.allArcs.add(a)
        }
    }
}