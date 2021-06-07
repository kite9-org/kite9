package com.kite9.pipeline.command.xml

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command
import com.kite9.pipeline.command.CommandContext
import com.kite9.pipeline.command.CommandException
import com.kite9.pipeline.uri.K9URI
import org.w3c.dom.*

/**
 * Contains a hash, which is used to make sure that the command is operating on a fresh version
 * of the data.
 *
 * @author robmoffat
 */
abstract class AbstractADLCommand : Command {

    fun ensureNotNull(field: String, value: Any?) {
        if (value == null) {
            throw CommandException(BAD_REQUEST, this.javaClass.name + " requires " + field + " to be set", this, null)
        }
    }

    protected fun isEmpty(s: String?) = s == null || s.trim { it <= ' ' }.length == 0


    protected fun findFragmentElement(
        doc: Document,
        fragmentId: String?,
        ctx: CommandContext
    ): Element {
        var fragmentId = fragmentId
        if (isEmpty(fragmentId)) {
            return doc.documentElement
        }
        val partIndex = fragmentId!!.indexOf("@")
        if (partIndex > -1) {
            ctx.log("Stripping part from $fragmentId")
            fragmentId = fragmentId.substring(0, partIndex)
        }
        return doc.getElementById(fragmentId)
    }

    fun getForeignElementCopy(
        currentDoc: Document?,
        baseUri: K9URI,
        uriStr: String,
        deep: Boolean,
        context: ADLDom
    ): Element {
        var currentDoc = currentDoc
        return try {
            val id = uriStr.substring(uriStr.indexOf("#") + 1)
            val location = uriStr.substring(0, uriStr.indexOf("#"))
            if (location.length > 0) {
                // referencing a different doc.
                val uri = baseUri.resolve(location)
                currentDoc = context.parseDocument(uri)
            }
            val template = currentDoc!!.getElementById(id)
            template.cloneNode(deep) as Element
        } catch (e: Exception) {
            throw RuntimeException("Couldn't get foreign element: $uriStr")
        }
    }

    /**
     * For some reason, in the clone node process, batik clears the parent element setting, and this doesn't get fixed.
     */
    protected fun ensureParentElements(parent: Node?, child: Node, ctx: CommandContext) {
        if (child is Attr && child.ownerElement == null) {
            ctx.setOwnerElement(child, (parent as Element?)!!)
        }
        if (child is Element) {
            // this makes sure the document has it's id-map updated so you can
            // use getElementById in further commands.
            child.setAttributeNode(child.getAttributeNode("id"))
            val children = child.getChildNodes()
            for (i in 0 until children.length) {
                ensureParentElements(child, children.item(i), ctx)
            }
            val map = child.getAttributes()
            for (i in 0 until map.length) {
                ensureParentElements(child, map.item(i), ctx)
            }
        }
    }

    protected fun replaceIds(insert: Element, ctx: CommandContext) {
        replaceIds(insert, ctx.uniqueId(insert.ownerDocument))
    }

    protected fun replaceIds(insert: Element, base: String) {
        if (insert.hasAttribute("id")) {
            insert.setAttribute("id", base)
        }
        val children = insert.childNodes
        replaceIds(children, "$base-")
    }

    protected fun replaceIds(children: NodeList, base: String) {
        var nextId = 0
        for (i in 0 until children.length) {
            val n = children.item(i)
            if (n is Element) {
                replaceIds(n, base + nextId)
                nextId++
            }
        }
    }

    companion object {
        val HTTP_CONFLICT = 409
        val BAD_REQUEST = 400
    }
}