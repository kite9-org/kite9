package org.kite9.diagram.visualization.compaction.slideable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.Side;
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo;
import org.kite9.framework.logging.Logable;

/**
 * Augments SlackOptimisation to keep track of segments underlying the slideables.
 * Also has a vertex-to-slideable map.
 * 
 * @author robmoffat
 * 
 */
public class SegmentSlackOptimisation extends AbstractSlackOptimisation<Segment> implements Logable {
	
	private Map<Vertex, Slideable<Segment>> vertexToSlidableMap = new HashMap<>();
	private Map<DiagramElement, OPair<Slideable<Segment>>> rectangularElementToSlideableMap = new HashMap<>();
	private Diagram theDiagram;
	
	private boolean isRectangular(DiagramElement underlying) {
		return (underlying instanceof Rectangular);
	}

	public SegmentSlackOptimisation(List<Segment> segments) {
		List<Slideable<Segment>> slideables = new ArrayList<>(segments.size());

		for (Segment s : segments) {
			Slideable<Segment> sli = new Slideable<Segment>(this, s);
			s.setSlideable(sli);
			log.send(log.go() ? null : "Created slideable: " + sli);
			slideables.add(sli);
		}

		pushCount = 0;
		addSlideables(slideables);

		initialiseSlackOptimisation();

	}
	
	protected void addedSlideable(Slideable<Segment> s) {
		// look for dependencies in the direction given
		// setupMinimumDistancesDueToDarts(s);
		updateMaps(s);
	}
	
	public void addSlideables(Slideable<Segment>... s) {
		for (Slideable<Segment> slideable : s) {
			updateMaps(slideable);
		}
	
		super.addSlideables(s);
	}
	
	public void addSlideables(Collection<Slideable<Segment>> s) {
		for (Slideable<Segment> slideable : s) {
			updateMaps(slideable);
		}
	
		super.addSlideables(s);
	}

	public void updateMaps(Slideable<Segment> s) {
		Segment seg = s.getUnderlying();
		
		for (Vertex v : seg.getVerticesInSegment()) {
			vertexToSlidableMap.put(v, s);
		} 
		
		for (UnderlyingInfo ui : seg.getUnderlyingInfo()) {
			DiagramElement underlying = ui.getDiagramElement();
			if (isRectangular(underlying)) {
				OPair<Slideable<Segment>> parts = rectangularElementToSlideableMap.get(underlying);
				if (parts == null) {
					parts = new OPair<Slideable<Segment>>(null, null);
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
	
	public Map<Vertex, Slideable<Segment>> getVertexToSlidableMap() {
		return vertexToSlidableMap;
	}

	@Override
	public String getIdentifier(Object underneath) {
		return ((Segment)underneath).getIdentifier();
	}
	

	public void initialiseSlackOptimisation() {
		OPair<Slideable<Segment>> diagramSlideables = rectangularElementToSlideableMap.get(theDiagram);
		diagramSlideables.getA().setMinimumPosition(0);
	}

	public Diagram getTheDiagram() {
		return theDiagram;
	}
	
	public OPair<Slideable<Segment>> getSlideablesFor(DiagramElement de) {
		return rectangularElementToSlideableMap.get(de);
	}

	public Collection<OPair<Slideable<Segment>>> getRectangularSlideablePairs() {
		return rectangularElementToSlideableMap.values();
	}	
	
}