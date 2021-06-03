package com.kite9.k9server.command.xml.replace

import com.kite9.k9server.adl.holder.pipeline.ADLDom
import com.kite9.k9server.command.Command
import com.kite9.k9server.command.CommandContext
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*

/**
 * Replaces some tag names and attributes of tags, but keeps contents unaltered.
 *
 * @author robmoffat
 */
open class ReplaceTag : AbstractReplaceCommand<Element, Element>() {

	var keptAttributes = Arrays.asList("id") // just keep ID by default.
    var keptTags = emptyList<String>()

    private fun checkKeptTags(e: Element, n: Element, d: Document) {
        if (keptTags.contains(e.tagName)) {
            d.renameNode(n, e.namespaceURI, e.localName)
        }
    }

    private fun keepImportantAttributes(e: Element, n: Element) {
        for (a in keptAttributes) {
            if (e.hasAttribute(a)) {
                n.setAttribute(a, e.getAttribute(a))
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

    override fun doReplace(d: ADLDom, e: Element, n: Element, fromContent: Element, ctx: CommandContext) {
        val doc: Document = d.document
        doc.adoptNode(n)
        keepImportantAttributes(e, n)
        e.parentNode.insertBefore(n, e)
        ensureParentElements(e.parentNode, n, ctx)
        copyAttributes(e, n, true)
        checkKeptTags(e, n, doc)
        moveContents(e, n)
        e.parentNode.removeChild(e)
        ctx.log("Processed replace tag of $fragmentId")
    }

    protected override fun same(existing: Element, with: Element): Command.Mismatch? {
        val m1: Command.Mismatch? = checkAttributesSame(existing, with)
        return if (m1 == null) checkAttributesSame(with, existing) else m1
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

    override fun getFromContent(adl: ADLDom, ctx: CommandContext): Element {
        return ctx.decodeElement(from, adl)
    }

    override fun getToContent(adl: ADLDom, ctx: CommandContext): Element {
        return ctx.decodeElement(to, adl)
    }

    override fun getExistingContent(d: ADLDom, ctx: CommandContext): Element {
        return findFragmentElement(d.document, fragmentId, ctx)
    }
}