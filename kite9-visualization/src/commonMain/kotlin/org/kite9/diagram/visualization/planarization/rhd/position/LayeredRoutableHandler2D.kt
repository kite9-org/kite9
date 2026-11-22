package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

interface LayeredRoutableHandler2D : RoutableHandler2D {

    /**
     * Marks a set of leaf groups as occupying the same position in a given dimension.
     */
    fun createLayer(groupsInLayer: Set<Group>, d: Dimension)

    /**
     * Ensures the layers containing any of g1 and g2 are separated.
     */
    fun orderLayers(g1: Set<Group>, g2: Set<Group>, d: Dimension)
}