package org.kite9.diagram.visualization.compaction.position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation;
import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.objects.OPair;
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
	
	private Map<Vertex, Slideable> vertexToSlidableMap = new HashMap<>();
	private Map<DiagramElement, OPair<Slideable>> rectangularElementToSlideableMap = new HashMap<>();
	private Diagram theDiagram;
	
	private boolean isRectangular(DiagramElement underlying) {
		return (underlying instanceof Leaf) || (underlying instanceof Connected) || (underlying instanceof Label);
	}

	public SegmentSlackOptimisation(List<Segment> segments, Direction d) {
		this.d = d;
		List<Slideable> slideables = new ArrayList<Slideable>(segments.size());

		for (Segment s : segments) {
			Slideable sli = new Slideable(this, s, getSegmentAlignStyle(s));
			log.send(log.go() ? null : "Created slideable: " + sli);
			slideables.add(sli);
			
			if (s.getUnderlying() instanceof Diagram) {
				theDiagram = (Diagram) s.getUnderlying();
			}
		}

		pushCount = 0;
		addSlideables(slideables);

		initialiseSlackOptimisation();

	}
	
	protected void addedSlideable(Slideable s) {
		// look for dependencies in the direction given
		setupMinimumDistancesDueToDarts(s);
		updateMaps(s);
	}
	
	

	private void setupMinimumDistancesDueToDarts(Slideable s) {
		for (Vertex v : ((Segment)s.getUnderlying()).getVerticesInSegment()) {
			for (Edge e : v.getEdges()) {
				if (e instanceof Dart) {
					if (e.getDrawDirectionFrom(v) == d) {
						// need to create a dependency for this dart
						Slideable other = vertexToSlidableMap.get(e.otherEnd(v));
						ensureMinimumDistance(s, other, (int) ((Dart) e).getLength());
					}
				}
			}
		}
	}
	
	public void addSlideables(Slideable... s) {
		for (Slideable slideable : s) {
			updateMaps(slideable);
		}
	
		super.addSlideables(s);
	}
	
	public void addSlideables(Collection<Slideable> s) {
		for (Slideable slideable : s) {
			updateMaps(slideable);
		}
	
		super.addSlideables(s);
	}

	public void updateMaps(Slideable s) {
		Segment seg = (Segment) s.getUnderlying();
		seg.setPositioned(false);
		DiagramElement underlying = seg.getUnderlying();
		for (Vertex v : seg.getVerticesInSegment()) {
			vertexToSlidableMap.put(v, s);

			if (isRectangular(underlying)) {
				OPair<Slideable> parts = rectangularElementToSlideableMap.get(underlying);
				if (parts == null) {
					parts = new OPair<Slideable>(null, null);
				}
				
				if ((seg.getUnderlyingSide() == Direction.LEFT) || (seg.getUnderlyingSide() == Direction.UP)) {
					parts = new OPair<>(s, parts.getB());
				} else if ((seg.getUnderlyingSide() == Direction.RIGHT) || (seg.getUnderlyingSide() == Direction.DOWN)) {
					parts = new OPair<>(parts.getA(), s);
				}
				
				rectangularElementToSlideableMap.put(underlying, parts);
			}
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
	

	public void initialiseSlackOptimisation() {
		OPair<Slideable> diagramSlideables = rectangularElementToSlideableMap.get(theDiagram);
		diagramSlideables.getA().setMinimumPosition(0);
	}

	public Diagram getTheDiagram() {
		return theDiagram;
	}
	
	public OPair<Slideable> getSlideablesFor(DiagramElement de) {
		return rectangularElementToSlideableMap.get(de);
	}

	public Collection<OPair<Slideable>> getRectangularSlideablePairs() {
		return rectangularElementToSlideableMap.values();
	}
}