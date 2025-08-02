package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side


/**
 * This is a special anchor added to orbitals to block routes through a container along its intersection
 * as these would bisect elements within the container
 */
data class BlockAnchor(override val e: Rectangular, override val s: Side, override val permeability: Permeability) : PermeableAnchor