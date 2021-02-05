package org.kite9.diagram.common.elements.factory

import org.kite9.diagram.model.DiagramElement

/**
 * Allows for the conversion from an `X` (usually XML element) to a Kite9 `DiagramElement` (used in layout).
 *
 * @author robmoffat
 */
interface DiagramElementFactory<X> {

    fun createDiagramElement(x: X, parent: DiagramElement): DiagramElement

    fun createTemporaryConnected(parent: DiagramElement, idSuffix: String): TemporaryConnected

}