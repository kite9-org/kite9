package org.kite9.diagram.visualization.orthogonalization.vertices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.AbstractAnchoringVertex;
import org.kite9.diagram.common.elements.vertex.AbstractAnchoringVertex.Anchor;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.HPos;
import org.kite9.diagram.model.position.VPos;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

/**
 * Transforms darts for undirected links to a container.  
 * Each link begins on a different vertex around the container's perimeter.
 * 
 * @author robmoffat
 *
 */
public class ContainerCornerVertexArranger extends FanInVertexArranger {
	
	public ContainerCornerVertexArranger(ElementMapper em) {
		super(em);
	}

	@Override
	protected void convertVertex(Orthogonalization o, Vertex v) {
		if (v instanceof MultiCornerVertex) {
			MultiCornerVertex cv = (MultiCornerVertex) v;
			List<Dart> dartOrdering = new ArrayList<Dart>(o.getDartOrdering().get(v));
			Map<Direction, List<Dart>> dartDirections = getDartsInDirection(dartOrdering, v);
			
			if (MultiCornerVertex.isMin(cv.getYOrdinal())) {
				if (MultiCornerVertex.isMin(cv.getXOrdinal())) {
					processCorner(o, cv, HPos.LEFT, VPos.UP, dartDirections, Direction.UP, Direction.RIGHT);
				} else if (MultiCornerVertex.isMax(cv.getXOrdinal())) {
					processCorner(o, cv, HPos.RIGHT, VPos.UP, dartDirections, Direction.UP, Direction.LEFT);
				} else {
					processSide(o, cv, null, VPos.UP, dartDirections, Direction.UP, Direction.RIGHT);
				}
			}
			
			if (MultiCornerVertex.isMax(cv.getYOrdinal())) {
				if (MultiCornerVertex.isMin(cv.getXOrdinal())) {
					processCorner(o, cv, HPos.LEFT, VPos.DOWN, dartDirections, Direction.DOWN, Direction.RIGHT);
				} else if (MultiCornerVertex.isMax(cv.getXOrdinal())) {
					processCorner(o, cv, HPos.RIGHT, VPos.DOWN, dartDirections, Direction.DOWN, Direction.LEFT);
				} else {
					processSide(o, cv, null, VPos.DOWN, dartDirections, Direction.DOWN, Direction.LEFT);
				}
			}
 
			if (MultiCornerVertex.isMin(cv.getXOrdinal())) {
				if (MultiCornerVertex.isMin(cv.getYOrdinal())) {
					processCorner(o, cv, HPos.LEFT, VPos.UP, dartDirections, Direction.LEFT, Direction.DOWN);
				} else if (MultiCornerVertex.isMax(cv.getYOrdinal())) {
					processCorner(o, cv, HPos.LEFT, VPos.DOWN, dartDirections, Direction.LEFT, Direction.UP);
				} else {
					processSide(o, cv, HPos.LEFT, null, dartDirections, Direction.LEFT, Direction.UP);
				}
			}
			
			if (MultiCornerVertex.isMax(cv.getXOrdinal())) {
				if (MultiCornerVertex.isMin(cv.getYOrdinal())) {
					processCorner(o, cv, HPos.RIGHT, VPos.UP, dartDirections, Direction.RIGHT, Direction.DOWN);
				} else if (MultiCornerVertex.isMax(cv.getYOrdinal())) {
					processCorner(o, cv, HPos.RIGHT, VPos.DOWN, dartDirections, Direction.RIGHT, Direction.UP);
				} else {
					processSide(o, cv, HPos.RIGHT, null, dartDirections, Direction.RIGHT, Direction.DOWN);
				}
			}
			
		} else {
			super.convertVertex(o, v);
		}
	}

	private void processCorner(Orthogonalization o, MultiCornerVertex cv,
			HPos h, VPos v, Map<Direction, List<Dart>> dartDirections, Direction outwards, Direction splitInDirection) {
		DiagramElement und = getCorrectUnderlying(cv, h, v);
		boolean reverse = Direction.rotateAntiClockwise(splitInDirection) == outwards;
		
		List<Dart> outDarts = dartDirections.get(outwards);
		if (outDarts.size() > 0) {
			if (checkDirection(outDarts, und)) {
				if (reverse) {
					Collections.reverse(outDarts);
				}
				processDarts(und, cv, splitInDirection, outDarts, getDartGoing(cv, splitInDirection), o);
			}
		}
	}
	
	private DiagramElement getCorrectUnderlying(MultiCornerVertex cv, HPos h, VPos v) {
		for (Anchor a : cv.getAnchors()) {
			if ((a.getLr() == h) && (a.getUd() == v)) {
				return a.getDe();
			} 
		}
		
		throw new Kite9ProcessingException("Couldn't find correct underlying");
	}

	private void processSide(Orthogonalization o, MultiCornerVertex cv, HPos h, VPos v,
			Map<Direction, List<Dart>> dartDirections, Direction outwards, Direction splitInDirection) {
		DiagramElement und = getCorrectUnderlying(cv, h, v);
		List<Dart> outDarts = dartDirections.get(outwards);
		if (outDarts.size() > 1) {
			if (checkDirection(outDarts, und)) {
				outDarts.remove(0);  // don't split the first one
				processDarts(und, cv, splitInDirection, outDarts, getDartGoing(cv, splitInDirection), o);
			}
		}
	}

	/**
	 * Makes sure we are looking at darts leaving the container
	 */
	private boolean checkDirection(List<Dart> outDarts, DiagramElement und) {
		if (outDarts.size() == 1) {
			return (outDarts.get(0).getOriginalUnderlying() != und);
		}
		
		return true;
	}

	private Dart getDartGoing(MultiCornerVertex cv, Direction d) {
		for (Edge e : cv.getEdges()) {
			if ((e instanceof Dart) && ((e.getDrawDirectionFrom(cv) == d))) {
				return (Dart) e;
			}
		}
		
		throw new LogicException("Couldn't find side dart going "+d+" from "+cv);
	}

	protected void processDarts(DiagramElement underlying, AbstractAnchoringVertex cv, Direction splitDirection, List<Dart> leaversToMove, Dart toSplit, Orthogonalization o) {
		Edge thisEdge = (Edge) toSplit.getUnderlying();
		BorderEdge cbe = (BorderEdge) toSplit.getUnderlying();
		
		for (int j = 0; j < leaversToMove.size(); j++) {
			Dart leaving = leaversToMove.get(j);
			Vertex from = toSplit.otherEnd(cv);
			Vertex vsv = createSideVertex(splitDirection, underlying, j, thisEdge instanceof DirectionEnforcingElement);
			//double dist = sizer.getLinkPadding(underlying, splitDirection);
			Dart sideDart = o.createDart(cv, vsv, cbe, splitDirection);
			
			cv.removeEdge(leaving);
			replaceEnd(cv, leaving, vsv);
			
			cv.removeEdge(toSplit);
			replaceEnd(cv, toSplit, vsv);
			
			vsv.addEdge(leaving);
			vsv.addEdge(toSplit);

			insertInWaypointMap(thisEdge, cv, from, vsv, o);
			updateWaypointMap((Edge) leaving.getUnderlying(), cv, vsv, o);

			toSplit = sideDart;
			
			// very inefficient - add a map
			for (DartFace df : o.getFaces()) {
				repairFace(cv, vsv, df, sideDart);
			}
			o.getAllDarts().add(sideDart);
			o.getAllVertices().add(vsv);
		}
	}

	private void repairFace(Vertex in, Vertex out, DartFace df, Dart newDart) {
		int size = df.dartsInFace.size();
		for (int i = 0; i < size; i++) {
			DartDirection before = df.dartsInFace.get(i);
			int ai = (i+1+size) % size;
			DartDirection after = df.dartsInFace.get(ai);
			if (before.getDart().meets(in) && after.getDart().meets(out)) {
				// needs to go here.
				df.dartsInFace.add(ai, new DartDirection(newDart, newDart.getDrawDirection()));
				return;
			} else if (before.getDart().meets(out) && after.getDart().meets(in)) {
				// or here
				df.dartsInFace.add(ai, new DartDirection(newDart, Direction.reverse(newDart.getDrawDirection())));
				return;
			}
		}
	}

	private void insertInWaypointMap(Edge thisEdge, AbstractAnchoringVertex before, Vertex after, Vertex insert, Orthogonalization o) {
		List<Vertex> waypoints = o.getWaypointMap().get(thisEdge);
		for (int i = 0; i < waypoints.size()-1; i++) {
			Vertex b = waypoints.get(i);
			Vertex a = waypoints.get((i+1+waypoints.size()) % waypoints.size());
			
			if (((before == a) && (after==b)) || ((before == b) && (after == a))) {
				waypoints.add(i+1, insert);
				return;
			}
		}

		throw new LogicException("Waypoint map can't add between "+before+" and "+after+ " "+waypoints);
	}

	private void replaceEnd(AbstractAnchoringVertex old, Dart leaving, Vertex to) {
		if (leaving.getFrom().equals(old)) {
			leaving.setFrom(to);
		} else if (leaving.getTo().equals(old)) {
			leaving.setTo(to);
		} else {
			throw new LogicException("logic error");
		}
	}
}
