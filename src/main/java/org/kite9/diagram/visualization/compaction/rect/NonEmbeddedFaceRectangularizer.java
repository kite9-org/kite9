package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
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
		
		log.send("Extender: "+ro.getExtender()+" dir= "+ro.getExtender().getDirection());
		
		if (((PrioritisedRectOption) ro).isSizingSafe()) {
			// when sizing is safe, there are always pairs of options.  Make sure we use the one where the 
			// meets won't increase in length
			
			VertexTurn meets = ro.getMeets();
			int meetsMinimumLength = setMaxLengthWithMidpoint(meets, c);

			VertexTurn par = ro.getPar();
			int parMinimumLength = setMaxLengthWithMidpoint(par, c);
			
			
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

	private int setMaxLengthWithMidpoint(VertexTurn vt, Compaction c) {
		
		Rectangular r = getRectangular(vt);
		if ((r!= null) && (r.getSizing() == DiagramElementSizing.MINIMIZE)) {
			// ok, size is needed.
			
			int minimumLength = vt.getMinimumLength();
			vt.ensureMaxLength(minimumLength);
			return minimumLength;
		} 
		
		// no size needed
		return 10000;
		
//		OPair<Slideable<Segment>> startEnd;
//		SegmentSlackOptimisation so = (Direction.isVertical(vt.getDirection())) ? 
//			c.getHorizontalSegmentSlackOptimisation() : 
//			c.getVerticalSegmentSlackOptimisation();
//			
//		
//		startEnd = so.getSlideablesFor(r);
//		int minDist = startEnd.getA().minimumDistanceTo(startEnd.getB());	
//		so.ensureMaximumDistance(startEnd.getA(), startEnd.getB(), minDist);
	}
	
//	private void setMaxLengthWithMidpoint(VertexTurn vt, Compaction c) {
//		Rectangular r = getRectangular(vt);
//		OPair<Slideable<Segment>> startEnd;
//		SegmentSlackOptimisation so = (Direction.isVertical(vt.getDirection())) ? 
//			c.getHorizontalSegmentSlackOptimisation() : 
//			c.getVerticalSegmentSlackOptimisation();
//			
//		
//		startEnd = so.getSlideablesFor(r);
//		int minDist = startEnd.getA().minimumDistanceTo(startEnd.getB());	
//		so.ensureMaximumDistance(startEnd.getA(), startEnd.getB(), minDist);
//	}
	
	

	private Rectangular getRectangular(VertexTurn vt) {
		List<Rectangular> r = vt.getSegment().getUnderlyingInfo().stream().map(ui -> ui.getDiagramElement()).filter(de -> de instanceof Rectangular).map(de -> (Rectangular) de).collect(Collectors.toList());
	
		if (r.size() > 1) {
			throw new Kite9ProcessingException();
		} else if (r.size() == 0) {
			return null;
		}
		
		return r.get(0);
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