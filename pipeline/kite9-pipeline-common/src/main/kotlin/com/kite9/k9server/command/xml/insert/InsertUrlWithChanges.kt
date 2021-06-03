package com.kite9.k9server.command.xml.insert

import com.kite9.k9server.adl.holder.pipeline.ADLDom
import com.kite9.k9server.command.CommandContext
import org.w3c.dom.Element

/**
 * Used for inserting links, where we have to say what the link joins to/from.
 *
 * @author robmoffat
 */
open class InsertUrlWithChanges : InsertUrl() {

    var xpathToValue: Map<String, String> = emptyMap()

    protected fun updateLinkEnds(insert: Element?, ctx: CommandContext) {
        if (insert != null) {
            xpathToValue.forEach { xpath, value ->
                ctx.setAttributeValue(insert, xpath, value)
            }
        }
    }

    override fun getContents(d: ADLDom, ctx: CommandContext): Element? {
        val out = super.getContents(d, ctx)
        updateLinkEnds(out, ctx)
        return out
    }

}