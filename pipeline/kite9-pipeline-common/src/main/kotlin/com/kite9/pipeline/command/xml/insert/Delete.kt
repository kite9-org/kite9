package com.kite9.pipeline.command.xml.insert

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command.Mismatch
import com.kite9.pipeline.command.CommandContext
import org.w3c.dom.Element

/**
 * Delete is basically the opposite of insert; you have to say what you're deleting so it can be
 * checked/undone.
 *
 * @author robmoffat
 */
class Delete : AbstractInsertCommand() {

	var base64Element: String = ""

    override fun applyCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        checkProperties()
        return doDelete(d, ctx)
    }

    override fun undoCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        checkProperties()
        return doInsert(d, ctx)
    }

    override fun getContents(d: ADLDom, ctx: CommandContext): Element? {
        return copyWithoutContainedIds(ctx.decodeElement(base64Element, d))
    }

    override fun checkProperties() {
        super.checkProperties()
        ensureNotNull("base64Element", fragmentId)
    }
}