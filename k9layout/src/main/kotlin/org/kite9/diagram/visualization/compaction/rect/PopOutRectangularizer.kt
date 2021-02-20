package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.compaction.rect.NonEmbeddedFaceRectangularizer
import org.kite9.diagram.visualization.compaction.rect.PrioritisedRectOption

/**
 * HAndles the case where we want to 'pop out' the corner to avoid rectangularizing two
 * minimized sides against each other (causes sizing issues).
 *
 * @author robmoffat
 */
class PopOutRectangularizer(cd: CompleteDisplayer) : NonEmbeddedFaceRectangularizer(cd) {
    //	@Override
    //	protected void performChange(Compaction c, PriorityQueue<RectOption> pq, Set<VertexTurn> onStack, RectOption ro,
    //			List<VertexTurn> theStack) {
    //
    //		PrioritisedRectOption pro = (PrioritisedRectOption) ro;
    //		
    //		if (pro.getTurnShape() == TurnShape.U) {
    //			VertexTurn par = pro.getPar();
    //			VertexTurn meets = pro.getMeets();
    //			
    //			if ((par.getTurnPriority() == TurnPriority.MINIMIZE_RECTANGULAR) && (meets.getTurnPriority() == TurnPriority.MINIMIZE_RECTANGULAR)) {
    //
    //				// change the slideable on meets
    //				Dimension dimension = meets.getSegment().getDimension();
    //				Direction d = meets.getDirection();
    //				boolean horiz = dimension == Dimension.H;				
    //				Slideable<Segment> old = meets.getSlideable(); 
    //				SegmentSlackOptimisation sso = (SegmentSlackOptimisation) old.getSlackOptimisation();
    //				Slideable<Segment> bufferSlideable = new Slideable<Segment>(sso, null);
    //				sso.updateMaps(bufferSlideable);
    //				
    //				Slideable left = (d ==Direction.UP) || ( )
    //				
    //				sso.ensureMinimumDistance(left, right, minLength);
    //				
    //				meets.set
    //				
    //			
    //			}
    //			
    //			pq.g
    //		}
    //		
    //		super.performChange(c, pq, onStack, ro, theStack);
    //	}
    protected fun SidesMinimized(pro: PrioritisedRectOption?) {}
}