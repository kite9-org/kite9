package com.kite9.pipeline.command.xml.move

import com.kite9.k9server.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.k9server.pipeline.command.Command.Mismatch
import com.kite9.k9server.pipeline.command.CommandContext
import com.kite9.k9server.pipeline.command.xml.AbstractADLCommand

open class Move : AbstractADLCommand() {

    // guy we are moving
    var moveId : String? = null
	var from: String? = null
    var fromBefore: String? = null
	var to: String? = null
	var toBefore: String? = null
    
    override fun applyCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        return move(d, moveId, to, toBefore, ctx)
    }

    private fun move(d: ADLDom, moveId: String?, to: String?, toBefore: String?, ctx: CommandContext): Mismatch? {
        checkProperties()
        val doc = d.document
        val moveEl = doc.getElementById(moveId)
        val toEl = doc.getElementById(to)
        val toBeforeEl = doc.getElementById(toBefore)
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