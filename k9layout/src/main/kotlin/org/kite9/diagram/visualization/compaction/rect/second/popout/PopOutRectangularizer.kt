package org.kite9.diagram.visualization.compaction.rect.second.popout

import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
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
//            val par = pro.par;
//            val meets = pro.meets;
//
//            if ((par.turnPriority == VertexTurn.TurnPriority.MINIMIZE_RECTANGULAR) &&
//                (meets.turnPriority == VertexTurn.TurnPriority.MINIMIZE_RECTANGULAR)) {
//
//    				// change the slideable on meets
//    				Dimension dimension = meets.getSegment().getDimension();
//    				Direction d = meets.getDirection();
//    				boolean horiz = dimension == Dimension.H;
//    				Slideable old = meets.getSlideable();
//    				SegmentSlackOptimisation sso = (SegmentSlackOptimisation) old.getSlackOptimisation();
//    				Slideable bufferSlideable = new Slideable(sso, null);
//    				sso.updateMaps(bufferSlideable);
//
//    				Slideable left = (d ==Direction.UP) || ( )
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

    protected fun SidesMinimized(pro: PrioritisedRectOption?) {}
}