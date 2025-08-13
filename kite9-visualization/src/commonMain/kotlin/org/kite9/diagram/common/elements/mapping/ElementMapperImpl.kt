package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.model.style.Placement

class ElementMapperImpl(private val gp: GridPositioner) : ElementMapper {

    private var hasConnections: MutableMap<DiagramElement?, Boolean> = HashMap()
    private var fractions: MutableMap<Pair<DiagramElement, Direction>, Map<DiagramElement, LongFraction>> = hashMapOf()

    override fun getFractions(c: DiagramElement, d: Direction): Map<DiagramElement, LongFraction> {
        return fractions.getOrPut(Pair(c, d), {
            val allItems: List<Pair<DiagramElement, Placement>> = if (c is Container) {
                c.getContents()
                    .filterIsInstance<Port>()
                    .filter { it.getPortDirection() == d }
                    .map { it to it.getContainerPosition() }
            } else {
                emptyList()
            } + if (c is Connected) {
                listOf(c to c.getConnectionAlignment(d))
            } else {
                emptyList()
            }

            val denom = allItems.size + 1

            allItems
                .sortedBy { it.second }
                .mapIndexed { idx, v -> v.first to LongFraction((idx+1).toLong(), denom.toLong()) }
                .toMap()
        })
    }

    override fun requiresPlanarizationCornerVertices(c: DiagramElement): Boolean {
        if (c is Diagram) {
            return true
        }
        // does anything inside it have connections?
        if (c is Container) {
            for (de in c.getContents()) {
                if (hasNestedConnections(de)) {
                    return true
                }
            }

            // are connections allowed to pass through it?
            val canTraverse = isElementTraversible(c)
            if (canTraverse && hasNestedConnections(c)) {
                return true
            }
        }

        // is it embedded in a grid?  If yes, use corners
        if (c is ConnectedRectangular) {
            val l = if (c.getParent() == null) null else (c.getParent() as Container?)!!.getLayout()
            return l == Layout.GRID
        }

        return false
    }

    private fun isElementTraversible(c: DiagramElement): Boolean {
        return isElementTraversible(c, Direction.UP) ||
                isElementTraversible(c, Direction.DOWN) ||
                isElementTraversible(c, Direction.LEFT) ||
                isElementTraversible(c, Direction.RIGHT)
    }

    private fun isElementTraversible(c: DiagramElement, d: Direction): Boolean {
        return if (c is Container) {
            c.getTraversalRule(d) == BorderTraversal.ALWAYS
        } else false
    }

    fun hasNestedConnections(c: DiagramElement?): Boolean {
        if (hasConnections.containsKey(c)) {
            return hasConnections[c]!!
        }
        var has = false
        if (c is Connected) {
            has = c.getLinks().size > 0
        }
        if (has == false && c is Container) {
            for (de in c.getContents()) {
                if (hasNestedConnections(de)) {
                    has = true
                    break
                }
            }
        }
        hasConnections[c] = has
        return has
    }

    override fun getGridPositioner(): GridPositioner {
        return gp
    }
}