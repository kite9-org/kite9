package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement

/**
 * Stores the details of how an edge maps back to a connection within
 * the planarization
 *
 */
class EdgeMapping(
    val underlying: DiagramElement,
    val edges: MutableList<PlanarizationEdge>) {

    constructor(und: DiagramElement, e: PlanarizationEdge) : this(und, mutableListOf<PlanarizationEdge>(e)) {
    }

    init {
        checkTest()
    }

    fun replace(replace: PlanarizationEdge, a: PlanarizationEdge, b: PlanarizationEdge) {
        checkTest()
        val it = edges.listIterator()
        var before: PlanarizationEdge? = null
        var after: PlanarizationEdge? = null
        var current: PlanarizationEdge? = null
        while (it.hasNext()) {
            before = current
            current = it.next()
            after = peek(it)
            if (current === replace) {
                if (nullOrMeets(a, before) && nullOrMeets(b, after)) {
                    it.previous()
                    it.set(b)
                    it.add(a)
                    checkTest()
                } else if (nullOrMeets(b, before) && nullOrMeets(a, after)) {
                    it.previous()
                    it.set(a)
                    it.add(b)
                    checkTest()
                } else {
                    throw LogicException("Can't figure out replacement order")
                }
            }
        }
    }

    private fun peek(it: ListIterator<PlanarizationEdge>): PlanarizationEdge? {
        return if (it.hasNext()) {
            val out = it.next()
            it.previous()
            out
        } else {
            null
        }
    }

    private fun nullOrMeets(item: PlanarizationEdge, meeting: PlanarizationEdge?): Boolean {
        return if (meeting == null) {
            true
        } else item.meets(meeting)
    }

    fun remove(b: Edge?) {
        edges.remove(b)
        checkTest()
    }

    fun remove(edges: Collection<PlanarizationEdge>?) {
        this.edges.removeAll(edges!!)
        checkTest()
    }

    fun add(e2: PlanarizationEdge) {
        edges.add(e2)
        checkTest()
    }

    private fun checkTest() {
        if (TEST_AFTER_CHANGE) {
            if (edges.size == 0) return
            val met: MutableSet<Vertex> = UnorderedSet()
            //System.out.println("CHECKTEST for: "+underlying+"\n"+this);
            var f = startVertex
            for (e in edges) {
                if (e.meets(f)) {
                    f = e.otherEnd(f)
                    if (met.contains(f)) {
                        throw LogicException("Edge route already visits $f")
                    }
                    met.add(f)
                } else {
                    throw LogicException("EdgeMapping is broken $edges since $e doesn't meet $f")
                }
            }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[EdgeMapping: \n")
        for (e in edges) {
            sb.append("  ")
            sb.append(e)
            sb.append("\t")
            sb.append(e.getFrom())
            sb.append("\t")
            sb.append(e.getTo())
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    val startVertex: Vertex
        get() {
            val iterator: Iterator<PlanarizationEdge> = edges.iterator()
            return firstVertex(iterator)
        }
    val endVertex: Vertex
        get() {
            val iterator = edges.asReversed().iterator()
            return firstVertex(iterator)
        }

    private fun firstVertex(iterator: Iterator<PlanarizationEdge>): Vertex {
        val first = iterator.next()
        if (edges.size == 1) {
            return first.getFrom()
        }
        var start: Vertex? = null
        val second: Edge = iterator.next()
        val commonFrom = second.meets(first.getFrom())
        val commonTo = second.meets(first.getTo())
        start = if (commonFrom && commonTo) {
            // doesn't matter.
            return first.getFrom()
        } else if (commonFrom) {
            first.getTo()
        } else if (commonTo) {
            first.getFrom()
        } else {
            throw LogicException("Can't determine start of edge map")
        }
        return start
    }

    companion object {
        const val TEST_AFTER_CHANGE = true
    }
}