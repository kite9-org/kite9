package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.PortVertex
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.common.fraction.LongFraction.Companion.ONE
import org.kite9.diagram.common.fraction.LongFraction.Companion.ZERO
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Port
import org.kite9.diagram.model.position.HPos
import org.kite9.diagram.model.position.VPos
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

abstract class AbstractCornerVertices(
    val rootContainer: DiagramElement,
    val xRange: OPair<LongFraction>,
    val yRange: OPair<LongFraction>,
    private val depth: Int
) : CornerVertices {

    val children: MutableCollection<CornerVertices> = ArrayList(5)
    private var tl: MultiCornerVertex? = null
    private var tr: MultiCornerVertex? = null
    private var bl: MultiCornerVertex? = null
    private var br: MultiCornerVertex? = null

    protected fun createInitialVertices(c: DiagramElement?) {
        tl = createVertex(ZERO, ZERO, null)
        tr = createVertex(ONE, ZERO, null)
        bl = createVertex(ZERO, ONE, null)
        br = createVertex(ONE, ONE, null)
        if (c != null) {
            tl!!.addAnchor(HPos.LEFT, VPos.UP, c)
            tr!!.addAnchor(HPos.RIGHT, VPos.UP, c)
            bl!!.addAnchor(HPos.LEFT, VPos.DOWN, c)
            br!!.addAnchor(HPos.RIGHT, VPos.DOWN, c)
        }
    }

    abstract override fun createVertex(x: LongFraction, y: LongFraction, p: Port?): MultiCornerVertex

    protected fun createVertexHere(
        x: LongFraction,
        y: LongFraction,
        elements: MutableMap<OPair<LongFraction>, MultiCornerVertex>,
        p: Port?
    ): MultiCornerVertex {
        val d = OPair(x, y)
        var cv = elements[d]
        if (cv == null) {
            if (p == null) {
                cv = MultiCornerVertex(getVertexIDStem(), x, y)
            } else {
                cv = PortVertex(getVertexIDStem()+"-"+p.getID(), x, y, p)
            }
            elements[d] = cv
        }
        return cv
    }

    protected open fun getVertexIDStem(): String {
        return rootContainer.getID()
    }

    private var perimeterVertices: Collection<MultiCornerVertex> = emptySet()

    override fun getPerimeterVertices(): Collection<MultiCornerVertex> {
        return perimeterVertices
    }

    override fun identifyPerimeterVertices() {
        val minx = tl!!.xOrdinal
        val maxx = br!!.xOrdinal
        val miny = tl!!.yOrdinal
        val maxy = br!!.yOrdinal
        val pset = HashSet<MultiCornerVertex>(10)
        collect(minx, maxx, miny, miny, pset)
        collect(maxx, maxx, miny, maxy, pset)
        collect(minx, maxx, maxy, maxy, pset)
        collect(minx, minx, miny, maxy, pset)
        perimeterVertices = pset
    }

    private fun afterEq(`in`: LongFraction?, with: LongFraction): Boolean {
        if (`in` == null) {
            return true
        }
        val c = `in`.compareTo(with)
        return c > -1
    }

    private fun beforeEq(`in`: LongFraction?, with: LongFraction): Boolean {
        if (`in` == null) {
            return true
        }
        val c = `in`.compareTo(with)
        return c < 1
    }

    private fun collect(
        minx: LongFraction,
        maxx: LongFraction,
        miny: LongFraction,
        maxy: LongFraction,
        out: MutableCollection<MultiCornerVertex>
    ) {
        for (cv in getTopContainerVertices().getAllDescendentVertices()) {
            val x = cv.xOrdinal
            val y = cv.yOrdinal
            if (afterEq(x, minx) && beforeEq(x, maxx) && afterEq(y, miny) && beforeEq(y, maxy)) {
                out.add(cv)
            }
        }
    }

    override fun getAllDescendentVertices(): MutableCollection<MultiCornerVertex> {
        val out: MutableCollection<MultiCornerVertex> = ArrayList()
        for (child in children) {
            out.addAll(child.getAllDescendentVertices())
        }
        return out
    }

    abstract fun getTopContainerVertices(): AbstractCornerVertices
    fun findOverlappingVertex(cv: MultiCornerVertex, rh: RoutableHandler2D): MultiCornerVertex? {
        val cvRoutingInfo = cv.routingInfo!!
        for (cv2 in getAllDescendentVertices()) {
            if (cv2 != cv) {
                val cv2routingInfo = cv2.routingInfo
                if (cv2routingInfo != null) {
                    if (rh.overlaps(cvRoutingInfo, cv2routingInfo)) {
                        return cv2
                    }
                }
            }
        }
        return null
    }

    override fun getTopLeft(): MultiCornerVertex {
        return tl!!
    }

    override fun getTopRight(): MultiCornerVertex {
        return tr!!
    }

    override fun getBottomLeft(): MultiCornerVertex {
        return bl!!
    }

    override fun getBottomRight(): MultiCornerVertex {
        return br!!
    }

    override fun getContainerDepth(): Int {
        return depth
    }

    companion object {
        fun scale(yy: LongFraction, range: OPair<LongFraction>): LongFraction {
            var y = yy
            val size = range.b.subtract(range.a)
            y = y.multiply(size)
            y = y.add(range.a)
            return y
        }
    }
}