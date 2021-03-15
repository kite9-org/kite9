package org.kite9.diagram.dom.processors

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.model.position.Rectangle2D
import org.w3c.dom.Element
import org.w3c.dom.Text

/**
 * The text-wrap processor examines <text> elements in the SVG, and reformats them as a
 * set of <tspan> elements.
 */
class TextWrapProcessor(val ctx: ElementContext) : AbstractInlineProcessor() {

    private val SPECIAL_WORD_CHARS = "\"\'\u2018\u2019\u201C\u201D?./!,;:_".toCharArray().toSet()

    private val PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toCharArray().toSet()

    override fun processTag(n: Element): Element {
        if ((n.localName == "text") && (n.namespaceURI == SVG_NAMESPACE)) {
            val width = ctx.getCssStyleDoubleProperty(CSSConstants.TEXT_BOUNDS_WIDTH, n)
            val height = ctx.getCssStyleDoubleProperty(CSSConstants.TEXT_BOUNDS_HEIGHT, n)
            val theText = n.textContent
            if ((width > 0.0) || (height > 0.0)) {
                val spans = splitIntoSpans(theText)
                val lines = buildLines(n, spans, width)
                val lineHeight = ctx.getCssStyleDoubleProperty("font-size", n)
                removeAllChildren(n)
                replaceWithCSpans(n, lines, lineHeight)
            }

            return n
        } else {
            return super.processTag(n)
        }
    }

    fun removeAllChildren(e: Element) {
        while (e.childNodes.length > 0) {
            e.removeChild(e.childNodes.item(0))
        }
    }

    fun replaceWithCSpans(e: Element, lines: List<String>, lineHeight: Double)  {
        val od = e.ownerDocument

        for ((lineNumber, t) in lines.withIndex()) {
            val cspan = od.createElementNS(SVG_NAMESPACE, "tspan")
            cspan.setAttribute("x", "0px")
            cspan.setAttribute("y", ""+  (lineHeight * lineNumber) +"px")
            val text = od.createTextNode(t)
            cspan.appendChild(text)
            e.appendChild(cspan)
        }
    }

    enum class SpanType { SPACE, PUNCTUATION, WORD_PART }

    fun splitIntoSpans(s: String) : List<String> {
        val out = mutableListOf<String>()
        var last = SpanType.WORD_PART
        val buf = StringBuilder()

        fun breakBuffer() {
            if (buf.isNotEmpty()) {
                out.add(buf.toString());
                buf.clear()
            }
        }

        fun consume(c: Char, current: SpanType) {
            when (last) {
                SpanType.SPACE -> {
                    when (current) {
                        SpanType.PUNCTUATION, SpanType.WORD_PART -> breakBuffer()
                    }
                }
                SpanType.PUNCTUATION -> {
                    when (current) {
                        SpanType.PUNCTUATION, SpanType.SPACE -> breakBuffer()
                    }
                }
                SpanType.WORD_PART -> {
                    when (current) {
                        SpanType.SPACE -> breakBuffer()
                    }
                }

            }

            last = current
            buf.append(c);
        }


        for (c in s) {
            when {
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


    fun buildLines(within: Element, spans: List<String>, width: Double): List<String> {
        var line = 0
        var out = mutableListOf<String>()
        var currentSpanText : String = ""
        var currentLineLength = 0.0

        fun spanType(s : String) : SpanType {
            return when {
                s.isBlank() -> SpanType.SPACE
                PUNCTUATION.contains(s[0]) -> SpanType.PUNCTUATION
                else -> SpanType.WORD_PART
            }
        }

        fun newLine() {
            if (currentLineLength > 0) {
                line++
                out.add(currentSpanText)
                currentSpanText = ""
                currentLineLength = 0.0
            }
        }

        fun continueLine(s: String, newWidth: Double, isSpace: Boolean) {
            if (!isSpace) {
                if (currentLineLength + newWidth > width) {
                    // need to wrap before adding
                    newLine();
                }
            }

            currentSpanText += s
            currentLineLength += newWidth
        }

        for (text in spans) {
            val isSpace = spanType(text) == SpanType.SPACE
            if ((currentLineLength == 0.0) && isSpace) {
                continueLine(text, 0.0, isSpace)
            } else {
                continueLine(text, ctx.textWidth(text, within), isSpace);
            }
        }

        newLine()
        return out
    }

}