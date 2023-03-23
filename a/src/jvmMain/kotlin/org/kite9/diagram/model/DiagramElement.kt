package org.kite9.diagram.model

import org.kite9.diagram.common.algorithms.det.Deterministic
import org.kite9.diagram.model.position.RenderingInformation

/**
 * Parent class for all elements of the diagram.
 */
interface DiagramElement : Comparable<DiagramElement>, Deterministic {

    /**
     * Returns the parent element, or null if there is no parent.
     */
    fun getParent(): DiagramElement?

    fun getRenderingInformation(): RenderingInformation

    /**
     * Returns the number of levels deep which this element is embedded in the diagram hierarchy, with zero the top level.
     */
    fun getDepth(): Int

    /**
     * Similar to parent element, but sometimes the parent doesn't do the containing.  e.g. with a terminator, the
     * container will be the thing the terminator is in, whereas the parent will be the link.
     */
    fun getContainer() : Container?
}