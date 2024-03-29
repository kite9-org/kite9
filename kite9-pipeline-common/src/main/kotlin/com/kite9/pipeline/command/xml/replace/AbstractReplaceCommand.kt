package com.kite9.pipeline.command.xml.replace

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command.Mismatch
import com.kite9.pipeline.command.CommandContext
import com.kite9.pipeline.command.xml.AbstractADLCommand

abstract class AbstractReplaceCommand<E, T> : AbstractADLCommand() {

    @JvmField
    public var fragmentId: String = ""

    @JvmField
    public var from: String? = null

    @JvmField
    var to: String? = null

    protected abstract fun getFromContent(context: ADLDom, ctx: CommandContext): T
    protected abstract fun getToContent(context: ADLDom, ctx: CommandContext): T
    protected abstract fun getExistingContent(o: ADLDom, ctx: CommandContext): E

    override fun applyCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        checkProperties()
        val existing = getExistingContent(d, ctx)
        val fromContent = getFromContent(d, ctx)
        val m = same(existing, fromContent, ctx)
        if (m != null) {
            return m
        }
        return doReplace(d, existing, getToContent(d, ctx), fromContent, ctx)
    }

    protected open fun checkProperties() {
        ensureNotNull("fragmentId", fragmentId)
    }

    protected abstract fun doReplace(on: ADLDom, site: E, toContent: T, fromContent: T, ctx: CommandContext) : Mismatch?

    protected open fun same(existing: E, with: T, ctx: CommandContext): Mismatch? {
        return if (existing == with) {
            null
        } else {
            Mismatch { "Diagram has changed since move command issued.  Expected $with got $existing" }
        }
    }

    override fun undoCommand(d: ADLDom, ctx: CommandContext): Mismatch? {
        checkProperties()
        val existing = getExistingContent(d, ctx)
        val toContent = getToContent(d, ctx)
        val m = same(existing, toContent, ctx)
        if (m != null) {
            return m
        }
        doReplace(d, existing, getFromContent(d, ctx), toContent, ctx)
        return null
    }
}