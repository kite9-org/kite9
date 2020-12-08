package org.kite9.diagram.model

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.BorderTraversal

/**
 * Interface to say that this diagram element contains a
 * variable number of others rendered within it.  The size of the element is in large part dependent
 * therefore on the elements within it.
 *
 * Opposite of [Leaf]
 *
 * @author robmoffat
 */
interface Container : Rectangular {

    fun getContents(): List<DiagramElement>
    fun getLayout(): Layout?
    fun getTraversalRule(d: Direction): BorderTraversal
    fun getGridColumns(): Int
    fun getGridRows(): Int
}