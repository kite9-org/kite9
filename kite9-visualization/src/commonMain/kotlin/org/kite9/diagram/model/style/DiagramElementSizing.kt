package org.kite9.diagram.model.style

/**
 * This is used to choose the right approach for laying out the diagram element.
 *
 * When elements are part of a grid, MINIMIZE has priority over MAXIMIZE.
 *
 * @author robmoffat
 */
enum class DiagramElementSizing {
    MINIMIZE, MAXIMIZE
}