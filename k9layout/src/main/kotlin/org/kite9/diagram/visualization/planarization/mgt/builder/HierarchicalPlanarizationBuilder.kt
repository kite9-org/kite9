package org.kite9.diagram.visualization.planarization.mgt.builder

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ContainerLayoutEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.EdgeMapping
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.Tools.Companion.setUnderlyingContradiction
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization
import org.kite9.diagram.visualization.planarization.mgt.router.CrossingType
import org.kite9.diagram.visualization.planarization.mgt.router.GeographyType
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering

/**
 * This handles the creation of edges for a diagram element border, and the
 * introduction of edges to respect the ordering / direction settings
 * provided on the element.
 *
 * @author robmoffat
 */
class HierarchicalPlanarizationBuilder(em: ElementMapper, gp: GridPositioner) : DirectedEdgePlanarizationBuilder(em, gp) {

    override fun completeEmbedding(p: MGTPlanarization) {
        setupElementBorderEdges(p, p.diagram)
        super.completeEmbedding(p)
        if (!log.go()) {
            LAST_PLANARIZATION_DEBUG = p.toString()
        }
    }

    override fun processCorrectDirectedConnections(p: MGTPlanarization): Int {
        // does the container layout edges, which will also be directed.
        val containerLayoutEdges: MutableList<PlanarizationEdge> = mutableListOf()
        addContainerLayoutEdges(p.diagram, p, containerLayoutEdges)
        log.send("Layout edges:", containerLayoutEdges)
        for (edge in containerLayoutEdges) {
            edgeRouter.addPlanarizationEdge(
                p,
                edge,
                edge!!.getDrawDirection(),
                CrossingType.STRICT,
                GeographyType.STRICT
            )
        }
        return super.processCorrectDirectedConnections(p)
    }

    /**
     * Checks to see if a current edge links from this vertex to the previous
     * one.   If it does, and the container is directed, then we use that edge to
     * maintain the container direction
     */
    private fun checkIfNewBackEdgeNeeded(
        current: DiagramElement,
        prev: DiagramElement,
        pln: MGTPlanarization,
        inside: Container
    ): Boolean {
        if (inside.getLayout() === Layout.GRID) {
            return false // back edges not needed as elements are connected together.
        }
        var needed = checkForInsertedBackEdge(current, prev, pln, inside)
        if (!needed) {
            return false
        }
        needed = checkForUninsertedBackEdge(current, prev, pln, inside)
        return if (!needed) {
            false
        } else true
    }

    private fun checkForUninsertedBackEdge(
        vUnd: DiagramElement,
        prevUnd: DiagramElement,
        pln: MGTPlanarization,
        inside: Container
    ): Boolean {
        if (vUnd is Connected && prevUnd is Connected) {
            for (c in prevUnd.getLinks()) {
                if (c.meets(vUnd)) {
                    val prevUndVertex = getVertexFor(prevUnd)
                    val e = getEdgeForConnection(c, pln)
                    if (pln.uninsertedConnections.contains(e)) {
                        val d = getDirectionForLayout(inside)
                        val setOk = setEdgeDirection(e, d, prevUndVertex, false)
                        if (setOk) {
                            pln.uninsertedConnections.remove(e)
                            edgeRouter.addPlanarizationEdge(
                                pln,
                                e,
                                c.getDrawDirection(),
                                CrossingType.STRICT,
                                GeographyType.STRICT
                            )
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    private fun checkForInsertedBackEdge(
        vUnd: DiagramElement,
        prevUnd: DiagramElement,
        pln: MGTPlanarization,
        inside: Container
    ): Boolean {
        val d = getDirectionForLayout(inside)
        if (vUnd is ConnectedRectangular && prevUnd is ConnectedRectangular) {
            val vOrd = getRelevantEdgeOrdering(vUnd, pln)
            val prevOrd = getRelevantEdgeOrdering(prevUnd, pln)
            if (vOrd == null || vOrd.size() == 0 || prevOrd == null || prevOrd.size() == 0) {
                // can't be linked
                return true
            }
            val vSet = vOrd.getUnderlyingLeavers()
            for (e in prevOrd.getEdgesAsList()) {
                if (e is BiDirectionalPlanarizationEdge) {
                    val eUnd = e.getOriginalUnderlying()
                    if (eUnd != null && vSet.contains(eUnd)) {
                        val em = pln.edgeMappings[eUnd]
                        val start = em!!.startVertex
                        val edges: List<PlanarizationEdge> = em.edges
                        val b = getRoute(vUnd, prevUnd, start, edges, d, inside)
                        if (b != null) {
                            log.send("Using $eUnd as a back edge from $prevUnd to $vUnd, from=$start going=$d")
                            paintRoute(b, edges, d)
                            return false
                        }
                    }
                }
            }
        }
        return true // back edge still needed
    }

    private fun getDirectionForLayout(inside: Container): Direction {
        when (inside.getLayout()) {
            Layout.UP, Layout.DOWN, Layout.VERTICAL -> return Direction.DOWN
            Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> return Direction.RIGHT
        }
        throw LogicException("Unexpected layout: " + inside.getLayout())
    }

    private fun paintRoute(b: Route, edges: List<PlanarizationEdge>, d: Direction) {
        var start = b.sv
        val end = if (b.end == null) edges.size - 1 else b.end!!
        for (i in b.start..end) {
            val e: Edge = edges[i]
            setEdgeDirection(e, d, start, b.reverse)
            start = e.otherEnd(start!!)
        }
    }

    internal inner class Route {
        var start = 0
        var sv: Vertex? = null
        var end: Int? = null
        var reverse = false
    }

    private fun getRoute(
        vUnd: DiagramElement, prevUnd: DiagramElement,
        start: Vertex, edges: List<PlanarizationEdge>, d: Direction, inside: Container
    ): Route {
        throw UnsupportedOperationException()

//		Route b = null;
//		for (int j = 0; j < edges.size(); j++) {
//			Edge edge = edges.get(j);
//			boolean metV = start.getOriginalUnderlying()==vUnd;
//			boolean metVPrev = start.getOriginalUnderlying()==prevUnd;
//			
//			if ((metV || metVPrev) && (b!=null)) {
//				b.end = j-1;
//				return b;
//			}  
//				
//			if ((metVPrev || metV) && (b==null)) {
//				b = new Route();
//				b.start = j;
//				b.sv = start;
//				b.reverse = metV;					
//			} 
//			
//	//			if ((!metV && !metVPrev) && (b!=null)) {
//	//				// we are in the route - make sure nothing is interferes
//	//				DiagramElement under = start.getOriginalUnderlying();
//	//				if (under!=null) {
//	//					return null;
//	//				}
//	//			}
//			
//			start = edge.otherEnd(start);
//		}
//		
//		return b;
    }

    /**
     * Returns true if we were able to set the edge to the new direction
     */
    private fun setEdgeDirection(e: Edge, d: Direction, from: Vertex?, reverse: Boolean): Boolean {
        var d: Direction? = d
        d = if (reverse) reverse(d) else d
        val wrongDirection = e.getDrawDirection() != null && e.getDrawDirectionFrom(from!!) !== d
        if (wrongDirection) {
            log.error("Direction already set for " + e + " as " + e.getDrawDirectionFrom(from!!) + " wanted to set " + d)
            setUnderlyingContradiction(e, true)
            return false
        }
        log.send("Setting $e $d")
        (e as PlanarizationEdge).setDrawDirectionFrom(d, from!!)
        return true
    }

    private fun getRelevantEdgeOrdering(vUnd: DiagramElement, pln: Planarization): EdgeOrdering? {
        return if (vUnd is Container) {
            pln.edgeOrderings.get(vUnd)
        } else {
            val v = getVertexFor(vUnd)
            pln.edgeOrderings[v]
        }
    }

    /**
     * This ensures that the containers' vertices have edges outsideEdge and
     * below the planarization line to contain their content vertices.
     *
     * This creates an [EdgeMapping] in the planarization, which is an ordered list
     * of edges.  So, we have to journey round the perimeter vertices of the container and
     * create edges between them in order.
     */
    protected fun setupElementBorderEdges(p: MGTPlanarization, outer: DiagramElement) {
        if (em.hasOuterCornerVertices(outer)) {
            val cv = em.getOuterCornerVertices(outer)
            val originalLabel = outer.getID()
            val out : MutableList<PlanarizationEdge> = mutableListOf()
            val em = EdgeMapping(outer, out)
            p.edgeMappings[outer] = em
            var i = 0
            var fromv: MultiCornerVertex?
            var tov: MultiCornerVertex? = null
            val perimeterVertices = gridHelp.getClockwiseOrderedContainerVertices(cv)
            val iterator = perimeterVertices.iterator()
            while (iterator.hasNext()) {
                fromv = tov
                tov = iterator.next()
                if (fromv != null) {
                    addEdgeBetween(p, outer, originalLabel, em, i, fromv, tov)
                    i++
                }
            }

            // join back into a circle
            addEdgeBetween(
                p, outer, originalLabel, em, i,
                perimeterVertices[perimeterVertices.size - 1],
                perimeterVertices[0]
            )
            if (outer is Container) {
                for (c in outer.getContents()) {
                    setupElementBorderEdges(p, c)
                }
            }
        }
    }

    private fun addEdgeBetween(
        p: MGTPlanarization,
        outer: DiagramElement,
        originalLabel: String,
        em: EdgeMapping,
        i: Int,
        fromv: MultiCornerVertex,
        tov: MultiCornerVertex
    ) {
        val newEdge = updateEdges(originalLabel, outer, fromv, tov, i, em)
        if (newEdge != null) {
            edgeRouter.addPlanarizationEdge(
                p,
                newEdge as PlanarizationEdge,
                newEdge.getDrawDirection(),
                CrossingType.STRICT,
                GeographyType.STRICT
            )
        }
    }

    private fun updateEdges(l: String, c: DiagramElement, from: Vertex, to: Vertex, i: Int, em: EdgeMapping): Edge? {
        var from = from
        var d: Direction? = null
        val ax = routableReader.getBoundsOf(from.routingInfo, true)
        val ay = routableReader.getBoundsOf(from.routingInfo, false)
        val bx = routableReader.getBoundsOf(to.routingInfo, true)
        val by = routableReader.getBoundsOf(to.routingInfo, false)
        if (ax.compareTo(bx) == 0) {
            val comp = ay.compareTo(by)
            if (comp == -1) {
                d = Direction.DOWN
            } else if (comp == 1) {
                d = Direction.UP
            }
        } else if (ay.compareTo(by) == 0) {
            val comp = ax.compareTo(bx)
            if (comp == -1) {
                d = Direction.RIGHT
            } else if (comp == 1) {
                d = Direction.LEFT
            }
        }
        return if (d != null) {
            while (from !== to) {
                val e = getLeaverInDirection(from, d)
                if (e == null) {
                    val elementMap: MutableMap<DiagramElement, Direction?> =
                        HashMap()
                    elementMap[c] = rotateAntiClockwise(d)
                    val cbe =
                        BorderEdge((from as MultiCornerVertex), (to as MultiCornerVertex), l + d + i, d, elementMap)
                    em.add(cbe)
                    return cbe
                }
                if (e !is BorderEdge) {
                    throw LogicException("What is this?")
                }
                e.getDiagramElements().put(c, rotateAntiClockwise(d))
                em.add(e)
                from = e.otherEnd(from)
            }
            null
        } else {
            throw LogicException("Not dealt with this ")
        }
    }

    private fun getLeaverInDirection(from: Vertex, d: Direction): PlanarizationEdge? {
        for (e in from.getEdges()) {
            if (e.getDrawDirectionFrom(from) === d) {
                return e as PlanarizationEdge
            }
        }
        return null
    }

    protected fun addContainerLayoutEdges(c: Container, p: MGTPlanarization, toAdd: MutableList<PlanarizationEdge>) {
        val contents: List<Connected>?
        val layingOut = c.getLayout() != null
        contents = if (layingOut) {
            p.containerOrderingMap[c]
        } else {
            getConnectedContainerContents(c.getContents())
        }
        if (contents != null) {
            var prev: DiagramElement? = null
            for (current in contents) {
                if (prev != null && layingOut) {
                    log.send("Ensuring layout between $current and $prev")
                    checkAddLayoutEdge(p, c, toAdd, prev, current)
                }
                prev = current
                if (current is Container) {
                    addContainerLayoutEdges(current as Container, p, toAdd)
                }
            }
        }
    }

    private fun checkAddLayoutEdge(
        p: MGTPlanarization,
        c: Container,
        newEdges: MutableList<PlanarizationEdge>,
        prev: DiagramElement,
        current: DiagramElement
    ) {
        val needsDirectingBackEdge = checkIfNewBackEdgeNeeded(current, prev, p, c)
        // create a directing back edge
        if (needsDirectingBackEdge) {
            val d = getDirectionForLayout(c)
            val e = ContainerLayoutEdge(
                getVertexFor(prev),
                getVertexFor(current),
                d,
                (prev as ConnectedRectangular),
                (current as ConnectedRectangular)
            )
            val und = e.getOriginalUnderlying()
            val em = EdgeMapping(und, e)
            p.edgeMappings[und] = em
            log.send("Creating New Layout Edge: $e going $d")
            newEdges.add(e)
        }
    }

    private fun getVertexFor(c: DiagramElement): Vertex {
        if (em.hasOuterCornerVertices(c)) {
            val vertices = em.getOuterCornerVertices((c as Container)).getPerimeterVertices()
            for (cv in vertices) {
                if (cv.hasAnchorFor(c)) {
                    return cv
                }
            }
        }
        if (c is ConnectedRectangular) {
            return em.getPlanarizationVertex(c)
        }
        throw LogicException("Can't get a vertex for $c")
    }

    override fun getEdgeForConnection(c: BiDirectional<Connected>?, p: MGTPlanarization?): PlanarizationEdge {
        val from = c!!.getFrom()
        val to = c.getTo()
        val fromv = getVertexFor(from)
        val tov = getVertexFor(to)

        // make sure we keep the edge/connection mapping list up to date.
        val out = em.getEdge(from, fromv, to, tov, c)
        if (c is DiagramElement) {
            val em = EdgeMapping((c as DiagramElement?)!!, out!!)
            p!!.edgeMappings[(c as DiagramElement?)!!] = em
        }
        return out!!
    }

    companion object {

		var LAST_PLANARIZATION_DEBUG: String? = null
    }

    init {
        LAST_PLANARIZATION_DEBUG = null
    }
}