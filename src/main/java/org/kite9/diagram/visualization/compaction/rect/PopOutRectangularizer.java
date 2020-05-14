package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.rect.PrioritisedRectOption.TurnShape;
import org.kite9.diagram.visualization.compaction.rect.VertexTurn.TurnPriority;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

/** 
 * HAndles the case where we want to 'pop out' the corner to avoid rectangularizing two 
 * minimized sides against each other (causes sizing issues).
 * 
 * @author robmoffat
 *
 */
public class PopOutRectangularizer extends NonEmbeddedFaceRectangularizer {

	public PopOutRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}

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

	protected void SidesMinimized(PrioritisedRectOption pro) {
		
	}

	
}
