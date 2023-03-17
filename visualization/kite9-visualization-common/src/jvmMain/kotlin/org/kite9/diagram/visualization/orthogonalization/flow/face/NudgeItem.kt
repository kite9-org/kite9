package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.algorithms.fg.SimpleNode
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.visualization.planarization.Face
import kotlin.math.min


class NudgeItem(
    var id: Int,
    private val r: Route,
    val faceCount: Int,
    val portionsClockwise: List<PortionNode>,
    val portionsAntiClockwise: List<PortionNode>
) {
    enum class NudgeItemType {
        SINGLE_CORNER, SINGLE_FACE, MULTI_FACE
    }

	val source: Node = SimpleNode("source-$id", 0, null)


	val sink: Node = SimpleNode("sink-$id", 0, null)


	val type = calculateType()

    val lastEdge: Edge by lazy {
        var c: Route = r
        while (c.rest != null) {
            c = c.rest!!
        }
        c.inEdge
    }

    val lastFace: Face by lazy {
        var c: Route = r
        while (c.rest != null) {
            c = c.rest!!
        }
        c.face
    }

    fun calculateType() : NudgeItemType{
        return if (faceCount > 1) {
            NudgeItemType.MULTI_FACE
        } else if (isCornerPortion(portionsAntiClockwise) || isCornerPortion(portionsClockwise)) {
            NudgeItemType.SINGLE_CORNER
        } else {
            NudgeItemType.SINGLE_FACE
        }
    }

    private fun isCornerPortion(lp: List<PortionNode>?): Boolean {
        return if (lp!!.size == 1) {
            val p = lp[0]
            val ces = p.constrainedEdgeStart
            val cee = p.constrainedEdgeEnd
            if (ces!!.meets(cee) && cee != null) {
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun portionCount(): Int {
        return min(portionsClockwise!!.size, portionsAntiClockwise!!.size)
    }

    override fun toString(): String {
        val se: Edge = getFirstEdge()
        val ee = lastEdge
        return "NI" + id + " " + (if (portionsClockwise != null) portionsClockwise.toString() else "") + " " + r + " " + type + " from=" + se + "(" + getUnderlyings(
            se
        ) + ") to=" + ee + "(" + getUnderlyings(ee) + ")"
    }

    private fun getUnderlyings(se: Edge?): String {
        return if (se is PlanarizationEdge) {
            se.getDiagramElements().keys.toString()
        } else ""
    }

    fun getFirstEdge(): Edge {
        return r.outEdge
    }

    fun getFirstFace(): Face {
        return r.face
    }


}