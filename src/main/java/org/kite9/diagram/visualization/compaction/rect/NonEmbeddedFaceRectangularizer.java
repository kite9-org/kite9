package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.rect.VertexTurn.TurnPriority;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.framework.common.Kite9ProcessingException;

public class NonEmbeddedFaceRectangularizer extends PrioritizingRectangularizer {

	public NonEmbeddedFaceRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}

	/**
	 * If we have a 'safe' rectangularization, make sure meets can't increase
	 */
	@Override
	protected Action checkRectOptionIsOk(Set<VertexTurn> onStack, RectOption ro, PriorityQueue<RectOption> pq, Compaction c) {
		Action superAction = super.checkRectOptionIsOk(onStack, ro, pq, c);
		
		if (superAction != Action.OK) {
			return superAction;
		}
		
		log.send("Checking: "+ro);
		log.send("Extender: "+ro.getExtender()+" dir= "+ro.getTurnDirection(ro.getExtender()));
		
//		if (((PrioritisedRectOption) ro).getType().isSafe()) {
//			// when sizing is safe, there are always pairs of options.  Make sure we use the one where the 
//			// meets won't increase in length
//			
//			VertexTurn meets = ro.getMeets();
//			VertexTurn link = ro.getLink();
//			VertexTurn par = ro.getPar();
//						
//			int meetsMinimumLength = checkMinimumLength(meets, link, c);
//
//			int parMinimumLength = checkMinimumLength(par, link, c);
////			
////			
////			if (meetsMinimumLength < parMinimumLength) {
////				log.send("Not Allowing: "+meetsMinimumLength+" for meets="+meets+"\n   "+parMinimumLength+" for par="+par);
////				return Action.PUT_BACK;
////			} 
////			
//			
//			
//			if (meetsMinimumLength < parMinimumLength) {
//				log.send("Not Allowing: "+meetsMinimumLength+" for meets="+meets+"\n   "+parMinimumLength+" for par="+par);
//				return Action.DISCARD;
//			} 
//			
//			
//			if ((ro.getScore() != ro.getInitialScore())) {
//				// change it and throw it back in - priority has changed.
//				log.send("Deferring: "+meetsMinimumLength+" for meets="+meets+"\n         "+parMinimumLength+" for par="+par);
//				return Action.PUT_BACK;
//			} else {
//				log.send("Allowing: "+meetsMinimumLength+" for meets="+meets+"\n         "+parMinimumLength+" for par="+par);
//				return Action.OK;
//			}
//		} 

		log.send("Allowing: meets="+ro.getMeets()+"\n          for par="+ro.getPar());						
		return Action.OK;
	}

	private int checkMinimumLength(VertexTurn vt, VertexTurn link, Compaction c) {
		if (vt.getTurnPriority() == TurnPriority.MINIMIZE_RECTANGULAR) {
			Rectangular r = getRectangular(vt);
			// ok, size is needed of overall rectangle then half.
			boolean isHorizontal = !Direction.isHorizontal(vt.getDirection());

			OPair<Slideable<Segment>> along = 
					(isHorizontal ? c.getHorizontalSegmentSlackOptimisation() : c.getVerticalSegmentSlackOptimisation())
							.getSlideablesFor(r);

			
			if (shouldSetMidpoint(vt, link)) {
				OPair<Slideable<Segment>> perp = 
						(!isHorizontal ? c.getHorizontalSegmentSlackOptimisation() : c.getVerticalSegmentSlackOptimisation())
								.getSlideablesFor(r);
				
				alignSingleConnections(c, perp, along);
			}

			int sideSize = along.getA().minimumDistanceTo(along.getB());
			log.send("Setting size of "+r+" to "+sideSize+" "+vt.getDirection());
			along.getA().getSlackOptimisation().ensureMaximumDistance(along.getA(), along.getB(), sideSize);
		}
		
		return vt.getMinimumLength();
	}
		
	private boolean shouldSetMidpoint(VertexTurn vt, VertexTurn link) {
		if (hasConnected(vt)) {
			if (link.getSegment().getConnections().size() == 1) {
				Set<Connection> leavingConnections = vt.getLeavingConnections();
				if (leavingConnections.size() == 1) {
					if (link.getSegment().getConnections().containsAll(leavingConnections)) {
						return true;
					}
				}
			}
		}
		
		return false;
		
	}

	private boolean hasConnected(VertexTurn vt) {
		return vt.getSegment().getRectangulars().stream().filter(r -> r instanceof Connected).count() > 0;
	}

	private Rectangular getRectangular(VertexTurn vt) {
		Set<Rectangular> r = vt.getSegment().getRectangulars();
	
		if (r.size() > 1) {
			throw new Kite9ProcessingException();
		} else if (r.size() == 0) {
			return null;
		}
		
		return r.iterator().next();
	}
	
	@Override
	public void compact(Compaction c, Embedding r, Compactor rc) {
		log.send("Rectangularizing Outer Faces Of: "+r);
		super.compact(c, r, rc);
	}

	@Override
	protected List<DartFace> selectFacesToRectangularize(List<DartFace> faces) {
		return faces.stream().filter(df -> df.getContainedFaces().size()==0).collect(Collectors.toList());
	}
}
