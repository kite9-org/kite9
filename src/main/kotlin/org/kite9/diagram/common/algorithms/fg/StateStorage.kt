package org.kite9.diagram.common.algorithms.fg


/**
 * Allows you to store and restore the state of a flow graph.
 *
 * @author robmoffat
 */
object StateStorage {

    @JvmStatic
	fun storeState(fg: FlowGraph): Map<Any, Int> {
        val state: MutableMap<Any, Int> = HashMap(100)
        for (n in fg.allNodes) {
            state[n] = n.flow
        }
        for (a in fg.allArcs) {
            state[a] = a.flow
        }
        return state
    }

    @JvmStatic
	fun restoreState(fg: FlowGraph, state: Map<Any, Int>) {
        for (n in fg.allNodes) {
            val flow = state[n]!!
            n.flow = flow
            n.supply = -flow
        }
        for (a in fg.allArcs) {
            val `val` = state[a]
            if (`val` != null) a.flow = `val`
        }
    }
}