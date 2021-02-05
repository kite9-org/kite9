package org.kite9.diagram.visualization.compaction.align

import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Compaction

interface Aligner {

    /**
     * Applies alignment to all rectangulars along a the same given axis.
     * Each AlignedRectangular may have a different alignment, but they will have all passed
     * through willAlign with true.
     */
    fun alignFor(co: Container, de: Set<Rectangular>, c: Compaction, horizontal: Boolean)
    fun willAlign(de: Rectangular, horizontal: Boolean): Boolean
}