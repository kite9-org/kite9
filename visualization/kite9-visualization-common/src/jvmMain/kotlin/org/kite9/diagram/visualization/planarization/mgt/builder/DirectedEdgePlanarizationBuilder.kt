package org.kite9.diagram.visualization.planarization.mgt.builder

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.PortVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.Tools.Companion.isConnectionContradicting
import org.kite9.diagram.visualization.planarization.Tools.Companion.isConnectionRendered
import org.kite9.diagram.visualization.planarization.Tools.Companion.setUnderlyingContradiction
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization
import org.kite9.diagram.visualization.planarization.mgt.router.CrossingType
import org.kite9.diagram.visualization.planarization.mgt.router.EdgeRouter
import org.kite9.diagram.visualization.planarization.mgt.router.GeographyType
import org.kite9.diagram.visualization.planarization.mgt.router.MGTEdgeRouter

/**
 * This supplies extra logic to insert edges into the planarization. These are
 * done in the order they are held in the planarization's uninserted edges list.
 *
 * Those that cannot be inserted (having a given direction) are inserted again
 * with their direction relaxed.
 *
 * @author robmoffat
 */
abstract class DirectedEdgePlanarizationBuilder(em: ElementMapper, gp: GridPositioner, ef: DiagramElementFactory<*>) : MGTPlanarizationBuilder(em, gp, ef) {

    protected val edgeRouter: EdgeRouter by lazy {
        MGTEdgeRouter(routableReader, em)
    }

    internal enum class EdgePhase {
        SINGLE_DIRECTION, SINGLE_DIRECTION_CONTRADICTORS, FORWARDS_DIRECTIONS, RELAXED_DIRECTIONS
    }

    public override fun processConnections(p: MGTPlanarization) {
        log.send("Plan: $p")
        log.send("Adding Connections: ", p.uninsertedConnections)
        var count = processCorrectDirectedConnections(p)
        count = processMarkedContradictingConnections(p, count)
        count = processForwardDirectionConnections(p, count)
        count = processRelaxedDirectionConnections(p, count)
        val left = p.uninsertedConnections.size
        if (left > 0) {
            log.error("Failed to add $left connections:")
            for (e in p.uninsertedConnections) {
                log.error(e.toString())
                val redundant = p.edgeMappings.get(e as DiagramElement)
                if (redundant != null) {
                    for (edge in redundant.edges) {
                        edge.remove()
                    }
                }
            }
        } else {
            log.send("Added $count connections")
        }
    }

    protected fun processRelaxedDirectionConnections(p: MGTPlanarization, count: Int): Int {
        return addAllItems(p, EdgePhase.RELAXED_DIRECTIONS, count)
    }

    protected fun processMarkedContradictingConnections(p: MGTPlanarization, count: Int): Int {
        return addAllItems(p, EdgePhase.SINGLE_DIRECTION_CONTRADICTORS, count)
    }

    protected fun processForwardDirectionConnections(p: MGTPlanarization, count: Int): Int {
        return addAllItems(p, EdgePhase.FORWARDS_DIRECTIONS, count)
    }

    protected open fun processCorrectDirectedConnections(p: MGTPlanarization): Int {
        return addAllItems(p, EdgePhase.SINGLE_DIRECTION, 0)
    }

    private fun addAllItems(p: MGTPlanarization, ep: EdgePhase, runningCount: Int): Int {
        var count = runningCount
        val max = p.uninsertedConnections.size + runningCount //10;
        val iterator = p.uninsertedConnections.iterator()
        while (iterator.hasNext()) {
            val c = iterator.next()
            var done = false
            if (count < max) {
                done = handleInsertionPhase(p, ep, c)
            }
            if (done) {
                iterator.remove()
                count++
            }
        }
        return count
    }

    private fun handleInsertionPhase(p: MGTPlanarization, ep: EdgePhase, c: BiDirectional<Connected>): Boolean {
        val e = getEdgeForConnection(c, p)
        var done = false
        val contradicting = c is Connection && isConnectionContradicting(
            c
        )
        val rendered = c is Connection && isConnectionRendered(
            c
        )
        val directed = c.getDrawDirection() != null
        if (!rendered) {
            return true
        }
        when (ep) {
            EdgePhase.SINGLE_DIRECTION -> if (!contradicting && directed) {
                done = edgeRouter.addPlanarizationEdge(p, e, e.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT)
            }
            EdgePhase.SINGLE_DIRECTION_CONTRADICTORS -> if (contradicting && directed) {
                if ((e.getFrom() !is PortVertex) && (e.getTo() !is PortVertex)) {
                    // have a go at getting the connections in, on the off chance they will fit
                    // not allowed for port vertices, since directed edges from them must leave in
                    // the right direction
                    done = edgeRouter.addPlanarizationEdge(p, e, e.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT)
                    if (done) {
                        setUnderlyingContradiction(e, false)
                    }
                }
            }
            EdgePhase.FORWARDS_DIRECTIONS -> if (!contradicting && !directed) {
                // stops edges wrapping round containers with layout - go in a straight line
                val comm = getCommonContainer(c.getFrom(), c.getTo())
                var d: Direction? = null
                if (comm!!.getLayout() != null && comm.getLayout() !== Layout.GRID) {
                    d = getInsertionDirection(comm.getLayout(), e.getFrom(), e.getTo())
                    done = edgeRouter.addPlanarizationEdge(p, e, d, CrossingType.NOT_BACKWARDS, GeographyType.RELAXED)
                }
            }
            EdgePhase.RELAXED_DIRECTIONS -> {
                if (e.getDrawDirection() != null) {
                    setUnderlyingContradiction(e, true)
                }
                done = edgeRouter.addPlanarizationEdge(p, e, null, CrossingType.UNDIRECTED, GeographyType.RELAXED)
            }
        }
        if (!done) {
            p.edgeMappings.remove(c as DiagramElement)
        }
        return done
    }

    private fun getInsertionDirection(layoutDirection: Layout?, from: Vertex, to: Vertex): Direction {
        return when (layoutDirection) {
            Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> if (from.routingInfo!!.centerX() < to.routingInfo!!.centerX()) Direction.RIGHT else Direction.LEFT
            Layout.UP, Layout.DOWN, Layout.VERTICAL -> if (from.routingInfo!!.centerY() < to.routingInfo!!.centerY()) Direction.DOWN else Direction.UP
            else -> throw LogicException("Couldn't determine direction to insert in")
        }
    }

    protected abstract fun getEdgeForConnection(c: BiDirectional<Connected>?, p: MGTPlanarization?): PlanarizationEdge
}