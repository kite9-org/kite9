package org.kite9.diagram.common.elements.grid

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.common.elements.mapping.CornerVertices
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.common.fraction.LongFraction.Companion.getReducedFraction
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.common.range.BasicIntegerRange
import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.common.range.IntegerRange.Companion.notSet
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.Table
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.GridContainerPosition

/**
 * Tools for helping create Grid structure.
 *
 * @author robmoffat
 */
class GridPositionerImpl(private val factory: DiagramElementFactory<*>) : GridPositioner, Logable {

    private val log = Kite9Log.instance(this)

    var placed: MutableMap<Container, Array<Array<DiagramElement>>> = HashMap()

    override fun placeOnGrid(ord: Container, allowSpanning: Boolean): Array<Array<DiagramElement>> {
        if (placed.containsKey(ord)) {
            return placed[ord]!!
        }

        val overlaps: MutableList<DiagramElement> = ArrayList()
        val out: MutableMap<Int, MutableMap<Int, DiagramElement>> = HashMap()
        val xOrdinals: MutableSet<Int> = mutableSetOf()
        val yOrdinals: MutableSet<Int> = mutableSetOf()

        // place elements in their correct positions, as far as possible.  
        // move overlaps to array.
        for (diagramElement in ord.getContents()) {
            if (shoudAddToGrid(diagramElement)) {
                val xpos = getXOccupies(diagramElement as Rectangular)
                val ypos = getYOccupies(diagramElement)
                if (!notSet(xpos) && !notSet(ypos) && ensureGrid(out, xpos, ypos, null, xOrdinals, yOrdinals) == null) {
                    ensureGrid(out, xpos, ypos, diagramElement, xOrdinals, yOrdinals)
                } else {
                    overlaps.add(diagramElement)
                }
            }
        }

        // add remaining/dummy elements elements, by adding extra rows if need be.
        var cell = 0
        if (overlaps.size > 0) {
            padOrdinal(xOrdinals, 1.coerceAtLeast(ord.getGridColumns()))
            padOrdinal(yOrdinals, 1.coerceAtLeast(ord.getGridRows()))
        }
        var xSize = xOrdinals.size
        var ySize = yOrdinals.size
        while (overlaps.size > 0) {
            val row = Math.floorDiv(cell, xSize)
            val col = cell % xSize
            val yOrder = yOrdinals.toMutableList().also { it.sort() }
            val xOrder = xOrdinals.toMutableList().also { it.sort() }
            if (row >= yOrder.size) {
                ySize++
                val max : Int = yOrder.reduceOrNull { a: Int, b: Int -> a.coerceAtLeast(b) } ?: 0
                yOrder.add(max + 1)
            }
            val co = xOrder[col]
            val ro = yOrder[row]
            val xpos = BasicIntegerRange(co, co)
            val ypos = BasicIntegerRange(ro, ro)
            val d = ensureGrid(out, xpos, ypos, null, xOrdinals, yOrdinals)
            if (d == null) {
                if (!overlaps.isEmpty()) {
                    ensureGrid(out, xpos, ypos, overlaps.removeAt(0), xOrdinals, yOrdinals)
                }
            }
            cell++
        }

        ySize = removeDuplicatesAndEmptyRows(out, ySize, yOrdinals, xOrdinals)
        xSize = removeDuplicatesAndEmptyCols(out, xSize, yOrdinals, xOrdinals)

        // to array
        val yOrder = yOrdinals.toMutableList().also { it.sort() }
        val xOrder = xOrdinals.toMutableList().also { it.sort() }
        fillInTheBlanks(out, ord, xOrder, yOrder)

        val done: Array<Array<DiagramElement>> = yOrder
            .map { y: Int ->
                xOrder
                    .map { x: Int -> out[y]!![x]!! }
                    .toTypedArray()
            }
            .toTypedArray()

        val size = OPair(xSize, ySize)
        scaleCoordinates(done, size)
        if (isLoggingEnabled) {
            val t = Table()
            for (diagramElements in done) {
                t.addObjectRow(diagramElements)
            }
            log.send("Grid Positions: \n", t)
        }
        val crri = ord.getRenderingInformation()
        crri.setGridXSize(size.a)
        crri.setGridYSize(size.b)
        placed[ord] = done
        return done
    }

    private fun padOrdinal(ordinals: MutableSet<Int>, s: Int) {
        var max = if (ordinals.size == 0) 0 else ordinals.max()
        while (ordinals.size < s) {
            max = if (max == null) 0 else max + 1
            ordinals.add(max)
        }
    }

    private fun fillInTheBlanks(
        contents: MutableMap<Int, MutableMap<Int, DiagramElement>>,
        ord: Container,
        xs: List<Int>,
        ys: List<Int>
    ) {
        for (y in ys) {
            for (x in xs) {
                var xMap = contents[y]

                if (xMap == null) {
                    xMap = mutableMapOf()
                    contents[y] = xMap
                }

                var toPlace = xMap[x]

                if (toPlace == null) {
                    toPlace = factory.createTemporaryConnected(ord, "$x-$y")
                    toPlace.setContainerPosition(
                        GridContainerPosition(BasicIntegerRange(x, x), BasicIntegerRange(y, y))
                    )
                    modifyContainerContents(ord, toPlace)
                    xMap[x] = toPlace
                }
            }
        }
    }

    private fun removeDuplicatesAndEmptyRows(
        out: Map<Int, MutableMap<Int, DiagramElement>>,
        height: Int,
        yOrdinals: MutableSet<Int>,
        xOrdinals: MutableSet<Int>
    ): Int {
        var height = height
        var last: List<DiagramElement?>? = null
        val yIt = yOrdinals.iterator()
        while (yIt.hasNext()) {
            val y = yIt.next()
            val line: List<DiagramElement?> = xOrdinals
                .map { x: Int ->
                    val row: Map<Int, DiagramElement>? = out[y]
                    row?.get(x)
                }
            if (last != null && last == line) {
                yIt.remove()
                height--
            } else if (line.filterNotNull().count() == 0) {
                yIt.remove()
                height--
            }
            last = line
        }
        return height
    }

    private fun removeDuplicatesAndEmptyCols(
        out: Map<Int, MutableMap<Int, DiagramElement>>,
        width: Int,
        yOrdinals: MutableSet<Int>,
        xOrdinals: MutableSet<Int>
    ): Int {
        var width = width
        var last: List<DiagramElement?>? = null
        val xIt = xOrdinals.iterator()
        while (xIt.hasNext()) {
            val x = xIt.next()
            val line: List<DiagramElement?> = yOrdinals
                .map { y: Int ->
                    val row: Map<Int, DiagramElement>? = out[y]
                    row?.get(x)
                }
            var remove = false
            if (last != null && last == line) {
                remove = true
            }
            if (line.filterNotNull().count() == 0) {
                remove = true
            }
            if (remove) {
                xIt.remove()
                width--
            }
            last = line
        }
        return width
    }

    protected fun init(l: Int): List<DiagramElement?> {
        val ys: MutableList<DiagramElement?> = ArrayList(l)
        for (y in 0 until l) {
            ys.add(null)
        }
        return ys
    }

    private fun shoudAddToGrid(diagramElement: DiagramElement): Boolean {
        return diagramElement is Connected
    }

    /**
     * Deprecated, because we wanted to have immutable containers.
     */
    @Deprecated("")
    private fun modifyContainerContents(ord: Container, d: DiagramElement) {
        ord.getContents().add(d)
    }

    private fun scaleCoordinates(grid: Array<Array<DiagramElement>>, size: OPair<Int>) {
        val xp: MutableMap<DiagramElement, OPair<Int>> = HashMap()
        val yp: MutableMap<DiagramElement, OPair<Int>> = HashMap()
        for (y in grid.indices) {
            for (x in grid[y].indices) {
                val de = grid[y][x]

                // setup x range
                var xr = if (xp.containsKey(de)) xp[de]!! else OPair(Int.MAX_VALUE, Int.MIN_VALUE)
                xr = OPair(x.coerceAtMost(xr.a), (x + 1).coerceAtLeast(xr.b))
                xp[de] = xr

                // setup y range
                var yr = if (yp.containsKey(de)) yp[de]!! else OPair(Int.MAX_VALUE, Int.MIN_VALUE)
                yr = OPair(y.coerceAtMost(yr.a), (y + 1).coerceAtLeast(yr.b))
                yp[de] = yr
            }
        }
        for (de in xp.keys) {
            val ri = de.getRenderingInformation() as RectangleRenderingInformation
            val (a, b) = xp[de]!!
            val (a1, b1) = yp[de]!!
            val xin = OPair(
                getReducedFraction(a, size.a), getReducedFraction(
                    b, size.a
                )
            )
            val yin = OPair(
                getReducedFraction(a1, size.b), getReducedFraction(
                    b1, size.b
                )
            )
            ri.setGridXPosition(xin)
            ri.setGridYPosition(yin)
        }
    }

    override fun getClockwiseOrderedContainerVertices(cvs: CornerVertices): List<MultiCornerVertex> {
        var minx: LongFraction? = null
        var maxx: LongFraction? = null
        var miny: LongFraction? = null
        var maxy: LongFraction? = null
        cvs.identifyPerimeterVertices()
        val perimeterVertices = cvs.getPerimeterVertices()
        for (cv in perimeterVertices) {
            val xb = cv.xOrdinal
            val yb = cv.yOrdinal
            minx = limit(minx, xb, -1)
            miny = limit(miny, yb, -1)
            maxx = limit(maxx, xb, 1)
            maxy = limit(maxy, yb, 1)
        }
        val top = sort(+1, 0, collect(minx, maxx, miny, miny, perimeterVertices))
        val right = sort(0, +1, collect(maxx, maxx, miny, maxy, perimeterVertices))
        val bottom = sort(-1, 0, collect(minx, maxx, maxy, maxy, perimeterVertices))
        val left = sort(0, -1, collect(minx, minx, miny, maxy, perimeterVertices))
        val plist: MutableList<MultiCornerVertex> = ArrayList(top.size + right.size + left.size + bottom.size)
        addAllExceptLast(plist, top)
        addAllExceptLast(plist, right)
        addAllExceptLast(plist, bottom)
        addAllExceptLast(plist, left)
        return plist
    }

    private fun limit(current: LongFraction?, `in`: LongFraction, compare: Int): LongFraction? {
        var current = current
        if (current == null || `in`.compareTo(current) == compare) {
            current = `in`
        }
        return current
    }

    private fun sort(xorder: Int, yorder: Int, collect: List<MultiCornerVertex>): List<MultiCornerVertex> {
        val sorted = collect.sortedWith { o1, o2 ->
            val ys = o1.yOrdinal.compareTo(o2.yOrdinal) * yorder
            val xs = o1.xOrdinal.compareTo(o2.xOrdinal) * xorder
            xs + ys
        }
        return sorted
    }

    /*
	 * Prevents duplicating the corner vertices
	 */
    private fun addAllExceptLast(out: MutableList<MultiCornerVertex>, `in`: List<MultiCornerVertex>) {
        for (i in 0 until `in`.size - 1) {
            out.add(`in`[i])
        }
    }

    private fun collect(
        minx: LongFraction?,
        maxx: LongFraction?,
        miny: LongFraction?,
        maxy: LongFraction?,
        elements: Collection<MultiCornerVertex>
    ): List<MultiCornerVertex> {
        val out: MutableList<MultiCornerVertex> = ArrayList()
        for (cv in elements) {
            val xb = cv.xOrdinal
            val yb = cv.yOrdinal
            if (minx!!.compareTo(xb) != 1 && maxx!!.compareTo(xb) != -1
                && miny!!.compareTo(yb) != 1 && maxy!!.compareTo(yb) != -1
            ) {
                out.add(cv)
            }
        }
        return out
    }

    override val prefix: String
        get() = "GP  "
    override val isLoggingEnabled: Boolean
        get() = true


    companion object {

		fun getYOccupies(diagramElement: Rectangular): IntegerRange {
            return (diagramElement.getContainerPosition() as GridContainerPosition?)!!.y
        }

		fun getXOccupies(diagramElement: Rectangular): IntegerRange {
            return (diagramElement.getContainerPosition() as GridContainerPosition?)!!.x
        }

        /**
         * Iterates over the grid squares occupied by the ranges and either checks that they are empty,
         * or sets their value.
         */
        private fun ensureGrid(
            out: MutableMap<Int, MutableMap<Int, DiagramElement>>,
            xpos: IntegerRange,
            ypos: IntegerRange,
            `in`: DiagramElement?,
            xOrdinals: MutableSet<Int>,
            yOrdinals: MutableSet<Int>
        ): DiagramElement? {
            for (x in xpos.from..xpos.to) {
                xOrdinals.add(x)
            }
            for (y in ypos.from..ypos.to) {
                yOrdinals.add(y)
            }

            // check that the area to place in is empty
            var filled: DiagramElement? = null
            for (x in xpos.from..xpos.to) {
                for (y in ypos.from..ypos.to) {
                    val row: Map<Int, DiagramElement>? = out[y]
                    val f = row?.get(x)
                    filled = filled ?: f
                }
            }
            if (filled != null) {
                return filled
            }
            return if (`in` != null) {
                // place the element
                for (x in xpos.from..xpos.to) {
                    for (y in ypos.from..ypos.to) {
                        var row = out[y]
                        if (row == null) {
                            row = HashMap()
                            out[y] = row
                        }
                        row[x] = `in`
                    }
                }
                `in`
            } else {
                null
            }
        }
    }
}