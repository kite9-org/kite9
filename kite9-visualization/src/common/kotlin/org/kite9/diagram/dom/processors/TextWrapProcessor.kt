package org.kite9.diagram.dom.processors

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.ns.Kite9Namespaces
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.style.DiagramElementType
import org.kite9.diagram.model.style.TextAlign
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import kotlin.math.max

/**
 * The text-wrap processor examines <text> elements in the SVG, and reformats them as a
 * set of <tspan> elements.
 *
 */
class TextWrapProcessor(val ctx: ElementContext) : AbstractInlineProcessor(), Logable {

    private val LOG: Kite9Log = Kite9Log.instance(this)

    private val PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toCharArray().toSet()

    private fun replaceZero(v: Double): Double {
        if (v == 0.0) {
            return 10000.0
        } else {
            return v
        }
    }

    override fun processTag(n: Element): Element {
        if (isWrapContents(n)) {
            // parameters to use
            val width = ctx.getCssStyleDoubleProperty(CSSConstants.TEXT_BOUNDS_WIDTH, n)
            val height = ctx.getCssStyleDoubleProperty(CSSConstants.TEXT_BOUNDS_HEIGHT, n)
            val align = toAlignEnum(ctx.getCssStyleStringProperty(CSSConstants.TEXT_ALIGN, n))

            // build the layout
            val spans = splitIntoSpans(n)
            val lines = buildLines(spans, replaceZero(width))
            val simplifiedLines = simplifyLines(lines)

            if (!LOG.go()) {
                val text = simplifiedLines.map { it.toString() + "\n" }
                LOG.send("Wrapping: \n " + text + "\n   " + width + " " + height + " " + align)
            }

            // replace original svg
            removeAllContent(n)
            val maxLineWidth = lines.maxOfOrNull { it.sumOf { it.width } } ?: 0.0
            replaceContents(n.ownerDocument!!, simplifiedLines, align, maxLineWidth, replaceZero(height), "text")

            return n
        } else {
            return super.processTag(n)
        }
    }

    /**
     * See: https://developer.mozilla.org/en-US/docs/Web/CSS/text-align
     * Doesn't really apply to SVG per-se, but we'll make it work.
     */
    private fun toAlignEnum(s: String?): TextAlign {
        return when (s) {
            "start" -> TextAlign.START
            "left" -> TextAlign.START
            "middle" -> TextAlign.MIDDLE  // "middle" is something batik introduced I think
            "center" -> TextAlign.MIDDLE
            "end" -> TextAlign.END
            "right" -> TextAlign.END
            "full" -> TextAlign.FULL
            "justify" -> TextAlign.FULL
            else -> TextAlign.START
        }
    }

    companion object {

        fun calculateLineHeight(n: Element, ctx: ElementContext): Double {
            val fontSize = ctx.getCssStyleDoubleProperty("font-size", n)
            var lineHeight = ctx.getCssStyleDoubleProperty("line-height", n)
            if (lineHeight == 0.0) {
                lineHeight = 1.2 * fontSize
            }
            return lineHeight
        }
    }

    fun removeAllContent(e: Element) {
        val childNodes: NodeList = e.childNodes
        var kept = 0
        while (childNodes.length > kept) {
            val item = childNodes.item(kept)!!
            if (isTextNode(item) || isImageNode(item)) {
                e.removeChild(item)
            } else {
                if (item is Element) {
                    removeAllContent(item)
                }
                kept++
            }
        }
    }

    fun replaceContents(
        od: Document,
        lines: List<List<Span>>,
        align: TextAlign,
        maxLineWidth: Double,
        maxHeight: Double,
        tagName: String
    ) {

        for ((lineNumber, t) in lines.withIndex()) {
            val sumOfWidths = t.sumOf { it.width }

            var sx = when (align) {
                TextAlign.END -> maxLineWidth - sumOfWidths
                TextAlign.MIDDLE -> (maxLineWidth - sumOfWidths) / 2.0
                else -> 0.0
            }

            val lineHeight = t.maxOfOrNull { it.height } ?: 0.0

            t.forEach {
                val cspan = if (it is StringSpan) {
                    val tag = od.createElementNS(Kite9Namespaces.SVG_NAMESPACE, tagName)
                    val text = od.createTextNode(it.s)
                    tag.appendChild(text)
                    tag.setAttribute("y", "" + (lineHeight * lineNumber) + "px")
                    tag
                } else {
                    val tag = (it as ImageSpan).e
                    // make the image sit on the line
                    tag.setAttribute("y", "" + ((lineHeight * lineNumber) - it.height) + "px")
                    tag
                }

                cspan.setAttribute("x", "" + sx + "px")

                if (lineHeight * (lineNumber + 1) > maxHeight) {
                    cspan.setAttribute("display", "none")
                }

                it.parent.appendChild(cspan)

                sx += when (align) {
                    TextAlign.FULL -> it.width + sumOfWidths / t.size
                    else -> it.width
                }
            }

        }
    }

    enum class SpanType { SPACE, PUNCTUATION, WORD_PART, NEW_LINE }

    interface Span {

        val width: Double
        val height: Double
        val parent: Element

    }

    data class StringSpan(
        val s: String,
        val type: SpanType,
        override val width: Double,
        override val height: Double,
        override val parent: Element
    ) : Span {
        override fun toString(): String {
            return "StringSpan(s='$s', type=$type, width=$width)"
        }
    }

    data class ImageSpan(
        val e: Element,
        override val width: Double,
        override val height: Double,
        override val parent: Element
    ) : Span {
        override fun toString(): String {
            return "ImageSpan()"
        }
    }

    fun splitIntoSpans(n: Node): List<Span> {
        return if (isTextNode(n)) {
            val text = n.textContent ?: ""
            splitIntoSpans(text, n as Element)
        } else if (isImageNode(n)) {
            val bounds = ctx.bounds(n as Element)
            return listOf(
                ImageSpan(
                    n,
                    bounds?.width ?: 0.0,
                    bounds?.height ?: 0.0, getNonTextParent(n)
                )
            )
        } else {
            val list = n.childNodes
            val out = mutableListOf<Span>()
            for (i in 0..list.length - 1) {
                val ret = splitIntoSpans(list.item(i)!!)
                out.addAll(ret)
            }
            out
        }
    }

    private fun getNonTextParent(n: Node): Element {
        var n = n
        while (isTextNode(n) || isImageNode(n)) {
            n = n.parentNode!!
        }

        return n as Element
    }

    private fun isTextNode(n: Node) =
        (n is Element) && ((n.localName == "text") || (n.localName == "cspan")) && (n.namespaceURI == Kite9Namespaces.SVG_NAMESPACE)

    private fun isImageNode(n: Node) =
        (n is Element) && (n.localName == "image") && (n.namespaceURI == Kite9Namespaces.SVG_NAMESPACE)

    private fun isWrapContents(n: Element) =
        ElementContext.getCssStyleEnumProperty<DiagramElementType>(
            CSSConstants.ELEMENT_TYPE_PROPERTY,
            n,
            ctx
        ) == DiagramElementType.TEXT


    fun splitIntoSpans(s: String, inside: Element): List<Span> {
        val out = mutableListOf<Span>()
        var last = SpanType.WORD_PART
        val buf = StringBuilder()

        fun breakBuffer() {
            if (buf.isNotEmpty()) {
                val width = ctx.textWidth(buf.toString(), inside)
                val lineHeight = calculateLineHeight(inside, ctx)
                out.add(StringSpan(buf.toString(), last, width, lineHeight, getNonTextParent(inside)))
                buf.clear()
            }
        }

        fun consume(c: Char, current: SpanType) {
            when (last) {
                SpanType.SPACE -> {
                    when (current) {
                        SpanType.PUNCTUATION, SpanType.WORD_PART, SpanType.NEW_LINE -> breakBuffer()
                        else -> {}
                    }
                }

                SpanType.PUNCTUATION -> {
                    when (current) {
                        SpanType.PUNCTUATION, SpanType.SPACE, SpanType.NEW_LINE -> breakBuffer()
                        else -> {}
                    }
                }

                SpanType.WORD_PART -> {
                    when (current) {
                        SpanType.SPACE, SpanType.NEW_LINE -> breakBuffer()
                        else -> {}
                    }
                }

                SpanType.NEW_LINE -> breakBuffer()
            }

            last = current
            buf.append(c)
        }


        for (c in s) {
            when {
                '\n' == c -> consume(c, SpanType.NEW_LINE)
                c.isWhitespace() -> consume(c, SpanType.SPACE)
                PUNCTUATION.contains(c) -> consume(c, SpanType.PUNCTUATION)
                else -> consume(c, SpanType.WORD_PART)
            }
        }

        if (buf.isNotEmpty()) {
            breakBuffer()
        }

        return out
    }


    fun buildLines(spans: List<Span>, width: Double): List<List<Span>> {
        val out = mutableListOf<List<Span>>()
        var line = 0
        var currentSpan = mutableListOf<Span>()
        var currentLineLength = 0.0
        var currentParent: Element? = null

        fun newLine(span: Span) {
            if (currentLineLength > 0) {
                line++
                currentSpan.add(span)
                currentParent = span.parent
                out.add(currentSpan)
                currentSpan = mutableListOf()
                currentLineLength = 0.0
            }
        }

        fun parentChange(span: Span): Boolean {
            return (currentParent != null) && (span.parent != currentParent)
        }

        fun continueLine(span: Span, newWidth: Double, isSpace: Boolean) {
            if (!isSpace) {
                if ((currentLineLength + newWidth > width) || parentChange(span)) {
                    // need to wrap before adding
                    newLine(StringSpan("", SpanType.SPACE, 0.0, 0.0, span.parent))
                }
            }

            currentParent = span.parent
            currentSpan.add(span)
            currentLineLength += newWidth
        }

        for (s in spans) {
            if (s is ImageSpan) {
                continueLine(s, s.width, false)
            } else if (s is StringSpan) {
                val spanType = s.type
                val isSpace = spanType == SpanType.SPACE
                if (spanType == SpanType.NEW_LINE) {
                    newLine(s)
                } else if ((currentLineLength == 0.0) && isSpace) {
                    continueLine(s, 0.0, isSpace)
                } else if (isSpace) {
                    // in xml/svg, multiple spaces are reduced to one
                    continueLine(s, s.width, true)
                } else {
                    continueLine(s, s.width, false)
                }
            }
        }

        if (currentParent != null) {
            newLine(StringSpan("", SpanType.SPACE, 0.0, 0.0, currentParent!!))
        }

        return out
    }

    fun simplifyLines(lines: List<List<Span>>): List<List<Span>> {
        return lines.map { mergeSpans(it) }
    }

    fun mergeSpans(spans: List<Span>): List<Span> {
        val out = mutableListOf<Span>()
        spans.forEach {
            if ((out.lastOrNull() is StringSpan) && (it is StringSpan)) {
                val last = out.removeLast() as StringSpan
                val newLast = StringSpan(
                    last.s + it.s,
                    SpanType.WORD_PART,
                    last.width + it.width,
                    max(last.height, it.height),
                    last.parent
                )
                out.add(newLast)
            } else {
                out.add(it)
            }
        }
        return out
    }

    override val prefix: String?
        get() = "TEXT"

    override val isLoggingEnabled = true
}