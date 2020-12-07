package org.kite9.diagram.visualization.compaction;

import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Rectangle;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

public interface Compaction {

	public SegmentSlackOptimisation getHorizontalSegmentSlackOptimisation();

	public SegmentSlackOptimisation getVerticalSegmentSlackOptimisation();
	
	public SegmentSlackOptimisation getSlackOptimisation(boolean horizontal);

	public abstract Orthogonalization getOrthogonalization();

	public abstract List<Segment> getVerticalSegments();

	public abstract List<Segment> getHorizontalSegments();
	
	public Map<Vertex, Segment> getHorizontalVertexSegmentMap();

	public Map<Vertex, Segment> getVerticalVertexSegmentMap();
	
	public static final Rectangle<FaceSide> DONE = new Rectangle<>(null, null, null, null);
	
	/**
	 * For an internal face, returns the empty rectangle in the centre of the space that can
	 * be used to insert subface contents. 
	 * 
	 * Rectangle is in top, right, bottom, left order.
	 */
	public Rectangle<FaceSide> getFaceSpace(DartFace df);
	
	public void createFaceSpace(DartFace df, Rectangle<FaceSide> r);
	
	public void setFaceSpaceToDone(DartFace df);
	
	public Segment getSegmentForDart(Dart d);
	
	public Embedding getTopEmbedding();
	
	public Segment newSegment(Dimension d);

}