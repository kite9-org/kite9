package org.kite9.diagram.visualization.planarization.ordering

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting

abstract class AbstractEdgeOrdering : EdgeOrdering {


	protected var directions: Any? = null

    override fun getEdgeDirections(): Any? {
        return directions
    }

    fun addEdgeDirection(d: Direction?, contradicting: Boolean) {
        if (d == null) {
            return
        }
        if (contradicting) {
            return
        }
        if (directions == null) {
            directions = d
        } else if (directions !== d) {
            directions = EdgeOrdering.MUTLIPLE_DIRECTIONS
        }
    }

    protected abstract fun getInterceptDirection(e: Edge): Direction

    override fun canInsert(before: PlanarizationEdge, d: Direction?, clockwise: Boolean, log: Kite9Log): Boolean {
        var before = before
        if (before.getDrawDirection() == null || isUnderlyingContradicting(before)) {
            // need to find directed edge
            val it = getIterator(!clockwise, before, null, true)
            it.next()
            before = it.next()
        }
        val it = getIterator(clockwise, before, null, true)
        it.next()
        val after: Edge = it.next()
        val turns = turnsBetween(before, after, d, clockwise)
        log.send("Routing between " + before + " " + after + " going " + (if (clockwise) "clockwise" else "anticlockwise") + " ok=" + (turns < 4))
        return turns < 4
    }

    /**
     * Turns round the circle and sees if introducing the turn will create a contradiction
     */
    private fun turnsBetween(first: Edge, next: Edge, fromD: Direction?, clockwise: Boolean): Int {
        val aboveDD = getInterceptDirection(first)
        var turns = 0
        turns += turnTo(aboveDD, fromD, clockwise)
        val belowDD = getInterceptDirection(next)
        turns += turnTo(fromD, belowDD, clockwise)
        return turns
    }

    private fun turnTo(start: Direction?, finish: Direction?, clockwise: Boolean): Int {
        var start = start
        var turns = 0
        if (start === finish) {
            return 0
        }
        do {
            start = if (clockwise) rotateClockwise(start!!) else rotateAntiClockwise(
                start!!
            )
            turns++
            if (start === finish) {
                return turns
            }
        } while (start !== finish || turns > 4)
        throw LogicException("can't turn to: $finish")
    }

    protected abstract inner class AbstractEdgeIterator(
        clockwise: Boolean,
        startingAt: PlanarizationEdge?,
        finish: PlanarizationEdge?,
        directedOnly: Boolean
    ) : MutableIterator<PlanarizationEdge> {
        private val finish: PlanarizationEdge?
        private var next: PlanarizationEdge? = null
        private val directed: Boolean
        private fun checkEdge(init: Boolean) {
            var ok: Boolean
            do {
                if (!init && next === finish) {
                    next = null
                    return
                }
                ok = true
                if (directed && edgeIsNotDirected()) {
                    ok = false
                }
                if (!ok) {
                    next = getNext()
                }
            } while (!ok)
        }

        private fun edgeIsNotDirected(): Boolean {
            return next!!.getDrawDirection() == null || isUnderlyingContradicting(next)
        }

        abstract fun getNext(): PlanarizationEdge?

        override fun hasNext(): Boolean {
            return next != null
        }

        override fun next(): PlanarizationEdge {
            val out = next
            next = getNext()
            checkEdge(false)
            return out!!
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }

        init {
            next = startingAt
            checkEdge(true)
            directed = directedOnly
            this.finish = finish
        }
    }

    override fun changed() {
        underlyingCache = null
    }

    private var underlyingCache: Set<DiagramElement>? = null

    override fun getUnderlyingLeavers(): Set<DiagramElement> {
        throw UnsupportedOperationException()

    }
}