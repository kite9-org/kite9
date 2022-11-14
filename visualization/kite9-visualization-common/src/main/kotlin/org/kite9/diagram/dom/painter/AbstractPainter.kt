package org.kite9.diagram.dom.painter

import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.dom.model.HasSVGRepresentation
import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Base class for painter implementations
 *
 * @author robmoffat
 */
abstract class AbstractPainter : Painter {

	protected var r: DiagramElement? = null

    override fun setDiagramElement(de: DiagramElement) {
        r = de
    }

    abstract fun shortPainterName() : String

    protected fun addInfoAttributes(out: Element) {
        val debug = StringBuilder()

        if (r is Port) {
            val pr = r as Port
            val rri = pr.getRenderingInformation()
            debug.append("port: true; ")
            debug.append("direction: "+lowercase(pr.getPortDirection())+"; ");
            debug.append("pos: " + xy(rri.position) + "; ")
        }

        if (r is SizedRectangular) {
            val sr = r as SizedRectangular
            debug.append(
                "margin: [" + sr.getMargin(Direction.UP) + " " + sr.getMargin(Direction.RIGHT) + " "
                        + sr.getMargin(Direction.DOWN) + " " + sr.getMargin(Direction.LEFT) + "]; "
            )
            debug.append(
                "padding: [" + sr.getPadding(Direction.UP) + " " + sr.getPadding(Direction.RIGHT) + " "
                        + sr.getPadding(Direction.DOWN) + " " + sr.getPadding(Direction.LEFT) + "]; "
            )
            debug.append(
                "min-size: [" + sr.getMinimumSize().width() + " "+sr.getMinimumSize().height()+ "]; "
            )
            debug.append(
                "sizing: [" + lowercase((r as SizedRectangular).getSizing(false)) + ", " +
                        lowercase((r as SizedRectangular).getSizing(true)) + "]; "
            )
        }
        if (r is AlignedRectangular) {
            debug.append("horiz: " + lowercase((r as AlignedRectangular).getHorizontalAlignment()) + "; ")
            debug.append("vert: " + lowercase((r as AlignedRectangular).getVerticalAlignment()) + "; ")
        }
        if (r is Container) {
            val c = r as Container
            debug.append("layout: " + lowercase((r as Container).getLayout()) + "; ")
            if (c.getLayout() === Layout.GRID) {
                val rri = c.getRenderingInformation()
                debug.append("grid-size: [" + rri.gridXSize() + ", " + rri.gridYSize() + "]; ")
                debug.append("cell-xs: [" + commaIntList(rri.cellXPositions) + "]; ")
                debug.append("cell-ys: [" + commaIntList(rri.cellYPositions) + "]; ")
            }
        }
        if (r is Rectangular) {
            val c = r as Rectangular
            val rri = c.getRenderingInformation()
            val usage = getUsage(r as Rectangular)
            debug.append("rectangular: $usage; ")
            debug.append("rect-pos: " + xy(rri.position) + "; ")
            debug.append("rect-size: " + xy(rri.size) + "; ")
            debug.append("position: " + (r as Rectangular).getContainerPosition() + "; ")

            if (c.getParent() is Container) {
                val parent = c.getParent() as Container?
                val prri = parent!!.getRenderingInformation()
                val l = parent.getLayout()
                if (l === Layout.GRID) {
                    val scaledX = scale(rri.gridXPosition(), prri.gridXSize())
                    val scaledY = scale(rri.gridYPosition(), prri.gridYSize())
                    debug.append("grid-x: $scaledX; ")
                    debug.append("grid-y: $scaledY; ")
                }
            }
        }
        if (r is Terminator) {
            val link = (r as Terminator).getConnection()
            val from = link.getDecorationForEnd(link.getFrom()) === r
            val direction = if (link.getDrawDirection() != null)
                if (from) Direction.reverse(link.getDrawDirection()) else link.getDrawDirection()
            else
                (r as Terminator).getArrivalSide()

            debug.append("terminates: " + link.getID() + "; ")
            debug.append("terminates-at: " + (if (from) link.getFrom().getID() else link.getTo().getID()) + "; ")
            debug.append("end: " + if (from) "from; " else "to; ")
            debug.append("direction: "+lowercase(direction)+"; ")
        }
        if (r is Connection) {
            val link = r as Connection
            debug.append("link: ['" + link.getFrom().getID() + "','" + link.getTo().getID() + "']; ")
            debug.append("direction: " + lowercase((r as Connection).getDrawDirection()) + "; ")
            if ((r as Connection).getRenderingInformation().isContradicting) {
                debug.append("contradicting: yes; ")
            }
        }

        if (r is Temporary) {
            debug.append("temporary: true; ")
        }

        if (r is Label) {
            val labelPlacement = (r as Label).getLabelPlacement()
            if (labelPlacement!=null) {
                debug.append("placement: " + lowercase(labelPlacement) + ";")
            }
            val end = (r as Label).getEnd()
            if (end != null) {
                debug.append("end: " + lowercase(end) + ";")
            }
        }

        debug.append("painter: "+this.shortPainterName()+"; ")

        out.setAttribute("k9-info", debug.toString())
    }

    fun lowercase(e: Any?): String {
        return e?.toString()?.lowercase() ?: "null";
    }

    private fun commaIntList(p: DoubleArray?): String {
        if (p == null) {
            return "";
        }
        return p
            .map { it.toInt().toString() }
            .fold("") { a, b -> "$a, $b"}
    }

    private fun scale(p: OPair<LongFraction>?, s: Int): Pair<Int> {
        val a = p!!.a
        val `as` = a.multiply(s)
        val b = p.b
        val bs = b.multiply(s)
        return Pair(`as`.intValue(), bs.intValue())
    }

    private fun xy(d: Dimension2D?) : String {
        return if (d == null) "" else "[" + d.x() + ", "+d.y()+"]"
    }

    private fun getUsage(rect: Rectangular): String {
        return if (rect is Diagram) {
            "diagram"
        } else if (rect is Decal) {
            "decal"
        } else if (rect is ConnectedRectangular) {
            "connected"
        } else if (rect is Label) {
            "label"
        } else if (rect is Terminator) {
            "terminator"
        } else {
            "unknown"
        }
    }

    /**
     * Outputs any SVG-renderable temporary elements to the output.
     */
    protected fun handleTemporaryElements(out: Element, d: Document, postProcessor: XMLProcessor) {
        if (r is Container) {
            (r as Container).getContents()
                .filterIsInstance<Temporary>()
                .filterIsInstance<HasSVGRepresentation>()
                .forEach {
                    val e = it.output(d, postProcessor)
                    if (e != null) {
                        e.setAttribute("k9-elem", "_temporary")
                        e.setAttribute("id", it.getID())
                        out.appendChild(e)
                    }
                }
        }
    }
}