package org.kite9.diagram.visualization.compaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
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
	
	/**
	 * One-dimensional compaction algorithm.  
	 */
	public Compaction compactDiagram(Orthogonalization o) {
		List<Segment> horizontal = buildSegmentList(o, HORIZONTAL);
		List<Segment> vertical = buildSegmentList(o, VERTICAL);
		CompactionImpl compaction = new CompactionImpl(o, horizontal, vertical, createVertexSegmentMap(horizontal), createVertexSegmentMap(vertical));
		compactDiagram(compaction);
		return compaction;
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

	protected void compactDiagram(Compaction c) {
		for (CompactionStep step : steps) {
			step.compactDiagram(c);
		}
	}
	
	public List<Segment> buildSegmentList(Orthogonalization o, Set<Direction> direction) {
		List<Segment> segments = sb.buildSegmentList(o, direction, direction==HORIZONTAL ? PositionAction.YAction : PositionAction.XAction);
		return segments;
	}
	
	
}
