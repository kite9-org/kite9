package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.vertex.ConnectedRectangularVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.HPos
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.VPos
import org.kite9.diagram.model.style.BorderTraversal

class ElementMapperImpl(private val gp: GridPositioner) : ElementMapper {

    private var singleVertices: MutableMap<DiagramElement, Vertex> = HashMap()
    private var cornerVertices: MutableMap<DiagramElement, CornerVertices> = HashMap()
    private var baseGrids: MutableMap<DiagramElement?, BaseGridCornerVertices> = HashMap()
    private var edges: MutableMap<BiDirectional<Connected>, PlanarizationEdge> = HashMap()
    private var hasConnections: MutableMap<DiagramElement?, Boolean> = HashMap()

    override fun hasOuterCornerVertices(d: DiagramElement): Boolean {
        return cornerVertices.containsKey(d)
    }

    override fun getOuterCornerVertices(c: DiagramElement): CornerVertices {
        var v = cornerVertices[c]
        if (v == null) {
            if (isEmbeddedWithinGrid(c)) {
                val parentCV = getBaseGridCornerVertices(c.getParent() as Container?)
                v = createSubGridCornerVertices(c, parentCV)
            } else {
                v = IndependentCornerVertices(c, c.getDepth())
                cornerVertices[c] = v
            }
        }
        return v
    }

    private fun createSubGridCornerVertices(
        c: DiagramElement,
        parentCV: BaseGridCornerVertices
    ): SubGridCornerVertices {
        val xspan = (c as Rectangular).getRenderingInformation().gridXPosition()
        val yspan = c.getRenderingInformation().gridYPosition()
        val v = SubGridCornerVertices(c, xspan!!, yspan!!, parentCV, c.getDepth())
        cornerVertices[c] = v
        return v
    }

    private fun getBaseGridCornerVertices(c: Container?): BaseGridCornerVertices {
        var bgcv = baseGrids[c]
        if (bgcv == null) {
            bgcv = BaseGridCornerVertices(c!!, c!!.getDepth() + 1)
            baseGrids[c] = bgcv
        }
        return bgcv
    }

    private fun isEmbeddedWithinGrid(c: DiagramElement): Boolean {
        val parent = c.getParent()
        if (c is Label) {
            return false
        }
        return if (parent != null && parent is Container) {
            parent.getLayout() == Layout.GRID
        } else false
    }

    override fun getEdge(
        from: Connected, vfrom: Vertex, to: Connected, vto: Vertex,
        element: BiDirectional<Connected>
    ): PlanarizationEdge? {
        var e = edges[element]
        val dd = element.getDrawDirectionFrom(from)
        return if (e == null) {
            e = if (element is Connection) {
                ConnectionEdge(vfrom, vto, element, dd)
            } else if (element is GeneratedLayoutBiDirectional) {
                ContainerLayoutEdge(vfrom, vto, dd!!, from, to)
            } else {
                throw LogicException("Unknown BiDirectional type: $element")
            }
            if (element != null) {
                edges[element] = e
            }
            e
        } else {
            val oldFrom = e.getFrom()
            oldFrom.removeEdge(e)
            vfrom.addEdge(e)
            e.setFrom(vfrom)
            val oldTo = e.getTo()
            oldTo.removeEdge(e)
            vto.addEdge(e)
            e.setTo(vto)
            e.setDrawDirectionFrom(dd, vfrom)
            e
        }
    }

    override fun getPlanarizationVertex(de: DiagramElement): Vertex {
        var v = singleVertices[de]
        if (v == null) {
            if (de is ConnectedRectangular) {
                v = ConnectedRectangularVertex(de.getID(), de)
                singleVertices[de] = v
            }else if (de is Port) {
                val c1 = de.getContainer()!!
                val cv = getOuterCornerVertices(c1)
                val (fracX, fracY) = buildFractions(c1, de)
                v = cv.createVertex(fracX, fracY,
                    HPos.getFromDirection(de.getPortDirection()), VPos.getFromDirection(de.getPortDirection()),
                    c1, de)

                singleVertices[de] = v
            } else {
                throw LogicException("Not sure how to create vertex for $de")
            }
        }
        return v
    }

    private fun buildFractions(c1: Container, de: Port): Pair<LongFraction, LongFraction> {
        val portsOnSide = c1.getContents()
            .filterIsInstance<Port>()
            .filter { it.getPortDirection() == de.getPortDirection() }
            .sortedBy { it.getPortPosition() }

        val num = portsOnSide.indexOf(de) + 1
        val denom = portsOnSide.size + 1
        val fraction = LongFraction(num.toLong(), denom.toLong())

        return when (de.getPortDirection()) {
            Direction.UP -> Pair(fraction, LongFraction.ZERO)
            Direction.DOWN -> Pair(fraction, LongFraction.ONE)
            Direction.LEFT -> Pair(LongFraction.ZERO, fraction)
            Direction.RIGHT -> Pair(LongFraction.ONE, fraction)
        }
    }

    /**
     * Debug only - very slow
     */
    override fun allVertices(): Collection<Vertex> {
        val out: MutableCollection<Vertex> = ArrayList(singleVertices.values)
        for (cv in cornerVertices.values) {
            for (vertex in cv.getVerticesAtThisLevel()) {
                out.add(vertex)
            }
        }
        return out
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