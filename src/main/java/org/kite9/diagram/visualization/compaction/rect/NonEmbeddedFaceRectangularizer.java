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

//	@Override
//	protected void afterChange(Compaction c, PriorityQueue<RectOption> pq, List<VertexTurn> theStack, int fromIndex) {
//		super.afterChange(c, pq, theStack, fromIndex);
//	}
//
	/**
	 * If we have a 'safe' rectangularization, make sure meets can't increase
	 */
	@Override
	protected boolean checkRectOptionIsOk(Set<VertexTurn> onStack, RectOption ro, PriorityQueue<RectOption> pq, Compaction c) {
		if (!super.checkRectOptionIsOk(onStack, ro, pq, c)) {
			return false;
		}
		
		log.send("Checking: "+ro);
		log.send("Extender: "+ro.getExtender()+" dir= "+ro.getTurnDirection(ro.getExtender()));
		
		if (((PrioritisedRectOption) ro).isSizingSafe()) {
			// when sizing is safe, there are always pairs of options.  Make sure we use the one where the 
			// meets won't increase in length
			
			VertexTurn meets = ro.getMeets();
			VertexTurn link = ro.getLink();
			int meetsMinimumLength = checkMinimumLength(meets, link, c, true);

			VertexTurn par = ro.getPar();
			int parMinimumLength = checkMinimumLength(par, link, c, true);
			
			
			if (meetsMinimumLength < parMinimumLength) {
				log.send("Not Allowing: "+meetsMinimumLength+" for meets="+meets+"\n         "+parMinimumLength+" for par="+par);
				return false;
			} 
			
			log.send("Allowing: "+meetsMinimumLength+" for meets="+meets+"\n         "+parMinimumLength+" for par="+par);	
			return true;
		} 

		log.send("Allowing: meets="+ro.getMeets()+"\n          for par="+ro.getPar());						
		return true;
	}

	private int checkMinimumLength(VertexTurn vt, VertexTurn link, Compaction c, boolean returnMinimum) {
		if (vt.getTurnPriority() == TurnPriority.MINIMIZE_RECTANGULAR) {
			if (shouldSetMidpoint(vt, link)) {
				Rectangular r = getRectangular(vt);
				// ok, size is needed of overall rectangle then half.
				boolean isHorizontal = !Direction.isHorizontal(vt.getDirection());
				OPair<Slideable<Segment>> perp = 
						(!isHorizontal ? c.getHorizontalSegmentSlackOptimisation() : c.getVerticalSegmentSlackOptimisation())
								.getSlideablesFor(r);
				
				OPair<Slideable<Segment>> along = 
						(isHorizontal ? c.getHorizontalSegmentSlackOptimisation() : c.getVerticalSegmentSlackOptimisation())
								.getSlideablesFor(r);
				
				
				alignConnections(c, perp, along);
				int sideSize = along.getA().minimumDistanceTo(along.getB());
				log.send("Setting size of "+r+" to "+sideSize+" "+vt.getDirection());
				along.getA().getSlackOptimisation().ensureMaximumDistance(along.getA(), along.getB(), sideSize);
				
				return vt.getMinimumLength();

			}

			if (returnMinimum) {
				// set to minimum possible size
				int minimumLength = vt.getMinimumLength();
				return minimumLength;			
			}
		}
		
		
		// no size needed
		return Integer.MAX_VALUE;
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
