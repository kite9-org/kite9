package org.kite9.diagram.visualization.compaction2.align

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction2.C2Compaction

interface Aligner {

    /**
     * Applies alignment to all rectangulars along a the same given axis.
     * Each AlignedRectangular may have a different alignment, but they will have all passed
     * through willAlign with true.
     */
    fun alignFor(co: Container, de: Set<Rectangular>, c: C2Compaction, d: Dimension)
    fun willAlign(de: Rectangular, d: Dimension): Boolean
}