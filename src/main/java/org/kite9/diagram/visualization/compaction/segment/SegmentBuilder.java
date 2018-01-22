package org.kite9.diagram.visualization.compaction.segment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.FanVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;


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
		Set<Connection> conns = s.getConnections();
		if (conns.size() == 1) {
			Connection de = conns.iterator().next();
			return decideConnectionSegmentAlignStyle(s, de);
		} else if (conns.size() == 0) {
			UnderlyingInfo toUse = s.getUnderlyingInfo().stream()
				.filter(ui -> ui.getDiagramElement() instanceof AlignedRectangular)
				.sorted((a, b) -> ((Integer) ((Rectangular) a.getDiagramElement()).getDepth())
					.compareTo(((Rectangular)b.getDiagramElement()).getDepth()))
				.findFirst().orElse(null);
			
			
			if (toUse != null) {
				AlignStyle out = decideRectangularAlignStyle(s, (AlignedRectangular) toUse.getDiagramElement()); 
				return out;
			}
			
			return null;
			
		} else {
			throw new LogicException();
		}
		
	}
	
	private AlignStyle decideRectangularAlignStyle(Segment s, AlignedRectangular de) {
		DiagramElementSizing des = de instanceof Container ? ((Container)de).getSizing() : null;
		
		if ((des == DiagramElementSizing.MINIMIZE) || (des == null)) {
			if (s.getDimension() == Dimension.H) {
				VerticalAlignment va = de.getVerticalAlignment();
				switch (va) {
				case BOTTOM:
					return AlignStyle.MAX;
				case CENTER:
					return AlignStyle.CENTER;
				case TOP:
					return AlignStyle.MIN;
				}
			} else if (s.getDimension() == Dimension.V) {
				HorizontalAlignment ha = de.getHorizontalAlignment();
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
		
		return null;
	}

	private AlignStyle decideConnectionSegmentAlignStyle(Segment s, Connection de) {
		if (de.getRenderingInformation().isContradicting()) {
			return null;
		}
		
		if (s.getDimension() == Dimension.H) {
			// horizontal segment, push up or down
			Set<Direction> pushDirections = filterFanDirections(s, Direction::isVertical);
			
//			if (pushDirections.size() > 1) {
//				throw new Kite9ProcessingException();
//			}
			
			if (pushDirections.size() == 1) {
				for (Direction d : pushDirections) {
					switch (d) {
					case UP:
						return AlignStyle.MIN;
					case DOWN:
						return AlignStyle.MAX;
					default:
					}
				}
			}
			
			return AlignStyle.CENTER;
		} else if (s.getDimension() == Dimension.V) {
			Set<Direction> pushDirections = filterFanDirections(s, Direction::isHorizontal);
			
//			if (pushDirections.size() > 1) {
//				throw new Kite9ProcessingException();
//			}
			
			if (pushDirections.size() == 1) {
				for (Direction d : pushDirections) {
					switch (d) {
					case LEFT:
						return AlignStyle.MIN;
					case RIGHT:
						return AlignStyle.MAX;
					default:
					}
				}
			}
			
			return AlignStyle.CENTER;
		} else {
			throw new Kite9ProcessingException("No dimension on segment");
		}
	}


	private Set<Direction> filterFanDirections(Segment s, Predicate<? super Direction> axis) {
		return s.getVerticesInSegment().stream()
			.filter(v -> v instanceof FanVertex)
			.map(v -> (FanVertex)v)
			.flatMap(v -> v.getFanSides().stream())
			.filter(axis)
			.collect(Collectors.toSet());
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
