package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction2.C2Slideable

class C2Point(a: C2Slideable, b: C2Slideable, val d: Direction) : Pair<C2Slideable>(a, b) {

    fun get(d: Dimension) : C2Slideable {
        return if (a.dimension == d) {
            a;
        } else if (b.dimension == d) {
            b;
        } else {
            throw LogicException("Couldn't find slideable in dimension $d")
        }
    }

}