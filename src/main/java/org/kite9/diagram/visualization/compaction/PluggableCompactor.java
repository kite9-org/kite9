package org.kite9.diagram.visualization.compaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.SegmentBuilder;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.framework.common.HelpMethods;


/**
 * Superclass for all segment-based compaction methods.   The process works by converting each face into
 * a set of segments, where segments represent horizontal or vertical positions.  
 *  
 * 
 * @author robmoffat
 *
 */
public class PluggableCompactor implements Compactor {

	public PluggableCompactor(CompactionStep[] steps) {
		this.steps = steps;
	}
	
	protected SegmentBuilder sb = new SegmentBuilder();
	protected CompactionStep[] steps;

	public static final Set<Direction> VERTICAL = HelpMethods.createSet(Direction.UP, Direction.DOWN);
	public static final Set<Direction> HORIZONTAL = HelpMethods.createSet(Direction.LEFT, Direction.RIGHT);
	
	public Compaction compactDiagram(Orthogonalization o) {
		List<Segment> horizontal = buildSegmentList(o, HORIZONTAL);
		List<Segment> vertical = buildSegmentList(o, VERTICAL);
		Map<Dart, Segment> dartToSegmentMap = calculateDartToSegmentMap(horizontal, vertical);
		Map<Vertex, Segment> horizontalSegmentMap = createVertexSegmentMap(horizontal);
		Map<Vertex, Segment> verticalSegmentMap = createVertexSegmentMap(vertical);
		Compaction compaction = instantiateCompaction(o, horizontal, vertical, dartToSegmentMap, horizontalSegmentMap, verticalSegmentMap);
		compact(o.getPlanarization().getDiagram(), compaction);
		return compaction;
	}

	protected Compaction instantiateCompaction(Orthogonalization o, List<Segment> horizontal, List<Segment> vertical, Map<Dart, Segment> dartToSegmentMap,
			Map<Vertex, Segment> horizontalSegmentMap, Map<Vertex, Segment> verticalSegmentMap) {
		return new CompactionImpl(o, horizontal, vertical, horizontalSegmentMap, verticalSegmentMap, dartToSegmentMap);
	}
	
	private Map<Dart, Segment> calculateDartToSegmentMap(List<Segment> h1, List<Segment> v1) {
		Map<Dart, Segment> out = new HashMap<>();
		h1.stream().forEach(s -> addSegmentsToMap(out, s));
		v1.stream().forEach(s -> addSegmentsToMap(out, s));
		return out;
	}
	
	private void addSegmentsToMap(Map<Dart, Segment> dartSegmentMap, Segment segment) {
		for (Dart d : segment.getDartsInSegment()) {
			dartSegmentMap.put(d, segment);
		}
	}

	@Override
	public void compact(Rectangular r, Compaction c) {
		for (CompactionStep step : steps) {
			step.compact(c, r, this);
		}
	}
	
	protected Map<Vertex, Segment> createVertexSegmentMap(List<Segment> segs) {
		Map<Vertex, Segment> vertexToSegmentMap = new HashMap<Vertex, Segment>();
		for (Segment segment : segs) {
			for (Vertex v : segment.getVerticesInSegment()) {
				vertexToSegmentMap.put(v, segment);
			}
		}
		return vertexToSegmentMap;
	}

	public List<Segment> buildSegmentList(Orthogonalization o, Set<Direction> direction) {
		List<Segment> segments = sb.buildSegmentList(o, direction, direction==HORIZONTAL ? PositionAction.YAction : PositionAction.XAction);
		return segments;
	}
	
}
