package org.kite9.diagram.visualization.compaction.rect.second.popout

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.rect.VertexTurn
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.compaction.rect.second.NonEmbeddedFaceRectangularizer
import org.kite9.diagram.visualization.compaction.rect.second.prioritised.PrioritisedRectOption
import org.kite9.diagram.visualization.compaction.rect.second.prioritised.RectOption

/**
 * HAndles the case where we want to 'pop out' the corner to avoid rectangularizing two
 * minimized sides against each other (causes sizing issues).
 *
 * @author robmoffat
 */
class PopOutRectangularizer(cd: CompleteDisplayer) : NonEmbeddedFaceRectangularizer(cd) {

//    override fun performChange(
//        c: Compaction,
//        pq: PriorityQueue<RectOption>,
//        onStack: Set<VertexTurn>,
//        ro: RectOption,
//        theStack: List<VertexTurn>
//    ) {
//        val pro = ro as PrioritisedRectOption
//        if (pro.turnShape == PrioritisedRectOption.TurnShape.U) {
//
//            if ((pro.par.turnPriority == VertexTurn.TurnPriority.MINIMIZE_RECTANGULAR) &&
//                (pro.meets.turnPriority == VertexTurn.TurnPriority.MINIMIZE_RECTANGULAR)) {
//    				// change the slideable on meets, extender to bufferSlideable
//    				val dimension = pro.extender.slideable.dimension
//                    val oldSlideables = listOf(pro.post.slideable, pro.extender.slideable)
//    				val sso = pro.post.slideable.so
//    				val bufferSlideable = BufferSlideable(sso, dimension, oldSlideables)
//    				sso.updateMaps(bufferSlideable);
//
//                    // make sure everything is the right distance
//                    when (pro.meets.direction) {
//                        Direction.DOWN -> {
//                            sso.ensureMinimumDistance(bufferSlideable, pro.post.slideable, 0)
//                            sso.ensureMinimumDistance(bufferSlideable, pro.extender.slideable, 0)
//                        }
//                        Direction.UP, Direction.LEFT -> sso.ensureMaximumDistance()
//                    }
//    				val left = (d == Direction.UP) || ( )
//
//    				sso.ensureMinimumDistance(left, right, minLength);
//
//    				meets.set
//
//
//    			}
//
//    			pq.g
//    		}
//
//    		super.performChange(c, pq, onStack, ro, theStack);
//    	}
//
//    protected fun SidesMinimized(pro: PrioritisedRectOption?) {}
}