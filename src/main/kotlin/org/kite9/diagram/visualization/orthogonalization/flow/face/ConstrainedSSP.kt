package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.fg.*
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph

class ConstrainedSSP(var source: Node, var sink: Node, var splits: List<Pair<SubdivisionNode>>) :
    FlowGraphSPP<MappedFlowGraph>() {
    /**
     * This implementation makes sure that if the path completes, then
     * it is only allowed to complete at a node which won't break the constraints
     */
    override fun generateNewPath(p: Path, reversed: Boolean, a: Arc): Path? {
        val beginNode = p.endNode
        val endNode = a.otherEnd(beginNode)
        if (beginNode === source || beginNode === sink) {
            if (endNode is SubdivisionNode) {
                // ensure it is in splits
                for (i in splits.indices) {
                    val psn = splits[i]
                    if (psn.a == endNode || psn.b == endNode) {
                        return super.generateNewPath(p, reversed, a)
                    }
                }
                return null
            }
        }
        if (endNode === source || endNode === sink) {
            val splitB = getStartNodeButOne(p)
            for (pair in splits) {
                if (pair.a == beginNode) {
                    if (pair.b == splitB) {
                        return super.generateNewPath(p, reversed, a)
                    }
                } else if (pair.a == splitB) {
                    if (pair.b == beginNode) {
                        return super.generateNewPath(p, reversed, a)
                    }
                }
            }
            return null
        }
        return super.generateNewPath(p, reversed, a)
    }

    /**
     * Since the path is also dependent on the split you are in, this must be part of the location
     * object.  Otherwise, lower cost splits will override higher cost, different splits.
     */
    override fun getLocation(path: Path): Any {
        val splitB = getStartNodeButOne(path) ?: return path.getLocation()
        return SplitLocation(splitB, path.getLocation())
    }

    fun getStartNodeButOne(p: Path?): Node? {
        return if (p!!.nextPathItem != null) {
            if (p.nextPathItem!!.nextPathItem == null) {
                p.endNode
            } else getStartNodeButOne(p.nextPathItem)
        } else {
            null
        }
    }

    override fun displayFlowInformation(fg: FlowGraph) {
        // do nothing to reduce logging
    }
}