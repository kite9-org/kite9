package org.kite9.diagram.visualization.compaction;

import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

public interface Compaction {

	public abstract Orthogonalization getOrthogonalization();

	public abstract List<Segment> getVerticalSegments();

	public abstract List<Segment> getHorizontalSegments();
	
	public Map<Vertex, Segment> getHorizontalVertexSegmentMap();

	public Map<Vertex, Segment> getVerticalVertexSegmentMap();
	
	/**
	 * For an internal face, returns the empty rectangle in the centre of the space that can
	 * be used to insert subface contents. 
	 * An external face returns the segments most extreme in each direction
	 * @return 4 item array of top, right, bottom, left segments.
	 */
	public Segment[] getFaceSpace(DartFace df);
	
	public void setFaceExtremeSections(DartFace df, Segment[] border);
	
	public Vertex createCompactionVertex(Segment s1, Segment s2);

	public Segment newSegment(PositionAction direction);
}