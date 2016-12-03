package org.kite9.diagram.visualization.compaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
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
	
	public List<Segment> buildSegmentList(Orthogonalization o, Set<Direction> planeDirection, PositionAction direction) {
		Set<Direction> transversePlane = new LinkedHashSet<Direction>();
		for (Direction d2 : planeDirection) {
			transversePlane.add(Direction.rotateClockwise(d2));
		}
		
		List<Segment> result = new ArrayList<Segment>();
		Set<Vertex> done = new UnorderedSet<Vertex>();
		int segNo = 1;
	
		for (Vertex v : o.getAllVertices()) {
			if (!done.contains(v)) {
				Segment s = new Segment(direction, segNo++);
				extendSegmentFromVertex(v, planeDirection, s, done);
				done.addAll(s.getVerticesInSegment());
				result.add(s);
			}
		}
		
		for (Segment s : result) {
			setSegmentUnderlying(s);
			
			if (s.underlying instanceof Connected) {
				s.underlyingSide = getContainedSegmentUnderlyingSide(s, s.underlying, transversePlane);
			}
			log.send(log.go() ? null : "Segment: "+s.i+" has underlying "+s.underlying+" on side "+s.underlyingSide);
		}
		
		return result;
	}

	private void setSegmentUnderlying(Segment s) {
		DiagramElement underlying = null;
		Collection<Dart> darts =s.getDartsInSegment();
		for (Dart d : darts) {
			DiagramElement u = getLowestLevelUnderlying(d);
			
			if (u!=null) {
				if (underlying==null) {
					underlying = u;
				} 
			}
			
			if (d.getOrthogonalPositionPreference()!=null) {
				s.underlyingSide = d.getOrthogonalPositionPreference();
			}
		}
		
		s.underlying = underlying;
	}

	/**
	 * This works out for a particular segment, which side of the underlying diagram element it represents.
	 * This works by tracing darts with the same underlying, and looking at which direction they go in.
	 */
	private Direction getContainedSegmentUnderlyingSide(Segment s, DiagramElement underlying, Set<Direction> planeDirection) {
		Set<Direction> sides = new LinkedHashSet<Direction>();
		for (Direction direction : planeDirection) {
			boolean yes = checkDiagramElementContinues(s, underlying, direction);
			if (!yes) {
				sides.add(direction);
			}
		}
		
		if (sides.size()==1) {
			return sides.iterator().next();
		}
		
		return null;
		
	}

	private boolean checkDiagramElementContinues(Segment s, DiagramElement underlying, Direction direction) {
		for (Vertex v : s.getVerticesInSegment()) {
			for (Edge e : v.getEdges()) {
				if (e instanceof Dart) {
					DiagramElement dartUnderlying = getLowestLevelUnderlying((Dart) e);
					if (dartUnderlying==underlying) {
						Direction eDir = e.getDrawDirectionFrom(v);
						if (eDir==direction) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	
	private DiagramElement getLowestLevelUnderlying(Dart d) {
		return d.getOriginalUnderlying();
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
