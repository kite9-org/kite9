package com.kite9.pipeline.command.xml.move

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command.Mismatch
import com.kite9.pipeline.command.CommandContext
import com.kite9.pipeline.command.xml.AbstractADLCommand

open class Move : AbstractADLCommand() {

    // guy we are moving
    @JvmField
    var moveId : String? = null

    @JvmField
    var from: String? = null

    @JvmField
    var fromBefore: String? = null

    @JvmField
    var to: String? = null

    @JvmField
    var toBefore: String? = null
    
    override fun applyCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        return move(d, moveId, to, toBefore, ctx)
    }

    private fun move(d: ADLDom, moveId: String?, to: String?, toBefore: String?, ctx: CommandContext): Mismatch? {
        checkProperties()
        val doc = d.document
        val moveEl = ctx.getElementById(doc, moveId)
        val toEl = ctx.getElementById(doc, to)
        val toBeforeEl = ctx.getElementById(doc, toBefore)
        if (moveEl == null || toEl == null) {
            return Mismatch { "Element missing, moveEl=$moveEl, toEl=$toEl" }
        }
        if (toBeforeEl == null) {
            toEl.appendChild(moveEl)
        } else {
            toEl.insertBefore(moveEl, toBeforeEl)
        }
        ctx.log("Completed move into $to")
        return null
    }

    private fun checkProperties() {
        ensureNotNull("moveId", moveId)
        ensureNotNull("from", from)
        ensureNotNull("to", to)
    }

    override fun undoCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        return move(d, moveId, from, fromBefore, ctx)
    }
}