package com.kite9.pipeline.command.xml.replace

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command
import com.kite9.pipeline.command.CommandContext
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*

/**
 * Replaces some tag names and attributes of tags, but keeps contents unaltered.
 *
 * @author robmoffat
 */
open class ReplaceTag : AbstractReplaceCommand<Element?, Element>() {

    @JvmField
    var keptAttributes : List<String> = Arrays.asList("id") // just keep ID by default.

    private fun keepImportantAttributes(e: Element, n: Element) {
        for (a in keptAttributes) {
            if (e.hasAttribute(a)) {
                n.setAttribute(a, e.getAttribute(a))
            } else {
                n.removeAttribute(a);
            }
        }
    }

    private fun copyAttributes(from: Element, to: Element, matching: Boolean) {
        for (i in 0 until from.attributes.length) {
            val item = from.attributes.item(i) as Attr
            if (matching == keptAttributes.contains(item.nodeName)) {
                to.setAttribute(item.nodeName, item.nodeValue)
            }
        }
    }

    private fun moveContents(from: Element, to: Element) {
        val toNodes = to.childNodes
        while (toNodes.length > 0) {
            to.removeChild(toNodes.item(0))
        }
        val fromNodes = from.childNodes
        while (fromNodes.length > 0) {
            to.appendChild(fromNodes.item(0))
        }
    }

    override fun doReplace(on: ADLDom, site: Element?, toContent: Element, fromContent: Element, ctx: CommandContext) : Command.Mismatch? {
        if (site==null) {
            // in this case, n must also be null, so nothing to do
            return null
        }

        val doc: Document = on.document
        doc.adoptNode(toContent)
        keepImportantAttributes(site, toContent)
        site.parentNode.insertBefore(toContent, site)
        copyAttributes(site, toContent, true)
        moveContents(site, toContent)
        site.parentNode.removeChild(site)
        ctx.log("Processed replace tag of $fragmentId")
        return null
    }

    override fun same(existing: Element?, with: Element, ctx: CommandContext): Command.Mismatch? {
        if (existing==null) {
            return Command.Mismatch { "Existing is null, so not a replace" }
        }
        val m1: Command.Mismatch? = checkAttributesSame(existing, with)
        return m1 ?: checkAttributesSame(with, existing)
    }

    private fun checkAttributesSame(a: Element, b: Element): Command.Mismatch? {
        val nnm = a.attributes
        for (i in 0 until nnm.length) {
            val aa = nnm.item(i) as Attr
            if (!keptAttributes.contains(aa.name) && "xmlns" != aa.name) {
                val aValue = aa.value
                val bValue = b.getAttribute(aa.name)
                if (aValue != bValue) {
                    return Command.Mismatch { "Attribute" + aa.name + " differs: " + aValue + " and " + bValue }
                }
            }
        }
        return null
    }

    override fun getFromContent(context: ADLDom, ctx: CommandContext): Element {
        return ctx.decodeElement(from, context)
    }

    override fun getToContent(context: ADLDom, ctx: CommandContext): Element {
        return ctx.decodeElement(to, context)
    }

    override fun getExistingContent(o: ADLDom, ctx: CommandContext): Element? {
        return findFragmentElement(o.document, fragmentId, ctx)
    }
}