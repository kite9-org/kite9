package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail

/**
 * Contains code for managing/checking for contradictions in links.
 *
 * @author robmoffat
 */
interface ContradictionHandler {

    fun checkForContainerContradiction(c: Connection)

    fun checkContradiction(
        ad: Direction, aOrdering: Boolean,
        aRank: Int, ac: Iterable<BiDirectional<Connected>>, bd: Direction, bOrdering: Boolean, bRank: Int,
        bc: Iterable<BiDirectional<Connected>>, containerLayout: Layout
    ): Direction

    fun checkContradiction(
        ld1: LinkDetail, ld2: LinkDetail,
        containerLayout: Layout
    ): Direction

    fun setContradiction(bic: BiDirectional<Connected>, dontRender: Boolean)
    fun setContradicting(connections: Iterable<BiDirectional<Connected>>, dontRender: Boolean)
}