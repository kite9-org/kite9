package org.kite9.diagram.visualization.compaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Rectangle;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 * Flyweight class that handles the state of the compaction as it goes along.
 * Contains lots of utility methods too.
 * 
 * 
 * @author robmoffat
 *
 */
public class CompactionImpl implements Compaction {
	
	private Orthogonalization orthogonalization;
	
	public Orthogonalization getOrthogonalization() {
		return orthogonalization;
	}

	public List<Segment> getVerticalSegments() {
		return verticalSegments;
	}

	public List<Segment> getHorizontalSegments() {
		return horizontalSegments;
	}

	private List<Segment> verticalSegments;
	private List<Segment> horizontalSegments;
	private Map<Dart, Segment> dartToSegmentMap;
	private Embedding topEmbedding;

	public CompactionImpl(Orthogonalization o, List<Segment> horizontal, List<Segment> vertical, Map<Vertex, Segment> hmap, Map<Vertex, Segment> vmap, Map<Dart, Segment> dartToSegmentMap, Embedding topEmbedding) {
		this.orthogonalization = o;
		this.horizontalSegments = horizontal;
		this.verticalSegments = vertical;
		this.hMap = hmap;
		this.vMap = vmap;
		this.dartToSegmentMap = dartToSegmentMap;		
		this.horizontalSegmentSlackOptimisation = new SegmentSlackOptimisation(horizontal);
		this.verticalSegmentSlackOptimisation = new SegmentSlackOptimisation(vertical);
		this.topEmbedding = topEmbedding;
	}
	
	private final SegmentSlackOptimisation horizontalSegmentSlackOptimisation; 
	private final SegmentSlackOptimisation verticalSegmentSlackOptimisation;
	
	public SegmentSlackOptimisation getHorizontalSegmentSlackOptimisation() {
		return horizontalSegmentSlackOptimisation;
	}

	public SegmentSlackOptimisation getVerticalSegmentSlackOptimisation() {
		return verticalSegmentSlackOptimisation;
	}

	private Map<Vertex, Segment> hMap;

	public Map<Vertex, Segment> getHorizontalVertexSegmentMap() {
		return hMap;
	}

	public Rectangle<Slideable<Segment>> getFaceSpace(DartFace df) {
		Rectangle<Slideable<Segment>> out = faceSpaces.get(df);
		return out;
	}
	
	private Map<Vertex, Segment> vMap;

	public Map<Vertex, Segment> getVerticalVertexSegmentMap() {
		return vMap;
	}

	private Map<DartFace, Rectangle<Slideable<Segment>>> faceSpaces = new HashMap<>();
	
	public void createFaceSpace(DartFace df, Rectangle<Slideable<Segment>> border) {
		faceSpaces.put(df, border);
	}
	
	public void setFaceSpaceToDone(DartFace df) {
		faceSpaces.put(df, DONE);
	}


	public Segment newSegment(Dimension direction) {
		Segment snew = null;
		if (direction==Dimension.V) {
			snew = new Segment(direction, getVerticalSegments().size());
			getVerticalSegments().add(snew);
		} else {
			snew = new Segment(direction, getHorizontalSegments().size());
			getHorizontalSegments().add(snew);
		}
		
		return snew;
	}

	@Override
	public Segment getSegmentForDart(Dart r) {
		return dartToSegmentMap.get(r);
	}

	@Override
	public SegmentSlackOptimisation getSlackOptimisation(boolean horizontal) {
		if (horizontal) {
			return getHorizontalSegmentSlackOptimisation();
		} else {
			return getVerticalSegmentSlackOptimisation();
		} 
	}

	@Override
	public Embedding getTopEmbedding() {
		return topEmbedding;
	}
}
