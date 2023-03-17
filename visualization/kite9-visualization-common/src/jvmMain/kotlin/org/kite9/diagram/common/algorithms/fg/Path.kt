package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.ssp.PathLocation

/**
 * @author robmoffat
 */
class Path : PathLocation<Path> {
	var endNode: Node
	var nextPathItem: Path? = null
    private var route: Arc?
    private var reversed = false
    private var cost = 0
    private var length = 0

    constructor(n: Node) {
        endNode = n
        route = null
    }

    /**
     * Extends an existing path to create a new path
     */
    constructor(p: Path, a: Arc, reversed: Boolean) {
        endNode = a.otherEnd(p.endNode)
        nextPathItem = p
        route = a
        this.reversed = reversed
        cost += p.cost + if (reversed) a.getIncrementalCost(-1) else a.getIncrementalCost(+1)
        length = p.length + 1
    }

    /**
     * Used to say whether this route has joined up with one of the start nodes.
     */
    fun meets(destination: List<Node?>): Boolean {
        return destination.contains(startNode)
    }

    val startNode: Node
        get() = if (nextPathItem != null) {
            nextPathItem!!.startNode
        } else {
            endNode
        }

    operator fun contains(a: Arc): Boolean {
        return if (route === a) {
            true
        } else if (nextPathItem == null) {
            false
        } else {
            nextPathItem!!.contains(a)
        }
    }

    operator fun contains(a: Node): Boolean {
        return if (endNode === a) {
            true
        } else if (nextPathItem == null) {
            false
        } else {
            nextPathItem!!.contains(a)
        }
    }

    override fun toString(): String {
        return (if (route == null) "" else (if (reversed) "!" else "") + route.toString()) +
                (if (nextPathItem == null) "" else "/" + nextPathItem.toString()) + ":" + cost
    }

    fun pushFlow(flow: Int) {
        if (route != null) {
            route!!.pushFlow((if (reversed) -1 else 1) * flow)
            nextPathItem!!.pushFlow(flow)
        }
    }

    override operator fun compareTo(o: Path): Int {
        return if (cost > o.cost) {
            1
        } else if (cost < o.cost) {
            -1
        } else if (length > o.length) {
            1
        } else if (length < o.length) {
            -1
        } else {
            0
        }
    }

    override fun getLocation(): Any {
        return endNode
        // return null;
    }

    private var active = true
    override fun isActive(): Boolean {
        return active
    }

    override fun setActive(a: Boolean) {
        active = a
    }

    fun getCost() : Int {
        return cost;
    }
}