package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

/**
 * This algorithm uses a Dijkstra-style shortest path in order to maximise flow
 * and minimise cost over the network.
 *
 * @author robmoffat
 */
open class FlowGraphSPP<X : FlowGraph> : AbstractSSP<Path>(), FlowAlgorithm<X>, Logable {

    private var log = Kite9Log(this)
    private var destination: List<Node>? = null
    private var startingPoints: List<Node>? = null
    var iterations = 0
    var cost = 0
    var paths: Long = 0

    override fun maximiseFlow(fg: X): Int {
        iterations = 0
        paths = 0
        cost = 0
        var p: Path? = null
        while (true) {
            p = getShortestPath(fg)
            if (p == null) break
            p.pushFlow(1)
            cost += p.getCost()
            val s: State<Path> = lastState!!
            log.send(
                if (log.go()) null else "Round " + iterations + " generated " + s.getAdds() + " paths, maxstack " + s.getMaxStack() + " chose " + displayPath(
                    p
                )
                        + " pushing 1 with cost " + p.getCost()
            )
            paths += s.getAdds()
        }
        log.send(
            if (log.go()) null else """Completed flow maximisation with: 
	iterations: $iterations
	paths:    $paths
	cost:     $cost
	nodes:    ${fg!!.allNodes.size}
	arcs:     ${fg.allArcs.size}"""
        )
        if (!log.go()) {
            val out = File("ssp.info")
            try {
                val w = FileWriter(out, true)
                w.write(
                    """
    $paths,$cost,${fg!!.allNodes.size},${fg.allArcs.size}
    
    """.trimIndent()
                )
                w.close()
            } catch (e: IOException) {
                throw LogicException("could not write ssp file: ", e)
            }
        }
        displayFlowInformation(fg)
        return cost
    }

    private fun displayPath(p: Path?): String {
        if (p == null) return ""
        val next = p.nextPathItem
        return if (next != null) {
            p.endNode.toString() + " -- " + displayPath(next)
        } else {
            p.endNode.toString()
        }
    }

    open fun displayFlowInformation(fg: FlowGraph) {
        if (!LOG_FLOW_INFORMATION) return
        val lines: MutableList<String> = ArrayList()
        for (n in fg.allNodes) {
            n.ensureEulersEquilibrium()
            val arcInfo = StringBuffer()
            for (a in n.arcs) {
                if (a.flow != 0) {
                    if (a.from === n) {
                        arcInfo.append(a.toString() + " " + a.flow + "   ")
                    } else {
                        arcInfo.append(a.toString() + " " + -a.flow + "   ")
                    }
                }
            }
            lines.add(
                "Flow on: " + n.getID() + " = " + n.flow + ", requires " + n.supply + ", due to "
                        + arcInfo
            )
        }

        lines.sort()

        log.send(if (log.go()) null else "Flow Information", lines)
    }

    /**
     * Returns a lowest-cost path from source to sink, using Dijkstra algorithm
     */
    fun getShortestPath(fg: FlowGraph): Path? {
        iterations++
        destination = getResidualSources(fg)
        startingPoints = getResidualSinks(fg)
        if (startingPoints!!.size == 0 && destination!!.size == 0) {
            return null
        }
        if (startingPoints!!.size == 0 || destination!!.size == 0) {
            // we have a problem, since you need both a starting point and a
            // destination
            displayFlowInformation(fg)
            log.send(if (log.go()) null else "New path not available from " + startingPoints.toString() + " TO: " + destination.toString())
            throw LogicException("Graph is unbalanced: $startingPoints vs $destination")
        }
        return try {
            createShortestPath()
        } catch (nsee: NoFurtherPathException) {
            displayFlowInformation(fg)
            displayRemainderInfo(startingPoints)
            displayRemainderInfo(destination)
            throw LogicException(
                "Graph cannot be completed after " + iterations + ".  Please check directional constraints don't prohibit diagram from drawing: "
                        + startingPoints + " to " + destination, nsee
            )
        } catch (other: Throwable) {
            throw LogicException(
                "Graph cannot be completed after " + iterations + ":" + startingPoints + " to " + destination + " has "
                        + fg.allNodes.size + " nodes and " + fg.allArcs.size + " arcs"
                        + " paths, lowest cost " + cost, other
            )
        }
    }

    private fun displayRemainderInfo(ns: List<Node>?) {
        for (node in ns!!) {
            val memento = if (node is SimpleNode) node.representation else null
            log.error("Node: $node\n\t$memento")
        }
    }

    override fun generateSuccessivePaths(p: Path, pq: State<Path>) {
        val fromNode = p.endNode
        for (a in fromNode.arcs) {
            if (!p.contains(a)) {
                val reversed = a.from !== fromNode
                val to = if (reversed) a.from else a.to
                val capacity = a.hasCapacity(reversed)
                if (capacity && !checkForLoopback(to, p)) {
                    val np = generateNewPath(p, reversed, a)
                    if (np != null) pq.add(np)
                }
            }
        }
    }

    private fun checkForLoopback(to: Node, p: Path): Boolean {
        //return p.contains(to);
        return false
    }

    protected open fun generateNewPath(p: Path, reversed: Boolean, a: Arc): Path? {
        return if (a.hasCapacity(reversed)) {
            Path(p, a, reversed)
        } else {
            null
        }
    }

    fun getResidualSources(fg: FlowGraph): List<Node> {
        val out: MutableList<Node> = ArrayList()
        for (n in fg.allNodes) {
            if (n.getResidualStatus() === Node.ResidualStatus.SOURCE) {
                out.add(n)
            }
        }
        return out
    }

    fun getResidualSinks(fg: FlowGraph): List<Node> {
        val out: MutableList<Node> = ArrayList()
        for (n in fg.allNodes) {
            if (n.getResidualStatus() === Node.ResidualStatus.SINK) {
                out.add(n)
            }
        }
        return out
    }

    override fun createInitialPaths(pq: State<Path>) {
        for (n in destination!!) {
            pq.add(Path(n))
        }
    }

    override fun pathComplete(r: Path): Boolean {
        return startingPoints!!.contains(r.endNode)
    }

    fun getDestination() : List<Node> {
        return destination!!
    }

    companion object {
        const val LOG_FLOW_INFORMATION = false
    }
}