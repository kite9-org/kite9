package org.kite9.diagram.common.elements.edge

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * Edge interface implemented by all of the Edges created by the planarization process.
 *
 * @author robmoffat
 */
interface PlanarizationEdge : Edge {

    enum class RemovalType {
        YES, NO, TRY
    }

    /**
     * Indicates that the edge is just a temporary edge used only during the planarization process
     */
    fun removeBeforeOrthogonalization(): RemovalType?

    /**
     * Indicates the cost of introducing an edge crossing this one
     * @return
     */
    fun getCrossCost(): Int

    /**
     * Indicates the cost of introducing a bend on this edge, relative to other edges.
     * Note that directed edges generally wont accept bends at all.
     */
    fun getBendCost(): Int

    /**
     * Returns true if this edge heads through the planarization in a straight line
     */
    fun isStraightInPlanarization(): Boolean

    /**
     * Returns true if the edge is enforcing a particular layout in the orthogonalization
     * step.  Indicates that no other edges should ideally leave or enter the same side.
     */
    fun isLayoutEnforcing(): Boolean
    fun setLayoutEnforcing(le: Boolean)

    /**
     * Performs a split on the current edge.  Returns 2 edge attr.  The original edge should then be discarded.
     */
    fun split(toIntroduce: Vertex?): Array<PlanarizationEdge>

    /**
     * Gives you information about the elements surrounding this one, and (potentially) which side
     * they are on.
     */
    fun getDiagramElements(): Map<DiagramElement, Direction>
    fun setFrom(v: Vertex)
    fun setTo(v: Vertex)

    /**
     * Unlinks the edge from the from, to vertices it is connected to.
     */
    fun remove()

    /**
     * If drawDirection is set, this will be the reverse of draw direction.
     */
    fun getFromArrivalSide(): Direction?

    /**
     * If drawDirection is set, this will be the same as draw direction.
     */
    fun getToArrivalSide(): Direction?
    fun setDrawDirectionFrom(dd: Direction?, end: Vertex)
}