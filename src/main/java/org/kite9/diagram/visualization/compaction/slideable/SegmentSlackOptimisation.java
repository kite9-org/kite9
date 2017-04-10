package org.kite9.diagram.visualization.compaction.slideable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation;
import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.Side;
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo;
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
			s.setSlideable(sli);
			log.send(log.go() ? null : "Created slideable: " + sli);
			slideables.add(sli);
		}

		pushCount = 0;
		addSlideables(slideables);

		initialiseSlackOptimisation();

	}
	
	protected void addedSlideable(Slideable s) {
		// look for dependencies in the direction given
		// setupMinimumDistancesDueToDarts(s);
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
		
		for (Vertex v : seg.getVerticesInSegment()) {
			vertexToSlidableMap.put(v, s);
		} 
		
		for (UnderlyingInfo ui : seg.getUnderlyingInfo()) {
			DiagramElement underlying = ui.getDiagramElement();
			if (isRectangular(underlying)) {
				OPair<Slideable> parts = rectangularElementToSlideableMap.get(underlying);
				if (parts == null) {
					parts = new OPair<Slideable>(null, null);
				}
				
				if ((ui.getSide() == Side.START)) {
					parts = new OPair<>(s, parts.getB());
				} else if (ui.getSide() == Side.END) {
					parts = new OPair<>(parts.getA(), s);
				}
				
				rectangularElementToSlideableMap.put(underlying, parts);
				
				if (underlying instanceof Diagram) {
					theDiagram = (Diagram) underlying;
				}
			}
		}
	}
	
	public AlignStyle getSegmentAlignStyle(Segment s) {
		if (s.getUnderlyingInfo().size() == 1) {
			UnderlyingInfo ui = s.getUnderlyingInfo().iterator().next();
			switch (ui.getSide()) {
			case END:
				return AlignStyle.RIGHT;
			case START:
				return AlignStyle.LEFT;
			default:
				return AlignStyle.CENTER;
			}
		} else {
			return AlignStyle.CENTER;
		}
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