package org.kite9.diagram.visualization.compaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Rectangle;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
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
	private Map<Rectangular, List<DartFace>> facesForRectangular;
	private Map<Dart, Segment> dartToSegmentMap;

	public CompactionImpl(Orthogonalization o, List<Segment> horizontal, List<Segment> vertical, Map<Vertex, Segment> hmap, Map<Vertex, Segment> vmap, Map<Rectangular, List<DartFace>> facesForRectangular, Map<Dart, Segment> dartToSegmentMap) {
		this.orthogonalization = o;
		this.horizontalSegments = horizontal;
		this.verticalSegments = vertical;
		this.hMap = hmap;
		this.vMap = vmap;
		this.facesForRectangular = facesForRectangular;
		this.dartToSegmentMap = dartToSegmentMap;		
		this.xSlackOptimisation = new SegmentSlackOptimisation(horizontal);
		this.ySlackOptimisation = new SegmentSlackOptimisation(vertical);
	}
	
	private final SegmentSlackOptimisation xSlackOptimisation; 
	private final SegmentSlackOptimisation ySlackOptimisation;
	
	public SegmentSlackOptimisation getXSlackOptimisation() {
		return xSlackOptimisation;
	}

	public SegmentSlackOptimisation getYSlackOptimisation() {
		return ySlackOptimisation;
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

	public Vertex createCompactionVertex(Segment s1, Segment s2) {
		Vertex v = orthogonalization.createHelperVertex();
		s1.addToSegment(v);
		s2.addToSegment(v);
		//System.out.println("Added vertex "+v+" to "+s1+" and "+s2);
		setupVertex(v, s1);
		setupVertex(v, s2);
		
		return v;
	}
	
	private void setupVertex(Vertex v, Segment s) {
		if (s.getDimension()==PositionAction.XAction) {
			vMap.put(v, s);
		} else {
			hMap.put(v, s);
		}
		
	}

	public Segment newSegment(PositionAction direction) {
		Segment snew = null;
		if (direction==PositionAction.XAction) {
			snew = new Segment(direction, getVerticalSegments().size());
			getVerticalSegments().add(snew);
		} else {
			snew = new Segment(direction, getHorizontalSegments().size());
			getHorizontalSegments().add(snew);
		}
		
		return snew;
	}

	@Override
	public List<DartFace> getDartFacesForRectangular(Rectangular r) {
		return facesForRectangular.get(r);
	}
	
	@Override
	public Segment getSegmentForDart(Dart r) {
		return dartToSegmentMap.get(r);
	}

	@Override
	public SegmentSlackOptimisation getSlackOptimisation(Direction d) {
		if ((d==Direction.UP) || (d == Direction.DOWN)) {
			return getYSlackOptimisation();
		} else {
			return getXSlackOptimisation();
		}
	}
}
