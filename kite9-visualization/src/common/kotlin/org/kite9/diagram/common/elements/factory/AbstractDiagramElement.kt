package org.kite9.diagram.common.elements.factory

import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement

abstract class AbstractDiagramElement(private val p: DiagramElement?) : DiagramElement {

    override fun compareTo(other: DiagramElement): Int {
        return getID().compareTo(other.getID())
    }

    override fun hashCode(): Int {
        return _hc;
    }

    private var depth = -1

    private val _hc : Int by lazy { getID().hashCode() }

    override fun getDepth(): Int {
        if (depth == -1) {
            depth = if ((this is Diagram) || (getParent() == null)){
                0
            } else {
                getParent()!!.getDepth()  +1
            }
        }
        return depth
    }

    /**
     * Used to keep track of any connections we find in the diagram during initialisation
     */
    protected open fun registerConnection(de: Connection) {
        (getParent() as AbstractDiagramElement).registerConnection(de)
    }

    override fun getParent(): DiagramElement? {
        return p
    }
}