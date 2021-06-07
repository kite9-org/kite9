package com.kite9.pipeline.command.xml.replace

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command
import com.kite9.pipeline.command.CommandContext
import com.kite9.pipeline.command.CommandException
import org.w3c.dom.Document
import org.w3c.dom.Element

class ReplaceXML : AbstractReplaceCommand<Element, Element>() {

    override fun getFromContent(context: ADLDom, ctx: CommandContext): Element {
        val out: Element = ctx.decodeElement(from, context)
        if (out.getAttribute("id") != fragmentId) {
            throw CommandException(BAD_REQUEST, "ReplaceXML should preserve ID", this, null)
        }
        return out
    }

    override fun getToContent(context: ADLDom, ctx: CommandContext): Element {
        val out: Element = ctx.decodeElement(to, context)
        if (out.getAttribute("id") != fragmentId) {
            throw CommandException(BAD_REQUEST, "ReplaceXML should preserve ID", this, null)
        }
        return out
    }

    override fun getExistingContent(o: ADLDom, ctx: CommandContext): Element {
        return findFragmentElement(o.document, fragmentId, ctx)
    }

    override fun doReplace(
        on: ADLDom,
        site: Element,
        toContent: Element,
        fromContent: Element,
        ctx: CommandContext
    ) {
        val doc: Document = on.document
        doc.adoptNode(toContent)
        val into = site.parentNode
        into.replaceChild(toContent, site)
        ensureParentElements(into, toContent, ctx)
        ctx.log("Processed replace XML of $fragmentId")
    }

    private fun same(existing: Element, with: Element, ctx: CommandContext): Command.Mismatch? {
        return ctx.twoElementsAreIdentical(existing, with)
    }
}