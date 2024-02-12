package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.compaction2.*

interface SlideableSet<X : SlideableSet<X>> {
    fun getAll() : Set<C2Slideable>

    fun replaceRectangular(s: C2RectangularSlideable, with: C2RectangularSlideable) : X

    fun replaceIntersection(s: C2IntersectionSlideable, with: C2IntersectionSlideable) : X

    fun replaceGeneric(s: C2Slideable, with: C2Slideable) : X {
        return if ((s is C2IntersectionSlideable) && (with is C2IntersectionSlideable)) {
            replaceIntersection(s, with)
        } else if ((s is C2RectangularSlideable) && (with is C2RectangularSlideable)) {
            replaceRectangular(s, with)
        } else {
            throw LogicException("Can only replace slideable with same type $s $with")
        }
    }

    fun getBufferSlideables() : Set<C2BufferSlideable>

    val done : Boolean

    val number : Int
}