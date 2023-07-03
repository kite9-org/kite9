package com.kite9.pipeline.command.xml.insert

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command.Mismatch
import com.kite9.pipeline.command.CommandContext
import org.w3c.dom.Element

/**
 * As well as inserting, you can also use this to "surround" an element.  Not sure if
 * we'll use that yet, as you can achive the same result with move.
 *
 * @author robmoffat
 */
class InsertXML : AbstractInsertCommand() {

    @JvmField
    var base64Element: String? = null

    override fun applyCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        checkProperties()
        return doInsert(d, ctx)
    }

    override fun undoCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        checkProperties()
        return doDelete(d, ctx)
    }

    override fun getContents(d: ADLDom, ctx: CommandContext): Element? {
        val out = copyWithoutContainedIds(ctx.decodeElement(base64Element, d))
        if (newId != null) {
            replaceIds(out!!, newId!!)
        }
        return out
    }

    override fun checkProperties() {
        super.checkProperties()
        ensureNotNull("base64Element", fragmentId)
    }
}