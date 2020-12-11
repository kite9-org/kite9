package org.kite9.diagram.common.algorithms.det

/**
 * Indicates that hashCode has been overridden to ensure deterministic operation.
 * i.e. that the hash of the object depends entirely on the object state.
 *
 * @author robmoffat
 */
interface Deterministic {

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

}