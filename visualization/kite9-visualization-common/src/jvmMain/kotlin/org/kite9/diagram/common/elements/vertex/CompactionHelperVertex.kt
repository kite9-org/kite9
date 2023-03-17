package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.model.DiagramElement

/**
 * Vertex inserted to aid the compaction process.  Typically added during rectangularization.
 *
 * @author robmoffat
 */
class CompactionHelperVertex(val id: String) : AbstractVertex(id), NoElementVertex {

    override fun isPartOf(de: DiagramElement?): Boolean {
        return false
    }

    override fun getDiagramElements(): Set<DiagramElement> {
        return emptySet()
    }
}