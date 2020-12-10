package org.kite9.diagram.common.elements.edge

import org.kite9.diagram.common.elements.AbstractBiDirectional
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.RemovalType
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse

/**
 * This is an edge created in the planarization process.
 *
 * @author robmoffat
 */
abstract class AbstractPlanarizationEdge(private var f: Vertex, private var t: Vertex, private var d: Direction?) :
    AbstractBiDirectional<Vertex>(), PlanarizationEdge {

    override fun getBendCost(): Int {
        return 1
    }

    private val id = f.getID() + "-" + t.getID()

    override fun toString(): String {
        return "[" + getID() + "/" + f + "-" + t + "]"
    }

    override fun remove() {
        f.removeEdge(this)
        t.removeEdge(this)
    }

    abstract override fun removeBeforeOrthogonalization(): RemovalType
    abstract override fun getCrossCost(): Int

    @JvmField
	protected var straight = true

    override fun isStraightInPlanarization(): Boolean {
        return straight
    }

    fun setStraight(straight: Boolean) {
        this.straight = straight
    }

    override fun getFromArrivalSide(): Direction? {
        return reverse(getDrawDirection())
    }

    override fun getToArrivalSide(): Direction? {
        return getDrawDirection()
    }


    override fun setDrawDirectionFrom(dd: Direction?, end: Vertex) {
		d = if (end == getFrom()) {
			dd
		} else if (end == getTo()) {
			reverse(dd)
		} else {
			throw RuntimeException(
					"Trying to set direction from an end that's not set: " + end
							+ " in " + this
			)
		}
	}

    init {
        f.addEdge(this)
        t.addEdge(this)
    }

    override fun getFrom(): Vertex {
        return f
    }

    override fun getTo(): Vertex {
        return t
    }

    override fun getDrawDirection(): Direction? {
        return d
    }

    override fun setFrom(v: Vertex) {
        this.f = v
    }

    override fun setTo(v: Vertex) {
       this.t = v
    }

    override fun getID(): String {
        return id
    }
}