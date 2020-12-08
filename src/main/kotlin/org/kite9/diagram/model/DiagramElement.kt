package org.kite9.diagram.model

import org.kite9.diagram.model.position.RenderingInformation

/**
 * Parent class for all elements of the diagram.
 */
interface DiagramElement : Comparable<DiagramElement?> {

    /**
     * Returns the parent element, or null if there is no parent.
     */
    fun getParent(): DiagramElement?

    /**
     * ID should be a project-unique ID to describe this element.  It is also used within the
     * XML to allow references between the elements of the XML file.
     *
     * ID is also used for hashcode and equals.  Set an ID to ensure sorting, maps
     * and therefore diagram layouts, are deterministic.
     *
     * IDs are expected for most elements, but are optional.
     *
     */
    fun getID(): String

    val renderingInformation: RenderingInformation

    /**
     * Returns the number of levels deep which this element is embedded in the diagram hierarchy, with zero the top level.
     */
    fun getDepth(): Int
}