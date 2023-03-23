package org.kite9.diagram.visualization.orthogonalization.contents

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

/**
 * Converts the contents of a vertex or a label into inner/outer faces.
 *
 * @author robmoffat
 */
interface ContentsConverter {

    fun convertDiagramElementToInnerFace(original: DiagramElement, o: Orthogonalization): DartFace
    fun convertToOuterFace(o: Orthogonalization, startVertex: Vertex, partOf: Rectangular): DartFace
}