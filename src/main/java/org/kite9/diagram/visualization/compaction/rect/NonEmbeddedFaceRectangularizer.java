package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

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
		log.send("Extender: "+ro.getExtender()+" dir= "+ro.getExtender().getDirection());
		
		if (((PrioritisedRectOption) ro).isSizingSafe()) {
			// when sizing is safe, there are always pairs of options.  Make sure we use the one where the 
			// meets won't increase in length
			
			VertexTurn meets = ro.getMeets();
			VertexTurn link = ro.getLink();
			int meetsMinimumLength = setMaxLengthWithMidpoint(meets, link, c);

			VertexTurn par = ro.getPar();
			int parMinimumLength = setMaxLengthWithMidpoint(par, link, c);
			
			
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

	private int setMaxLengthWithMidpoint(VertexTurn vt, VertexTurn link, Compaction c) {
		Rectangular r = getRectangular(vt);
		
		
		if ((r!= null) && (r.getSizing() == DiagramElementSizing.MINIMIZE)) {
			if (shouldSetMidpoint(vt, link)) {
				// ok, size is needed of overall rectangle then half.
				boolean isHorizontal = !Direction.isHorizontal(vt.getDirection());
				OPair<Slideable<Segment>> limits = 
						(isHorizontal ? c.getHorizontalSegmentSlackOptimisation() : c.getVerticalSegmentSlackOptimisation())
								.getSlideablesFor(r);
						
				int rectangleSize = limits.getA().minimumDistanceTo(limits.getB());	
				int half = (int) Math.ceil(rectangleSize / 2f);
				rectangleSize = half * 2; // to avoid rounding errors
				log.send("Setting size of "+r+" to "+rectangleSize+" "+vt.getDirection());
				AbstractSlackOptimisation<Segment> so = limits.getA().getSlackOptimisation();
				so.ensureMaximumDistance(limits.getA(), limits.getB(), rectangleSize);
				Slideable<Segment> linkSlideable = link.getSlideable();
				so.ensureMinimumDistance(limits.getA(), linkSlideable, half);
				so.ensureMinimumDistance(linkSlideable, limits.getB(), half);				
				return half;
			} 
			
			
			// set to minimum possible size
			int minimumLength = vt.getMinimumLength();
			vt.ensureMaxLength(minimumLength);
			return minimumLength;
			
			
		} 
		
		// no size needed
		return 10000;
	}
		
	private boolean shouldSetMidpoint(VertexTurn vt, VertexTurn link) {
		if (link.getSegment().getConnections().size() == 1) {
			Set<Connection> leavingConnections = vt.getLeavingConnections();
			if (leavingConnections.size() == 1) {
				if (link.getSegment().getConnections().containsAll(leavingConnections)) {
					return true;
				}
			}
		}
		
		return false;
		
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
