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
import org.kite9.diagram.common.elements.vertex.PortVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
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

    data class LongFractionExtent(val up: LongFraction, val down: LongFraction) {

        fun isEmpty() : Boolean {
            return up == down
        }

        fun contains(f: LongFraction?) : Int {
            if (f==null) {
                return 0
            }
            return if  ((up <= f) && (down >= f)) {
                1
            } else {
                0
            }
        }
    }

    data class FreeBounds(val b: Bounds, val from: LongFractionExtent, val to:LongFractionExtent) {

        fun bisect(o: FreeBounds): List<FreeBounds> {
            return generateOptions(o)
                .filter { !it.from.isEmpty() }
                .filter { !it.to.isEmpty() }
        }

        private fun min(a: LongFraction, b: LongFraction): LongFraction {
            if (a<b) {
                return a
            } else {
                return b
            }
        }

        private fun max(a: LongFraction, b: LongFraction): LongFraction {
            if (a>b) {
                return a
            } else {
                return b
            }
        }

        private fun upper(base: LongFractionExtent, intersect: LongFractionExtent) : LongFractionExtent {
            return LongFractionExtent(base.up, min(base.down, intersect.down))
        }

        private fun lower(base: LongFractionExtent, intersect: LongFractionExtent) : LongFractionExtent {
            return LongFractionExtent(max(base.up, intersect.up), base.down)
        }

        private fun generateOptions(
            o: FreeBounds
        ): List<FreeBounds> {
            if ((this.b.distanceMin < o.b.distanceMin) && (this.b.distanceMax > o.b.distanceMax)) {
                // intersection
                return listOf(
                    FreeBounds(BasicBounds(b.distanceMin, o.b.distanceMin), upper(from, o.from), upper(to, o.to)),
                    FreeBounds(BasicBounds(o.b.distanceMax, b.distanceMax), lower(from, o.from), lower(to, o.to))
                )
            } else if (b.distanceMax < o.b.distanceMin) {
                return listOf(FreeBounds(BasicBounds(b.distanceMin, b.distanceMax), upper(from, o.from), upper(to, o.to)))
            } else if (b.distanceMin > o.b.distanceMax) {
                return listOf(FreeBounds(BasicBounds(b.distanceMin, b.distanceMax), lower(from, o.from), lower(to, o.to)))
            } else if (b.distanceMin < o.b.distanceMin) {
                return listOf(
                    FreeBounds(BasicBounds(b.distanceMin, o.b.distanceMin), upper(from, o.from), upper(to, o.to))
                )
            } else if (b.distanceMax > o.b.distanceMax) {
                return listOf(
                    FreeBounds(BasicBounds(o.b.distanceMax, b.distanceMax), lower(from, o.from), lower(to, o.to))
                )
            } else {
                return emptyList()
            }
        }

    }

    private fun addFacingVertices(
        from: Connected,
        d: Direction,
        to: Connected,
        out: MutableList<Vertex>,
    ) {
        val toHasCornerVertices = em.hasOuterCornerVertices(to)
        val fromHasCornerVertices = em.hasOuterCornerVertices(from)
        val horiz = d == Direction.LEFT || d == Direction.RIGHT

        val toBounds = rh.getPlacedPosition(to)
        val tox = rh.getBoundsOf(toBounds, true)
        val toy = rh.getBoundsOf(toBounds, false)

        val fromBounds = rh.getPlacedPosition(from)
        val fromx = rh.getBoundsOf(fromBounds, true)
        val fromy = rh.getBoundsOf(fromBounds, false)

        // first, figure out the shared area
        val sharedBounds = mutableListOf(
            FreeBounds(
                when (d) {
                    Direction.UP, Direction.DOWN -> fromx.narrow(tox)
                    Direction.LEFT, Direction.RIGHT -> fromy.narrow(toy)
                }
            ,
            LongFractionExtent(LongFraction.ZERO, LongFraction.ONE),
            LongFractionExtent(LongFraction.ZERO, LongFraction.ONE)
        ))

        // now figure out any interfering corner vertices
        val interferingBounds =
            (if (fromHasCornerVertices) em.getOuterCornerVertices(from).getVerticesOnSide(d).map {
                val bounds = rh.getBoundsOf(it.routingInfo!!, !horiz)
                val frac = if (horiz) {
                    it.yOrdinal
                } else {
                    it.xOrdinal
                }
                FreeBounds(bounds, LongFractionExtent(frac, frac), LongFractionExtent(LongFraction.ZERO, LongFraction.ONE))
            } else emptyList()) + (if (toHasCornerVertices) em.getOuterCornerVertices(to).getVerticesOnSide(Direction.reverse(d)!!).map {
                val bounds = rh.getBoundsOf(it.routingInfo!!, !horiz)
                val frac = if (horiz) {
                    it.yOrdinal
                } else {
                    it.xOrdinal
                }
                FreeBounds(bounds, LongFractionExtent(LongFraction.ZERO, LongFraction.ONE), LongFractionExtent(frac, frac))
            } else emptyList())


        // bisect the bounds, removing areas that are already blocked by vertices
        var openBounds: List<FreeBounds> = sharedBounds
        interferingBounds.forEach { ib ->
            openBounds = openBounds.flatMap { it.bisect(ib) }
        }

        // find the one closest to our fraction
        val fromFraction = if (from is Port) { null } else { em.getFractions(from, d)[from]!! }
        val toFraction = if (to is Port) { null } else { em.getFractions(to, Direction.reverse(d)!!)[to]!! }
        var largestFreeBounds =
            openBounds.maxByOrNull { it.from.contains(fromFraction) + it.to.contains(toFraction) + it.b.size() }!!

        if (largestFreeBounds == null) {
            throw LogicException("Couldn't find place for a vertex between $from and $to")
        }

        addSideVertex(fromHasCornerVertices, d, from, horiz, largestFreeBounds.b, largestFreeBounds.from, out)
        addSideVertex(toHasCornerVertices, Direction.reverse(d)!!, to, horiz, largestFreeBounds.b, largestFreeBounds.to, out)

    }

    private fun addSideVertex(
        hasCornerVertices: Boolean,
        d: Direction,
        c: Connected,
        horiz: Boolean,
        bounds: Bounds,
        extent: LongFractionExtent,
        out: MutableList<Vertex>
    ) {
        // now, construct a vertex on each connected using these bounds
        if (hasCornerVertices) {
            val cvs = em.getOuterCornerVertices(c)
            val otherBounds = rh.getBoundsOf(rh.getPlacedPosition(c), horiz)

            if (horiz) {
                // mid-point fraction
                val yOrd = (extent.up.add(extent.down)).multiply(LongFraction.ONE_HALF)
                val xOrd = getOrdForXDirection(d)
                val cvNew = cvs.createVertex(xOrd, yOrd, HPos.getFromDirection(d), VPos.getFromDirection(d), c, null)
                setCornerVertexRoutingAndMerge(c, cvs, cvNew, otherBounds, bounds, out, mapOf(xOrd to xOrd.doubleValue()), mapOf(yOrd to yOrd.doubleValue()))
                log.send("Added side vertex: $cvNew")
            } else {
                // mid-point fraction
                val xOrd = (extent.up.add(extent.down)).multiply(LongFraction.ONE_HALF)
                val yOrd = getOrdForYDirection(d)
                val cvNew = cvs.createVertex(xOrd, yOrd, HPos.getFromDirection(d), VPos.getFromDirection(d), c, null)
                setCornerVertexRoutingAndMerge(c, cvs, cvNew, bounds, otherBounds, out, mapOf(xOrd to xOrd.doubleValue()), mapOf(yOrd to yOrd.doubleValue()))
                log.send("Added side vertex: $cvNew")
            }
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
        cvs: CornerVertices,
        out: MutableList<Vertex>
    ) {
        val bounds: RoutingInfo
        val bx: Bounds
        val by: Bounds
        var fracMapX: Map<LongFraction, Double>
        var fracMapY: Map<LongFraction, Double>
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
            val xm = FracMapperImpl.createNullFracMap()
            val ym = FracMapperImpl.createNullFracMap()

            cvs.getVerticesAtThisLevel()
                .filterIsInstance<PortVertex>()
                .forEach {
                    xm.put( it.xOrdinal, it.xOrdinal.doubleValue())
                    ym.put(it.yOrdinal, it.yOrdinal.doubleValue())
                }
            
            fracMapX = xm
            fracMapY = ym
        }

        // set up frac maps to control where the vertices will be positioned	
        for (cv in cvs.getVerticesAtThisLevel()) {
            setCornerVertexRoutingAndMerge(c, cvs, cv, bx, by, out, fracMapX, fracMapY)
        }

        addSideVertices(before, c, out)
    }

    private fun addSideVertices(
        before: Connected?,
        c: DiagramElement,
        out: MutableList<Vertex>,
    ) {
        val l = if (c.getParent() == null) null else (c.getParent() as Container?)!!.getLayout()
        if (c is Connected) {

            // add extra vertices for connections to keep the layout
            if (l != null) {
                val d = when (l) {
                    Layout.UP, Layout.DOWN, Layout.VERTICAL -> Direction.DOWN
                    Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> Direction.RIGHT
                    else -> return
                }
                if (before != null) {
                    if (cmp(c, before) == 1) {
                        addFacingVertices(before, d, c, out)
                    } else {
                        addFacingVertices(c, d, before, out)
                    }
                }
            }
        }
    }

    override fun setFacingVerticesForStraightEdges(conn: Connection, out: MutableList<Vertex>) {
        // add border vertices for directed edges.
        val drawDirection = conn.getDrawDirection()
        if (drawDirection != null && !conn.getRenderingInformation().isContradicting) {
            addFacingVertices(conn.getFrom(), drawDirection, conn.getTo(), out)
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