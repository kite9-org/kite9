package com.kite9.pipeline.command.xml.insert

import com.kite9.k9server.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.k9server.pipeline.command.Command.Mismatch
import com.kite9.k9server.pipeline.command.CommandContext
import org.w3c.dom.Element

open class InsertUrl : AbstractInsertCommand() {

 	var uriStr : String? = null

    override fun applyCommand(d: ADLDom, ctx:CommandContext): Mismatch? {
        checkProperties()
        return doInsert(d, ctx)
    }

    override fun undoCommand(d: ADLDom, ctx:CommandContext): Mismatch? {
        checkProperties()
        return doDelete(d, ctx)
    }

    override fun getContents(d: ADLDom, ctx:CommandContext): Element? {
        val out = getForeignElementCopy(d.document, d.uri, uriStr!!, true, d)
        replaceIds(out, newId!!)
        return out
    }

    override fun checkProperties() {
        super.checkProperties()
        ensureNotNull("uriStr", uriStr)
        ensureNotNull("newId", newId)
    }
}