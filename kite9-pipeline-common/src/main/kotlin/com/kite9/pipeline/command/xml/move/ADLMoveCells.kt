package com.kite9.pipeline.command.xml.move

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command
import com.kite9.pipeline.command.CommandContext
import com.kite9.pipeline.command.xml.AbstractADLCommand
import org.kite9.diagram.common.range.BasicIntegerRange
import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.dom.css.CSSConstants
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Makes some space in a grid for new cells to be dropped in.  This is a special
 * operation that actually understands something of the diagram it is being used in.
 *
 * We could change this later to be more like InsertUrlWithChanges...
 *
 * @author robmoffat
 */
class ADLMoveCells : AbstractADLCommand() {

    @JvmField
    var push: Int = 0

    @JvmField
    var from: Int = 0

    @JvmField
    var horiz: Boolean = false

    @JvmField
    var fragmentId: String? = null

    @JvmField
    var excludedIds = emptyList<String>()

    private fun doMove(adl: ADLDom, push: Int, ctx: CommandContext) : Command.Mismatch? {
        checkProperties()
        val doc: Document = adl.document
        val container: Element? = findFragmentElement(doc, fragmentId, ctx)

        if (container == null) {
            return Command.Mismatch { "Couldn't find container $fragmentId" }
        }

        var moved = 0
        val contents = container.childNodes
        for (i in 0 until contents.length) {
            if (contents.item(i) is Element) {
                val el = contents.item(i) as Element
                val id: String = el.getAttribute("id")
                if (!excludedIds.contains(id)) {
                    val r = getRange(el, horiz, ctx)

                    if (r != null) {
                        if (r.from >= from) {
                            setRange(el, horiz, BasicIntegerRange(r.from + push, r.to + push), ctx)
                            moved++
                        } else if (r.to >= from) {
                            setRange(el, horiz, BasicIntegerRange(r.from, r.to + push), ctx)
                            moved++
                        }
                    }
                }
            }
        }

        ctx.log("Processed move from $from push $push horiz=$horiz,moved=$moved")
        return null
    }

    private fun checkProperties() {
        ensureNotNull("fragmentId", fragmentId)
        ensureNotNull("from", from)
        ensureNotNull("push", push)
    }

    override fun applyCommand(d: ADLDom, ctx: CommandContext): Command.Mismatch? {
        return doMove(d, push, ctx)
    }

    override fun undoCommand(d: ADLDom, ctx: CommandContext): Command.Mismatch? {
        return doMove(d, -push, ctx)
    }

    private fun getRange(el: Element, horiz: Boolean, ctx: CommandContext) : IntegerRange? {
        return when (horiz) {
            true -> ctx.getStyleRangeValue(el, CSSConstants.GRID_OCCUPIES_X_PROPERTY)
            false -> ctx.getStyleRangeValue(el, CSSConstants.GRID_OCCUPIES_Y_PROPERTY)
        }
    }

    private fun setRange(el: Element, horiz: Boolean, r: IntegerRange, ctx: CommandContext) {
        when (horiz) {
            true -> ctx.setStyleValue(el, CSSConstants.GRID_OCCUPIES_X_PROPERTY, r.toString())
            false -> ctx.setStyleValue(el, CSSConstants.GRID_OCCUPIES_Y_PROPERTY, r.toString())
        }
    }
}