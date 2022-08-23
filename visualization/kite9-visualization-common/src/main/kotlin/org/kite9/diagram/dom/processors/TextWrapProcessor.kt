package org.kite9.diagram.dom.processors

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.ns.Kite9Namespaces
import org.kite9.diagram.model.style.DiagramElementType
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import kotlin.math.max

/**
 * The text-wrap processor examines <text> elements in the SVG, and reformats them as a
 * set of <tspan> elements.
 *
 */
class TextWrapProcessor(val ctx: ElementContext) : AbstractInlineProcessor() {

    private val SPECIAL_WORD_CHARS = "\"\'\u2018\u2019\u201C\u201D?./!,;:_".toCharArray().toSet()

    private val PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toCharArray().toSet()

    private fun replaceZero(v: Double) : Double {
        if (v == 0.0) {
            return 10000.0;
        } else {
            return v;
        }
    }

    override fun processTag(n: Element): Element {
        if (isWrapContents(n)) {
            // parameters to use
            val width = ctx.getCssStyleDoubleProperty(CSSConstants.TEXT_BOUNDS_WIDTH, n)
            val height = ctx.getCssStyleDoubleProperty(CSSConstants.TEXT_BOUNDS_HEIGHT, n)
            val align = ctx.getCssStyleStringProperty("text-align", n) ?: "start";

            // build the layout
            val spans = splitIntoSpans(n)
            val lines = buildLines(n, spans, replaceZero(width))
            val simplifiedLines = simplifyLines(lines)

            // replace original svg
            removeAllChildren(n)
            replaceContents(n, simplifiedLines, align, lines.map { it.second }.maxOfOrNull { it } ?: 0.0, replaceZero(height), "text")

            return n
        } else {
            return super.processTag(n)
        }
    }

    companion object {

        fun calculateLineHeight(n: Element, ctx: ElementContext): Double {
            val fontSize = ctx.getCssStyleDoubleProperty("font-size", n)
            var lineHeight = ctx.getCssStyleDoubleProperty("line-height", n)
            if (lineHeight == 0.0) {
                lineHeight = 1.2 * fontSize;
            }
            return lineHeight
        }
    }



    fun removeAllChildren(e: Element) {
        val childNodes : NodeList = e.childNodes
        while (childNodes.length > 0) {
            e.removeChild(childNodes.item(0)!!)
        }
    }

    fun replaceContents(e: Element, lines: List<Pair<List<Span>, Double>>, align: String, maxLineWidth: Double, maxHeight: Double, tagName: String)  {
        val od = e.ownerDocument!!

        for ((lineNumber, t) in lines.withIndex()) {
            var sx =  when (align) {
                "end" ->  maxLineWidth - t.second
                "middle" -> (maxLineWidth - t.second) / 2.0
                else -> 0.0
            }

            val lineHeight = t.first.maxOfOrNull { it.height } ?: 0.0

            t.first.forEach {
                val cspan = if (it is StringSpan) {
                    val tag = od.createElementNS(Kite9Namespaces.SVG_NAMESPACE, tagName)
                    val text = od.createTextNode(it.s)
                    tag.appendChild(text)
                    tag.setAttribute("y", ""+  (lineHeight * lineNumber) +"px")
                    tag
                } else {
                    val tag = (it as ImageSpan).e
                    // make the image sit on the line
                    tag.setAttribute("y", ""+((lineHeight * lineNumber) - it.height) + "px")
                    tag
                }

                cspan.setAttribute("x", ""+sx+"px")

                if (lineHeight * (lineNumber + 1) > maxHeight) {
                    cspan.setAttribute("display", "none")
                }

                e.appendChild(cspan)
                sx += it.width
            }

        }
    }

    enum class SpanType { SPACE, PUNCTUATION, WORD_PART, NEW_LINE }

    interface Span {

        val width: Double
        val height: Double
    }

    data class StringSpan(val s: String, val type: SpanType, override val width: Double, override val height: Double) : Span {}

    data class ImageSpan(val e: Element, override val width: Double, override val height: Double) : Span {}

    fun splitIntoSpans(n: Node) : List<Span> {
        return if (isTextNode(n)) {
            val text = n.textContent ?: ""
            splitIntoSpans(text, n as Element)
        } else if (isImageNode(n))  {
            val bounds = ctx.bounds(n as Element)
            return listOf(ImageSpan(n,
                bounds?.width ?: 0.0,
                bounds?.height ?: 0.0))
        } else {
            val list = n.childNodes
            val out = mutableListOf<Span>()
            for (i in 0..list.length-1) {
                var ret = splitIntoSpans(list.item(i)!!)
                out.addAll(ret)
            }
            out
        }
    }

    private fun isTextNode(n: Node) =
        (n is Element) && ((n.localName == "text") || (n.localName == "cspan")) && (n.namespaceURI == Kite9Namespaces.SVG_NAMESPACE)

    private fun isImageNode(n: Node) =
        (n is Element) && (n.localName == "image") && (n.namespaceURI == Kite9Namespaces.SVG_NAMESPACE)

    private fun isWrapContents(n: Element) =
        ctx.getCssStyleEnumProperty(CSSConstants.ELEMENT_TYPE_PROPERTY, n, DiagramElementType::class) == DiagramElementType.TEXT


    fun splitIntoSpans(s: String, inside: Element) : List<Span> {
        val out = mutableListOf<Span>()
        var last = SpanType.WORD_PART
        val buf = StringBuilder()

        fun breakBuffer() {
            if (buf.isNotEmpty()) {
                val width = ctx.textWidth(buf.toString(), inside)
                val lineHeight = calculateLineHeight(inside, ctx)
                out.add(StringSpan(buf.toString(), last, width, lineHeight));
                buf.clear()
            }
        }

        fun consume(c: Char, current: SpanType) {
            when (last) {
                SpanType.SPACE -> {
                    when (current) {
                        SpanType.PUNCTUATION, SpanType.WORD_PART, SpanType.NEW_LINE -> breakBuffer()
                    }
                }
                SpanType.PUNCTUATION -> {
                    when (current) {
                        SpanType.PUNCTUATION, SpanType.SPACE, SpanType.NEW_LINE -> breakBuffer()
                    }
                }
                SpanType.WORD_PART -> {
                    when (current) {
                        SpanType.SPACE, SpanType.NEW_LINE -> breakBuffer()
                    }
                }
                SpanType.NEW_LINE -> breakBuffer()
            }

            last = current
            buf.append(c);
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


    fun buildLines(within: Element, spans: List<Span>, width: Double): List<Pair<List<Span>, Double>> {
        var line = 0
        var out = mutableListOf<Pair<List<Span>, Double>>()
        var currentSpan = mutableListOf<Span>()
        var currentLineLength = 0.0

        fun newLine(span: Span) {
            if (currentLineLength > 0) {
                line++
                currentSpan.add(span)
                out.add(Pair(currentSpan, currentLineLength))
                currentSpan = mutableListOf()
                currentLineLength = 0.0
            }
        }

        fun continueLine(span: Span, newWidth: Double, isSpace: Boolean) {
            if (!isSpace) {
                if (currentLineLength + newWidth > width) {
                    // need to wrap before adding
                    newLine(StringSpan("", SpanType.SPACE, 0.0, 0.0));
                }
            }

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

        newLine(StringSpan("", SpanType.SPACE, 0.0, 0.0))
        return out
    }

    fun simplifyLines(lines: List<Pair<List<Span>, Double>>) : List<Pair<List<Span>, Double>> {
        return lines.map { Pair( mergeSpans(it.first), it.second) }
    }

    fun mergeSpans(spans: List<Span>) : List<Span> {
        val out = mutableListOf<Span>()
        spans.forEach {
            if ((out.lastOrNull() is StringSpan) && (it is StringSpan)) {
                val last = out.removeLast() as StringSpan
                val newLast = StringSpan(last.s + it.s, SpanType.WORD_PART, last.width + it.width, max(last.height, it.height))
                out.add(newLast)
            } else {
                out.add(it)
            }
        }
        return out
    }
}