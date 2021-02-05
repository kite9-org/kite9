package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.fg.Arc
import org.kite9.diagram.common.algorithms.det.DetHashSet

/**
 * Basic data object class for containing a flow network
 *
 * @author robmoffat
 */
open class FlowGraph(val allNodes: MutableList<Node>) {

    var allArcs: MutableSet<Arc> = DetHashSet()
        protected set

    override fun toString(): String {
        return """[FlowGraph:
	nodes:$allNodes
	arcs:$allArcs]"""
    }
}