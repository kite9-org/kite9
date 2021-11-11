package com.kite9.pipeline.command.xml.replace

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.CommandContext
import org.w3c.dom.Element

/**
 * Allows the to field to be loaded from a url.
 *
 * @author robmoffat
 */
class ReplaceTagUrl : ReplaceTag() {

    override fun getToContent(adl: ADLDom, ctx: CommandContext): Element {
        return getForeignElementCopy(adl.document, adl.uri!!, to!!, false, adl, ctx)
    }
}