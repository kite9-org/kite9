package org.kite9.diagram.visualization.compaction.position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation;
import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.framework.logging.Logable;

/**
 * Augments SlackOptimisation to keep track of segments underlying the slideables.
 * Also has a vertex-to-slideable map.
 * 
 * @author robmoffat
 * 
 */
public class SegmentSlackOptimisation extends AbstractSlackOptimisation<Segment> implements Logable {

	Map<Vertex, Slideable> vertexToSlidableMap;

	public SegmentSlackOptimisation(List<Segment> segments, Direction d) {
		this.d = d;
		List<Slideable> slideables = new ArrayList<Slideable>(segments.size());

		for (Segment s : segments) {
			Slideable sli = new Slideable(this, s, getSegmentAlignStyle(s));
			log.send(log.go() ? null : "Created slideable: " + sli);
			slideables.add(sli);
		}

		this.canonicalOrder = slideables;
		pushCount = 0;

		createVertexSlidableMap();

		for (Slideable s : canonicalOrder) {
			ensureMinimumDistances(s, true);
		}

		updateCanonicalOrdering();

		initialiseSlackOptimisation();

	}
	
	protected void ensureMinimumDistances(Slideable s, boolean push) {
		// look for dependencies in the direction given
		for (Vertex v : ((Segment)s.getUnderlying()).getVerticesInSegment()) {
			for (Edge e : v.getEdges()) {
				if (e instanceof Dart) {
					if (e.getDrawDirectionFrom(v) == d) {
						// need to create a dependency for this dart
						Slideable other = vertexToSlidableMap.get(e.otherEnd(v));
						ensureMinimumDistance(s, other, (int) ((Dart) e).getLength(), push);
					}
				}
			}
		}
	}
	
	public void addSlideables(Slideable... s) {
		for (Slideable slideable : s) {
			updateVertexSlidableMap(slideable);
		}

		super.addSlideables(s);
	}
	
	protected void createVertexSlidableMap() {
		vertexToSlidableMap = new HashMap<Vertex, Slideable>();
		for (Slideable s : canonicalOrder) {
			updateVertexSlidableMap(s);
		}
	}

	public void updateVertexSlidableMap(Slideable s) {
		Segment segment = (Segment) s.getUnderlying();
		segment.setPositioned(false);
		for (Vertex v : segment.getVerticesInSegment()) {
			vertexToSlidableMap.put(v, s);
		}
	}
	
	public AlignStyle getSegmentAlignStyle(Segment s) {
		Direction us = s.getUnderlyingSide();
		if (us == null) {
			return AlignStyle.CENTER;
		} else {
			switch (us) {
			case UP:
			case LEFT:
				return AlignStyle.LEFT;
			case DOWN:
			case RIGHT:
				return AlignStyle.RIGHT;
			}
		}
		
		return AlignStyle.LEFT;
	}
	
	public Map<Vertex, Slideable> getVertexToSlidableMap() {
		return vertexToSlidableMap;
	}

	@Override
	public String getIdentifier(Object underneath) {
		return ((Segment)underneath).getIdentifier();
	}
}