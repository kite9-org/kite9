package org.kite9.diagram.dom.processors

import org.w3c.dom.Comment
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Text

/**
 * Does without copying, changing the elements in place.
 *
 * @author robmoffat
 */
abstract class AbstractInlineProcessor : AbstractProcessor() {

    override fun processContents(from: Node?): Node? {
        return processContents(from!!, null)
    }

    override fun processTag(n: Element): Element {
        return n
    }

    override fun processText(n: Text): Text {
        return n
    }

    override val isAppending: Boolean
        protected get() = false

    override fun processComment(c: Comment): Comment {
        return c
    }
}
