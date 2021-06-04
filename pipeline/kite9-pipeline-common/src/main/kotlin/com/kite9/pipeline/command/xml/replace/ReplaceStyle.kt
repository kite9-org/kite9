package com.kite9.pipeline.command.xml.replace

import com.kite9.k9server.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.k9server.pipeline.command.Command
import com.kite9.k9server.pipeline.command.CommandContext

class ReplaceStyle : AbstractReplaceCommand<String?, String?>() {

    var name: String = ""

    override fun getFromContent(context: ADLDom, ctx: CommandContext): String {
        return from!!
    }

    override fun getToContent(context: ADLDom, ctx: CommandContext): String {
        return to!!
    }

    override fun getExistingContent(o: ADLDom, ctx: CommandContext): String? {
        val e = findFragmentElement(o.document, fragmentId, ctx)
        return ctx.getStyleValue(e, name)
    }

    override fun doReplace(
        on: ADLDom,
        site: String?,
        toContent: String?,
        fromContent: String?,
        ctx: CommandContext
    ) {
        val e = findFragmentElement(on.document, fragmentId, ctx)
        ctx.setStyleValue(e, name, toContent)
    }

    override fun checkProperties() {
        ensureNotNull("name", name)
        super.checkProperties()
    }

    override fun same(existing: String?, with: String?): Command.Mismatch? {
        return if (isEmpty(existing) && isEmpty(with)) {
            null
        } else super.same(existing, with)
    }
}