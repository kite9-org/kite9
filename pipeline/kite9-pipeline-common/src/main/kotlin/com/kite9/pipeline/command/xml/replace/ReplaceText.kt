package com.kite9.pipeline.command.xml.replace

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command.Mismatch
import com.kite9.pipeline.command.CommandContext
import org.w3c.dom.Element
import java.util.*

class ReplaceText : AbstractReplaceCommand<Element, String>() {

    enum class PreserveChildElements {
        BEFORE, AFTER, NONE
    }

	var preserve = PreserveChildElements.AFTER

    private fun collectChildren(e: Element): List<Element?> {
        return if (preserve == PreserveChildElements.NONE) {
            emptyList<Element>()
        } else {
            val out: MutableList<Element?> = ArrayList()
            val nl = e.childNodes
            for (i in 0 until nl.length) {
                if (nl.item(i) is Element) {
                    out.add(nl.item(i) as Element)
                }
            }
            out
        }
    }

    override fun getFromContent(context: ADLDom, ctx: CommandContext): String {
        return from!!
    }

    override fun getToContent(context: ADLDom, ctx: CommandContext): String {
        return to!!
    }

    override fun getExistingContent(o: ADLDom, ctx: CommandContext): Element {
        return findFragmentElement(o.document, fragmentId, ctx)
    }

    override fun doReplace(on: ADLDom, site: Element, toContent: String, fromContent: String, ctx: CommandContext) {
        val childElements = collectChildren(site)
        site.textContent = toContent
        if (preserve == PreserveChildElements.BEFORE) {
            Collections.reverse(childElements)
            childElements.stream().forEach { c: Element? -> site.insertBefore(c, null) }
        } else if (preserve == PreserveChildElements.AFTER) {
            childElements.stream().forEach { c: Element? -> site.appendChild(c) }
        }
        ctx.log("Processed replace text of $fragmentId")
    }

    protected override fun same(existing: Element, with: String): Mismatch? {
        val eText = existing.textContent
        val eTextReplaced = eText.replace("\\s".toRegex(), "")
        val withReplaced = with.replace("\\s".toRegex(), "")
        return when (preserve) {
            PreserveChildElements.AFTER -> check(eTextReplaced.startsWith(withReplaced), eText, with)
            PreserveChildElements.BEFORE -> check(eTextReplaced.endsWith(withReplaced), eText, with)
            PreserveChildElements.NONE -> check(eTextReplaced == withReplaced, eText, with)
            else -> check(eTextReplaced.startsWith(withReplaced), eText, with)
        }
    }

    private fun check(ok: Boolean, eText: String, with: String): Mismatch? {
        return if (ok) null else Mismatch { "Text not same: '$eText' and '$with'" }
    }
}