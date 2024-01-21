package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet

/**
 * Flyweight class that handles the state of the compaction as it goes along.
 * Contains lots of utility methods too.
 *
 *
 * @author robmoffat
 */
class C2CompactionImpl(private val diagram: Diagram) : C2Compaction {

    private val horizontalSegmentSlackOptimisation = C2SlackOptimisation(this)
    private val verticalSegmentSlackOptimisation = C2SlackOptimisation(this)

    override fun getSlackOptimisation(d: Dimension): C2SlackOptimisation {
        return if (d ==Dimension.H) {
            horizontalSegmentSlackOptimisation
        } else {
            verticalSegmentSlackOptimisation
        }
    }

    override fun getDiagram(): Diagram {
        return diagram
    }

    private val junctions = mutableMapOf<C2BufferSlideable, C2Slideable>()

//    override fun recordJunctions(r1: RectangularSlideableSet, r2: RectangularSlideableSet) {
//        TODO("Not yet implemented")
//    }
//
    override fun replaceJunction(s1: C2Slideable, s2: C2Slideable, sNew: C2Slideable) {
    }

}