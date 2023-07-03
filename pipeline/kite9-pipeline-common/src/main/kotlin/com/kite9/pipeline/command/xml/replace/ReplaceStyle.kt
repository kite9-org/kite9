package com.kite9.pipeline.command.xml.replace

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command
import com.kite9.pipeline.command.CommandContext

class ReplaceStyle : AbstractReplaceCommand<String?, String?>() {

    @JvmField
    var name: String = ""

    override fun getFromContent(context: ADLDom, ctx: CommandContext): String? {
        return from
    }

    override fun getToContent(context: ADLDom, ctx: CommandContext): String? {
        return to
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
    ) : Command.Mismatch? {
        val e = findFragmentElement(on.document, fragmentId, ctx)

        if (e==null) {
            return Command.Mismatch { "Couldn't find element with id "+fragmentId }
        }

        ctx.setStyleValue(e, name, toContent)

        return null
    }

    override fun checkProperties() {
        ensureNotNull("name", name)
        super.checkProperties()
    }

    override fun same(existing: String?, with: String?, ctx: CommandContext): Command.Mismatch? {
        return if (isEmpty(existing) && isEmpty(with)) {
            null
        } else super.same(existing, with, ctx)
    }
}