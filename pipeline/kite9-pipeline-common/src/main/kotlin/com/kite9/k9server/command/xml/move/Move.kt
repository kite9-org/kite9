package com.kite9.k9server.command.xml.move

import com.kite9.k9server.adl.holder.pipeline.XMLDom.document
import org.kite9.diagram.logging.Kite9Log.send
import com.kite9.k9server.command.xml.AbstractADLCommand.ensureNotNull
import com.kite9.k9server.command.xml.AbstractADLCommand
import com.kite9.k9server.adl.holder.pipeline.ADLDom
import org.kite9.diagram.logging.Kite9Log
import com.kite9.k9server.command.Command.Mismatch

class Move : AbstractADLCommand() {
    // guy we are moving
    var moveId : String? = null
	var from: String? = null
    var fromBefore: String? = null
	var to: String? = null
	var toBefore: String? = null
    fun applyCommand(`in`: ADLDom, log: Kite9Log): Mismatch? {
        return move(`in`, moveId, to, toBefore, log)
    }

    private fun move(`in`: ADLDom, moveId: String?, to: String?, toBefore: String?, log: Kite9Log): Mismatch? {
        checkProperties()
        val doc = `in`.document
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
        log.send("Completed move into $to")
        return null
    }

    protected fun checkProperties() {
        ensureNotNull("moveId", moveId)
        ensureNotNull("from", from)
        ensureNotNull("to", to)
    }

    fun undoCommand(`in`: ADLDom, log: Kite9Log): Mismatch? {
        return move(`in`, moveId, from, fromBefore, log)
    }
}