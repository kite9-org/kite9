package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.elements.grid.FracMapper
import org.kite9.diagram.common.elements.grid.FracMapperImpl
import org.kite9.diagram.common.elements.mapping.CornerVertices
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.mapping.SubGridCornerVertices
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.getOrdForXDirection
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.getOrdForYDirection
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.common.fraction.LongFraction.Companion.ONE_HALF
import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.HPos
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.VPos
import kotlin.math.min

class VertexPositionerImpl(
    private val em: ElementMapper,
    private val rh: RoutableHandler2D,
    private val cmp: (DiagramElement, DiagramElement) -> Int
) : Logable, VertexPositioner {

    private val fracMapper: FracMapper = FracMapperImpl()
    private val log = Kite9Log.instance(this)

    // temp workspace
    private var borderTrimAreaX = .25
    private var borderTrimAreaY = .25


    override fun checkMinimumGridSizes(ri: RoutingInfo) {
        if (ri is PositionRoutingInfo) {
            val pri = ri
            borderTrimAreaX = min(borderTrimAreaX, pri.getWidth() / 4.0)
            borderTrimAreaY = min(borderTrimAreaY, pri.getHeight() / 4.0)
        }
    }

    private fun addExtraSideVertex(
        c: Connected,
        d: Direction?,
        to: Connected?,
        cvs: CornerVertices,
        cx: Bounds,
        cy: Bounds,
        out: MutableList<Vertex>,
        trim: BorderTrim
    ) {
        if (to != null) {
            val toBounds = rh.getPlacedPosition(to)
            val tox = rh.getBoundsOf(toBounds, true)
            val toy = rh.getBoundsOf(toBounds, false)
            val toHasCornerVertices = em.hasOuterCornerVertices(to)
            when (d) {
                Direction.UP, Direction.DOWN -> {
                    val yOrd = getOrdForYDirection(d)
                    val cvNew = cvs.createVertex(ONE_HALF, yOrd)
                    val left = if (d === Direction.UP) cvs.getTopLeft() else cvs.getBottomLeft()
                    val right = if (d === Direction.UP) cvs.getTopRight() else cvs.getBottomRight()
                    val leftBounds = rh.getBoundsOf(left.routingInfo, true)
                    val rightBounds = rh.getBoundsOf(right.routingInfo, true)
                    val gap = trim.xe - trim.xs
                    val cVertexBounds: Bounds = BasicBounds(leftBounds.distanceMax + gap, rightBounds.distanceMin - gap)
                    val yBounds = rh.getBoundsOf(left.routingInfo, false)
                    val xBounds = getSideBounds(cx, tox, toHasCornerVertices, borderTrimAreaX, cVertexBounds, gap)
                    setSideVertexRoutingInfo(c, d, out, xBounds, yBounds, cvNew)
                    log.send("Added side vertex: $cvNew")
                }
                Direction.LEFT, Direction.RIGHT -> {
                    val xOrd = getOrdForXDirection(d)
                    val cvNew = cvs.createVertex(xOrd, ONE_HALF)
                    val up = if (d === Direction.LEFT) cvs.getTopLeft() else cvs.getTopRight()
                    val down = if (d === Direction.LEFT) cvs.getBottomLeft() else cvs.getBottomRight()
                    val upBounds = rh.getBoundsOf(up.routingInfo, false)
                    val downBounds = rh.getBoundsOf(down.routingInfo, false)
                    val gap2 = trim.ye - trim.ys
                    val cVertexBounds2: Bounds = BasicBounds(upBounds.distanceMax + gap2, downBounds.distanceMin - gap2)
                    val xBounds2 = rh.getBoundsOf(up.routingInfo, true)
                    val yBounds2 = getSideBounds(cy, toy, toHasCornerVertices, borderTrimAreaY, cVertexBounds2, gap2)
                    setSideVertexRoutingInfo(c, d, out, xBounds2, yBounds2, cvNew)
                    log.send("Added side vertex: $cvNew")
                }
            }
        }
    }

    private fun getSideBounds(
        cBounds: Bounds,
        toDeBounds: Bounds,
        toHasCornerVertices: Boolean,
        trimVertex: Double,
        cVertexBounds: Bounds,
        gap: Double
    ): Bounds {
        return if (toHasCornerVertices) {
            // in this case, we need to find the mid-point of the common area between the two diagram-element bounds.
            val out = cBounds.narrow(toDeBounds)
            val centre = out.distanceCenter
            val radius = gap / 2.0
            BasicBounds(centre - radius, centre + radius)
        } else {
            // in the case that we are dealing with a regular vertex, we need to be the same size as that vertex.
            val toVertexBounds = toDeBounds.narrow(trimVertex)
            cVertexBounds.narrow(toVertexBounds)
        }
    }

    private fun setSideVertexRoutingInfo(
        c: Connected,
        d: Direction,
        out: MutableList<Vertex>,
        xNew: Bounds,
        yNew: Bounds,
        cvNew: MultiCornerVertex
    ) {
        if (cvNew.routingInfo == null) {
            // new vertex				
            cvNew.routingInfo = rh.createRouting(xNew, yNew)
            cvNew.addAnchor(HPos.getFromDirection(d), VPos.getFromDirection(d), c)
            out.add(cvNew)
        } else {
            // extend the existing vertex
            val existing = cvNew.routingInfo!!
            val ri = rh.createRouting(xNew, yNew)
            val merged = rh.increaseBounds(existing, ri)
            cvNew.routingInfo = merged
        }
    }

    class BorderTrim {
        var xs = 0.0
        var ys = 0.0
        var xe = 0.0
        var ye = 0.0
    }

    private fun calculateBorderTrims(c: CornerVertices): BorderTrim {
        val out = BorderTrim()
        val depth = c.getContainerDepth()
        out.xs = borderTrimAreaX - borderTrimAreaX / (depth + 1).toDouble()
        out.xe = borderTrimAreaX - borderTrimAreaX / (depth + 2).toDouble()
        out.ys = borderTrimAreaY - borderTrimAreaY / (depth + 1).toDouble()
        out.ye = borderTrimAreaY - borderTrimAreaY / (depth + 2).toDouble()
        return out
    }

    override fun setPerimeterVertexPositions(
        before: Connected?,
        c: DiagramElement,
        after: Connected?,
        cvs: CornerVertices,
        out: MutableList<Vertex>
    ) {
        val bounds: RoutingInfo
        val bx: Bounds
        val by: Bounds
        val fracMapX: Map<LongFraction, Double>
        val fracMapY: Map<LongFraction, Double>
        if (cvs is SubGridCornerVertices) {
            val container = cvs.getGridContainer()
            bounds = rh.getPlacedPosition(container)!!
            bx = rh.getBoundsOf(bounds, true)
            by = rh.getBoundsOf(bounds, false)
            val (a, b) = fracMapper.getFracMapForGrid(c, rh, cvs.baseGrid, bounds)
            fracMapX = a
            fracMapY = b
        } else {
            bounds = rh.getPlacedPosition(c)!!
            bx = rh.getBoundsOf(bounds, true)
            by = rh.getBoundsOf(bounds, false)
            fracMapX = FracMapper.NULL_FRAC_MAP
            fracMapY = FracMapper.NULL_FRAC_MAP
        }

        // set up frac maps to control where the vertices will be positioned	
        for (cv in cvs.getVerticesAtThisLevel()) {
            setCornerVertexRoutingAndMerge(c, cvs, cv, bx, by, out, fracMapX, fracMapY)
        }
        addSideVertices(before, c, after, cvs, out, bx, by)
    }

    private fun addSideVertices(
        before: Connected?,
        c: DiagramElement,
        after: Connected?,
        cvs: CornerVertices,
        out: MutableList<Vertex>,
        bx: Bounds,
        by: Bounds
    ) {
        val trim = calculateBorderTrims(cvs)
        val l = if (c.getParent() == null) null else (c.getParent() as Container?)!!.getLayout()
        if (c is Connected) {

            // add extra vertices for connections to keep the layout
            if (l != null) {
                when (l) {
                    Layout.UP, Layout.DOWN, Layout.VERTICAL -> {
                        val d1 = if (before != null && cmp(c, before) == 1) Direction.UP else Direction.DOWN
                        val d2 = if (after != null && cmp(c, after) == 1) Direction.UP else Direction.DOWN
                        addExtraSideVertex(c, d1, before, cvs, bx, by, out, trim)
                        addExtraSideVertex(c, d2, after, cvs, bx, by, out, trim)
                    }
                    Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> {
                        val d1 = if (before != null && cmp(c, before) == 1) Direction.LEFT else Direction.RIGHT
                        val d2 = if (after != null && cmp(c, after) == 1) Direction.LEFT else Direction.RIGHT
                        addExtraSideVertex(c, d1, before, cvs, bx, by, out, trim)
                        addExtraSideVertex(c, d2, after, cvs, bx, by, out, trim)
                    }
                    else -> {
                    }
                }
            }

            // add border vertices for directed edges.
            for (conn in c.getLinks()) {
                if (conn.getDrawDirection() != null && !conn.getRenderingInformation().isContradicting) {
                    val d = conn.getDrawDirectionFrom(c)
                    addExtraSideVertex(c, d, conn.otherEnd(c), cvs, bx, by, out, trim)
                }
            }
        }
    }

    override fun setCentralVertexPosition(c: DiagramElement, out: MutableList<Vertex>) {
        var bounds = rh.getPlacedPosition(c)
        log.send("Placed position: $c is $bounds")
        val v = em.getPlanarizationVertex(c)
        out.add(v)
        bounds = rh.narrow(bounds, borderTrimAreaX, borderTrimAreaY)
        v.routingInfo = bounds
    }

    private fun setCornerVertexRoutingAndMerge(
        c: DiagramElement,
        mergeWith: CornerVertices,
        cv: MultiCornerVertex,
        bx: Bounds,
        by: Bounds,
        out: MutableList<Vertex>,
        fracMapX: Map<LongFraction, Double>,
        fracMapY: Map<LongFraction, Double>
    ) {
        var cv: MultiCornerVertex? = cv
        var bx = bx
        var by = by
        if (cv!!.routingInfo == null) {
            val trim = calculateBorderTrims(mergeWith)
            val xOrdinal = cv.xOrdinal
            val yOrdinal = cv.yOrdinal
            val xfrac = fracMapX[xOrdinal]
            val yfrac = fracMapY[yOrdinal]
            bx = bx.keep(trim.xs, trim.xe - trim.xs, xfrac!!)
            by = by.keep(trim.ys, trim.ye - trim.ys, yfrac!!)
            cv.routingInfo = rh.createRouting(bx, by)
            cv = mergeWith.mergeDuplicates(cv, rh)
            if (cv != null) {
                out.add(cv)
                log.send("Setting routing info: $cv $bx $by")
            }
        }
    }

    override val prefix: String
        get() = "VP  "

    override val isLoggingEnabled: Boolean
        get() = true
}