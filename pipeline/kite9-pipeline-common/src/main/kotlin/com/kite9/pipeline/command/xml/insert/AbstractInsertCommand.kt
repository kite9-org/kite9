package com.kite9.pipeline.command.xml.insert

import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.command.Command.Mismatch
import com.kite9.pipeline.command.CommandContext
import com.kite9.pipeline.command.xml.AbstractADLCommand
import org.w3c.dom.Element
import java.util.*

abstract class AbstractInsertCommand : AbstractADLCommand() {

	var fragmentId: String? = null
	var beforeId: String? = null
	var containedIds = emptyList<String>()
	var newId: String? = null

    protected fun doDelete(d: ADLDom, ctx: CommandContext): Mismatch? {
        val oldState = getContents(d, ctx)

        if (oldState == null) {
            return null
        }

        val toDelete = findFragmentElement(d.document, oldState.getAttribute("id"), ctx)
        val parent = toDelete.parentNode as Element
        val m = same(toDelete, oldState, ctx)
        if (m != null) {
            return m
        }
        val parentId = parent.getAttribute("id")
        if (parentId != fragmentId) {
            return Mismatch { "Parent id $parentId doesn't match $fragmentId" }
        }
        val children = toDelete.childNodes
        var i = 0
        while (i < children.length) {
            val c = children.item(i)
            if (c is Element && containedIds.contains(c.getAttribute("id"))) {
                parent.insertBefore(c, toDelete)
            } else {
                i++
            }
        }
        toDelete.parentNode.removeChild(toDelete)
        ctx.log("Processed delete to $fragmentId")
        return null
    }

    protected fun same(expected: Element?, actual: Element?, ctx: CommandContext): Mismatch? {
        val expectedNoId = copyWithoutContainedIds(expected)
        val actualNoId = copyWithoutContainedIds(actual)
        return ctx.twoElementsAreIdentical(expectedNoId, actualNoId)
    }

    protected fun copyWithoutContainedIds(expected: Element?): Element? {
        if (expected == null) {
            return null
        }

        val out = expected.cloneNode(true) as Element
        var i = 0
        while (i < out.childNodes.length) {
            val n = out.childNodes.item(i)
            if (n is Element && containedIds.contains(n.getAttribute("id"))) {
                out.removeChild(n)
            } else {
                i++
            }
        }
        return out
    }

    protected fun doInsert(d: ADLDom, ctx: CommandContext): Mismatch? {
        val destination = getDestination(d, ctx)
        val before = getBefore(d, ctx)
        val contents = getContents(d, ctx)

        if (contents == null) {
            return null
        }

        val containsId = contents.getAttribute("id")
        if (alreadyExists(d, containsId)) {
            return Mismatch { "Already contains $containsId" }
        }
        if (destination == null) {
            return Mismatch { "Destination no longer exists" }
        }
        d.document.adoptNode(contents)
        if (before != null) {
            destination.insertBefore(contents, before)
        } else {
            destination.appendChild(contents)
        }
        ensureParentElements(destination, contents, ctx)

        // now make sure content is set
        val children = destination.childNodes
        val toMove: MutableList<Element> = ArrayList()
        for (i in 0 until children.length) {
            val c = children.item(i)
            if (c is Element && containedIds.contains(c.getAttribute("id"))) {
                toMove.add(c)
            }
        }
        for (element in toMove) {
            contents.appendChild(element)
        }
        ctx.log("Processed insert into $fragmentId")
        return null
    }

    private fun alreadyExists(d: ADLDom, newId: String): Boolean {
        return d.document.getElementById(newId) != null
    }

    protected fun getBefore(d: ADLDom, ctx: CommandContext): Element? {
        return if (beforeId != null) {
            findFragmentElement(d.document, beforeId!!, ctx)
        } else {
            null
        }
    }

    protected fun getDestination(d: ADLDom, ctx: CommandContext): Element {
        return findFragmentElement(d.document, fragmentId, ctx)
    }

    protected abstract fun getContents(d: ADLDom, ctx: CommandContext): Element?

    protected open fun checkProperties() {
        ensureNotNull("fragmentId", fragmentId)
        ensureNotNull("newId", fragmentId)
    }
}