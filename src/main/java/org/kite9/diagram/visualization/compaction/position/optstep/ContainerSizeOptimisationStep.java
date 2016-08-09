package org.kite9.diagram.visualization.compaction.position.optstep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.framework.logging.Logable;

/**
 * Pushes the left sides of containers as far right as possible. </ul>
 * 
 * @author robmoffat
 * 
 */
public class ContainerSizeOptimisationStep implements OptimisationStep, Logable {
	
	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		sizeContainerRule(xo);
		sizeContainerRule(yo);
	}

	private void sizeContainerRule(SegmentSlackOptimisation xo) {
		Map<Container, Pair<List<Slideable>>> containerMap = new HashMap<Container, Pair<List<Slideable>>>();

		for (Slideable s : xo.getCanonicalOrder()) {
			DiagramElement de = ((Segment)s.getUnderlying()).getUnderlying();
			if (de instanceof Container) {
				Container con = (Container) de;
				Direction d = getSegmentUnderlyingSide((Segment) s.getUnderlying(), con);

				if (d != null) {
					Pair<List<Slideable>> mapItem = containerMap.get(con);
					if (mapItem == null) {
						mapItem = new Pair<List<Slideable>>(new ArrayList<Slideable>(), new ArrayList<Slideable>());
						containerMap.put(con, mapItem);
					}

					switch (d) {
					case LEFT:
					case UP:
						mapItem.getA().add(s);
						break;
					case RIGHT:
					case DOWN:
						mapItem.getB().add(s);
						break;
					}
				}
			}
		}

		// ok, now work through the items removing slack
		for (Pair<List<Slideable>> p : containerMap.values()) {
			for (Slideable from : p.getA()) {
				for (Slideable to : p.getB()) {
					maximizeDistance(xo, from, to);
				}
			}
		}

	}
	
	/**
	 * This works out for a particular segment, which side of the underlying diagram element it represents.
	 * This works by tracing darts with the same underlying, and looking at which direction they go in.
	 */
	private Direction getSegmentUnderlyingSide(Segment s, DiagramElement underlying) {
		Set<Direction> sides = new LinkedHashSet<Direction>();
		Set<Direction> planeDirection = new LinkedHashSet<Direction>(4);
		if (s.getDimension()==PositionAction.XAction) {
			planeDirection.add(Direction.LEFT);
			planeDirection.add(Direction.RIGHT);
		} else {
			planeDirection.add(Direction.UP);
			planeDirection.add(Direction.DOWN);
		}
		
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
					DiagramElement dartUnderlying = ((Dart) e).getOriginalUnderlying();
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

	
	private void maximizeDistance(SegmentSlackOptimisation xo, Slideable from, Slideable to) {
		int slackAvailable = to.getMaximumPosition() - from.getMinimumPosition();
		from.decreaseMaximum(from.getMinimumPosition());
		xo.ensureMinimumDistance(from, to, slackAvailable, true);
	}
	

	public String getPrefix() {
		return "MCSO";
	}

	public boolean isLoggingEnabled() {
		return true;
	}


}
