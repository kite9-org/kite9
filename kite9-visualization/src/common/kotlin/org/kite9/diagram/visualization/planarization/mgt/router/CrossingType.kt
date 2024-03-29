package org.kite9.diagram.visualization.planarization.mgt.router

/**
 * Describes how we insert the edge, wrt to the direction the edge has been given.
 */
enum class CrossingType {
    STRICT, NOT_BACKWARDS, UNDIRECTED
}