package org.kite9.diagram.visualization.compaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.SegmentBuilder;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.framework.common.HelpMethods;
import org.kite9.framework.logging.LogicException;


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
		Embedding topEmbedding = generateEmbeddings(o);
		Compaction compaction = instantiateCompaction(o, horizontal, vertical, dartToSegmentMap, horizontalSegmentMap, verticalSegmentMap, topEmbedding);
		compact(compaction.getTopEmbedding(), compaction);
		return compaction;
	}

	
	private Embedding generateEmbeddings(Orthogonalization o) {
		Map<DartFace, EmbeddingImpl> done = new LinkedHashMap<>();
		int embeddingNumber = 0;
		for (DartFace dartFace : o.getFaces()) {
			if (!done.containsKey(dartFace)) {
				Set<DartFace> touching = new HashSet<>();
				touching = getTouchingFaces(touching, dartFace, o);
				EmbeddingImpl ei = new EmbeddingImpl(embeddingNumber++, touching);
				touching.forEach(df -> done.put(df, ei));
			}
		}
		
		for (EmbeddingImpl e : new LinkedHashSet<>(done.values())) {
			List<DartFace> contained = e.getDartFaces().stream().flatMap(df -> df.getContainedFaces().stream()).collect(Collectors.toList());
			List<Embedding> inside = contained.stream()
					.map(df -> done.get(df))
					.collect(Collectors.toList());
			e.setInnerEmbeddings(inside);
		}
		
		// return just the top one
		EmbeddingImpl topEmbedding = done.values().stream().filter(e -> isTopEmbedding(e)).findFirst().orElseThrow(() -> new LogicException("No top embedding"));
		topEmbedding.setTopEmbedding(true);
		return topEmbedding;
	}

	private boolean isTopEmbedding(Embedding e) {
		return e.getDartFaces().stream().anyMatch(df -> df.outerFace && df.getContainedBy() == null);
	}


	private Set<DartFace> getTouchingFaces(Set<DartFace> touching, DartFace dartFace, Orthogonalization o) {
		boolean todo = touching.add(dartFace);
		if (todo) {
			for (DartDirection d : dartFace.getDartsInFace()) {
				List<DartFace> faces = o.getDartFacesForDart(d.getDart());
				for (DartFace f2 : faces) {
					if (f2 != dartFace) {
						touching = getTouchingFaces(touching, f2, o);
					}
				}
			}
		}
		
		return touching;
	}


	protected Compaction instantiateCompaction(Orthogonalization o, List<Segment> horizontal, List<Segment> vertical, Map<Dart, Segment> dartToSegmentMap,
			Map<Vertex, Segment> horizontalSegmentMap, Map<Vertex, Segment> verticalSegmentMap, Embedding topEmbedding) {
		return new CompactionImpl(o, horizontal, vertical, horizontalSegmentMap, verticalSegmentMap, dartToSegmentMap, topEmbedding);
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
	public void compact(Embedding e, Compaction c) {
		for (CompactionStep step : steps) {
			step.compact(c, e, this);
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
		List<Segment> segments = sb.buildSegmentList(o, direction, direction==HORIZONTAL ? Dimension.H : Dimension.V);
		return segments;
	}
	
}
