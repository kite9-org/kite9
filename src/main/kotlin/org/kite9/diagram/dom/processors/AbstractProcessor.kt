package org.kite9.diagram.dom.processors

import org.w3c.dom.*

abstract class AbstractProcessor : XMLProcessor {

    protected abstract val isAppending: Boolean

    fun processContents(n: Node, inside: Node?): Node? {
        //System.out.println("Process Contents : "+this.getClass()+ "    "+n.getLocalName()+"  inside "+inside);
        return if (n is Element) {
            val out = processTag(n)
            checkDoAppend(inside, out)
            val contents = n.childNodes
            mergeTextNodes(contents)
            processNodeList(contents, out)
            out
        } else if (n is Text) {
            val out = processText(n)
            checkDoAppend(inside, out)
            out
        } else if (n is Document) {
            val contents = n.childNodes
            processNodeList(contents, inside)
            inside
        } else if (n is Comment) {
            val out = processComment(n)
            checkDoAppend(inside, out)
            out
        } else {
            throw UnsupportedOperationException("Don't know how to handle $n")
        }
    }

    private fun checkDoAppend(inside: Node?, out: Node?) {
        if (isAppending && inside != null && out != null) {
            //System.out.println("Appending "+n);
            inside.appendChild(out)
        }
    }

    protected abstract fun processTag(n: Element): Element

    protected abstract fun processText(n: Text): Text

    protected abstract fun processComment(c: Comment): Comment

    protected open fun processNodeList(contents: NodeList, inside: Node?) {
        for (i in 0 until contents.length) {
            val n = contents.item(i)
            processContents(n!!, inside)
        }
    }

    protected fun mergeTextNodes(nodeList: NodeList) {
        var lastTextNode: Text? = null
        var i = 0
        while (i < nodeList.length) {
            val n = nodeList.item(i)
            if (n is Text) {
                if (lastTextNode != null) {
                    lastTextNode.data = lastTextNode.data + n.data
                    n.parentNode!!.removeChild(n)
                } else {
                    lastTextNode = n
                    i++
                }
            } else {
                lastTextNode = null
                i++
            }
        }
    }
}