package org.kite9.diagram.model

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.BorderTraversal

/**
 * Marker interface for diagram elements which consume a rectangular space, and therefore
 * return width and height in [RectangleRenderingInformation].
 *
 * @author robmoffat
 */
interface Rectangular : Positioned {

    fun getContents(): List<DiagramElement>
    fun addTemporaryContent(t: Temporary)

    fun getTraversalRule(d: Direction): BorderTraversal

    /**
     * For the layout of contents within this rectangular
     */
    fun getLayout(): Layout?
    fun getGridColumns(): Int
    fun getGridRows(): Int

    /**
     * Returns true if this element or any of its children contains d
     */
    fun deepContains(d: DiagramElement) : Boolean
}