package org.kite9.diagram.visualization.compaction.segment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.FanVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.CompactedRectangular;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;


/**
 * This looks at the orthogonal representation and works out from the available Darts what 
 * Vertices must be on the same Vertical or Horizontal line.
 * 
 * @author robmoffat
 *
 */
public class SegmentBuilder implements Logable {
	
	Kite9Log log = new Kite9Log(this);
	
	public List<Segment> buildSegmentList(Orthogonalization o, Set<Direction> planeDirection, Dimension direction) {
		Set<Direction> transversePlane = new LinkedHashSet<Direction>();
		for (Direction d2 : planeDirection) {
			transversePlane.add(Direction.rotateClockwise(d2));
		}
		
		List<Segment> result = new ArrayList<Segment>();
		Set<Vertex> done = new UnorderedSet<Vertex>();
		int segNo = 1;
	
		for (Dart d : o.getAllDarts()) {
			if (planeDirection.contains(d.getDrawDirection())) {
				Vertex v = d.getFrom();
				if (!done.contains(v)) {
					Segment s = new Segment(direction, segNo++);
					extendSegmentFromVertex(v, planeDirection, s, done);
					s.setAlignStyle(getSegmentAlignStyle(s));
					done.addAll(s.getVerticesInSegment());
					result.add(s);
				}
			}
		}
		
		log.send("Segments", result);
		
		return result;
	}
	

	public AlignStyle getSegmentAlignStyle(Segment s) {
		if (s.getUnderlyingInfo().size() == 1) {
			UnderlyingInfo ui = s.getUnderlyingInfo().iterator().next();
			DiagramElement de = ui.getDiagramElement();
			if (de instanceof Connection) {
				for (Vertex v : s.getVerticesInSegment()) {
					if (v instanceof FanVertex) {
						Direction d = ((FanVertex) v).getFanSide();
						switch (d) {
						case UP:
						case LEFT:
							return AlignStyle.MIN;
						case DOWN:
						case RIGHT:
							return AlignStyle.MAX;
						}
					}
				}
				
			} else if (de instanceof CompactedRectangular) {
				DiagramElementSizing des = ((Rectangular) de).getSizing();
				
				if (des == DiagramElementSizing.MAXIMIZE) {
					switch (ui.getSide()) {
					case END:
						return AlignStyle.MAX;
					case START:
						return AlignStyle.MIN;
					default:
					}					
				} else {
					if (s.getDimension() == Dimension.H) {
						VerticalAlignment va = ((CompactedRectangular) de).getVerticalAlignment();
						switch (va) {
						case BOTTOM:
							return AlignStyle.MAX;
						case CENTER:
							return AlignStyle.CENTER;
						case TOP:
							return AlignStyle.MIN;
						}
					} else if (s.getDimension() == Dimension.V) {
						HorizontalAlignment ha = ((CompactedRectangular) de).getHorizontalAlignment();
						switch (ha) {
						case LEFT:
							return AlignStyle.MIN;
						case CENTER:
							return AlignStyle.CENTER;
						case RIGHT:
							return AlignStyle.MAX;
						}
					}
				}
				
				((CompactedRectangular) de).getVerticalAlignment();
			}
		}
		
		return null;
	}
	

	private void extendSegmentFromVertex(Vertex v, Set<Direction> planeDirection, Segment samePlane, Set<Vertex> done) {
		if (done.contains(v)) 
			return;
		
		samePlane.addToSegment(v);
		done.add(v);
		for (Edge e : v.getEdges()) {
			if ((e instanceof Dart) && (planeDirection.contains(((Dart)e).getDrawDirection()))) {
				Vertex other = e.otherEnd(v);
				extendSegmentFromVertex(other, planeDirection, samePlane, done);
			}
		}
	}

	public String getPrefix() {
		return "SEGB";
	}

	public boolean isLoggingEnabled() {
		return true;
	}
	
}
