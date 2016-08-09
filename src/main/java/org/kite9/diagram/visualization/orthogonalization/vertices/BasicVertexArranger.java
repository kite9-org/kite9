package org.kite9.diagram.visualization.orthogonalization.vertices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.PositionableDiagramElement;
import org.kite9.diagram.common.algorithms.Tools;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.CornerVertex;
import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.SideVertex;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.xml.LinkTerminator;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This converts a vertex to a face, so that the shape of the vertex can be
 * compacted, and the links into the vertex are attached to the correct sides.
 * 
 * @author robmoffat
 */
public class BasicVertexArranger implements Logable, VertexArranger {

	protected CompleteDisplayer sizer;
	
	public BasicVertexArranger(CompleteDisplayer cd) {
		super();
		this.sizer = cd;
	}

	public static final int INTER_EDGE_SEPARATION = 0;

	Kite9Log log = new Kite9Log(this);

	/**
	 * Holds vertices and darts for one side of the vertex being converted
	 */
	static class Side {

		List<Vertex> vertices = new ArrayList<Vertex>();

		List<Dart> newEdgeDarts = new ArrayList<Dart>();

	}

	static class Boundary {
		Dart from, to;
		Direction fromDir, toDir;
		List<DartDirection> toInsert = new ArrayList<DartDirection>();
		boolean used = false;
		@Override
		public String toString() {
			return "Boundary [from=" + from + ", fromDir=" + fromDir + ", to=" + to + ", toDir=" + toDir + ", used="
					+ used + "]";
		}
	}

	public void convertAllVerticesWithDimension(Orthogonalization o) {
		List<Vertex> allVertices = new ArrayList<Vertex>(o.getAllVertices());
		for (Vertex v : allVertices) {
			convertVertex(o, v);
		}
	}

	protected void convertVertex(Orthogonalization o, Vertex v) {
		if (!v.hasDimension()) 
			return;
		
		List<Dart> dartOrdering = new ArrayList<Dart>(o.getDartOrdering().get(v));
		Map<Direction, List<Dart>> dartDirections = getDartsInDirection(dartOrdering, v);
		boolean sized = sizer.requiresDimension(v.getOriginalUnderlying());
		
		boolean mulitpleHorizDarts = (dartDirections.get(Direction.LEFT).size() > 1) || 
			(dartDirections.get(Direction.RIGHT).size() > 1);
		
		boolean multipleVertDarts = (dartDirections.get(Direction.UP).size() > 1) || 
		(dartDirections.get(Direction.DOWN).size() > 1);
		
		
		convert(v, o, dartDirections, dartOrdering, sized || mulitpleHorizDarts || multipleVertDarts);
		RectangleRenderingInformation rri = (RectangleRenderingInformation) ((PositionableDiagramElement)(v.getOriginalUnderlying())).getRenderingInformation();
		// temporarily set
		rri.setMultipleHorizontalLinks(mulitpleHorizDarts);
		rri.setMultipleVerticalLinks(multipleVertDarts);
	}

	public void convert(Vertex v, Orthogonalization o, Map<Direction, List<Dart>> dartDirections, List<Dart> dartOrdering, boolean requiresMinSize) {
		log.send(log.go() ? null : "Converting: " + v + " with edges: ", dartOrdering);
		String name = v.getID();
		// first, need to create the four corner vertices
		DiagramElement originalUnderlying = v.getOriginalUnderlying();
		CornerVertex tl = new CornerVertex(name + "tl", HPos.LEFT, VPos.UP, originalUnderlying);
		CornerVertex tr = new CornerVertex(name + "tr", HPos.RIGHT, VPos.UP, originalUnderlying);
		CornerVertex bl = new CornerVertex(name + "bl", HPos.LEFT, VPos.DOWN, originalUnderlying);
		CornerVertex br = new CornerVertex(name + "br", HPos.RIGHT, VPos.DOWN, originalUnderlying);
		
		
		List<Dart> upDarts = dartDirections.get(Direction.UP);
		List<Dart> rightDarts =  dartDirections.get(Direction.RIGHT);
		List<Dart> downDarts = dartDirections.get(Direction.DOWN);
		List<Dart> leftDarts =  dartDirections.get(Direction.LEFT);

		
		// create darts for the minimum size of the vertex
		CostedDimension cd = sizer.size(originalUnderlying, CostedDimension.UNBOUNDED);

		Dart dx = o.createDart(tl, tr, v, Direction.RIGHT, cd.x());
		Dart dy = o.createDart(tl, bl, v, Direction.DOWN, cd.y());

		// put together darts for the edges of the vertex and join them up with
		// nominal directions for now
		// going in clockwise order
	

		Side tls = createSide(tl, tr, Direction.UP, v, upDarts, o, downDarts.size(), cd.x(), requiresMinSize);
		Side trs = createSide(tr, br, Direction.RIGHT, v, rightDarts, o, leftDarts.size(),  cd.y(), requiresMinSize);
		Side brs = createSide(br, bl, Direction.DOWN, v, downDarts, o, upDarts.size(), cd.x(), requiresMinSize);
		Side bls = createSide(bl, tl, Direction.LEFT, v, leftDarts, o, rightDarts.size(), cd.y(), requiresMinSize);

		// join segments
		Set<Vertex> allNewVertices = new UnorderedSet<Vertex>();
		allNewVertices.addAll(tls.vertices);
		allNewVertices.addAll(trs.vertices);
		allNewVertices.addAll(brs.vertices);
		allNewVertices.addAll(bls.vertices);

		LinkedHashSet<Dart> allSideDarts = new LinkedHashSet<Dart>();
		allSideDarts.addAll(tls.newEdgeDarts);
		allSideDarts.addAll(trs.newEdgeDarts);
		allSideDarts.addAll(brs.newEdgeDarts);
		allSideDarts.addAll(bls.newEdgeDarts);

		if (allSideDarts.size() != allNewVertices.size())
			throw new LogicException("Logic Error");

		List<Boundary> interveningDarts = getInterveningDarts(allNewVertices, allSideDarts, dartOrdering, originalUnderlying);

		if (interveningDarts.size() > 0) {
			for (DartFace f : o.getFaces()) {
				repairFace(f, dartOrdering, interveningDarts, originalUnderlying);
			}
		}
		
		for (Boundary boundary : interveningDarts) {
			if (!boundary.used) {
				throw new LogicException("Part of boundary not added to dart face: "+boundary);
			}
		}

		o.getAllVertices().addAll(allNewVertices);
		o.getAllDarts().add(dx);
		o.getAllDarts().add(dy);

		// remove the converted vertex from the orthogonalization
		o.getAllVertices().remove(v);

		// unconnected vertex in face
		if (dartOrdering.size() == 0) {
			handleUnconnectedVertex(o, v, allSideDarts, tl);
		}
	}

	/**
	 * An unconnected vertex should also be an outer dart face in the diagram.
	 * The face exists, but the dart face currently does not.
	 * @param start 
	 */
	private void handleUnconnectedVertex(Orthogonalization o, Vertex v, LinkedHashSet<Dart> allDarts, Vertex vs) {
		for (Face f : o.getPlanarization().getFaces()) {
			if (f.contains(v) && f.isOuterFace()) {
				DartFace df = o.createDartFace(f, f.isOuterFace());
				df.dartsInFace = new ArrayList<DartDirection>(allDarts.size());
				
				for (Dart dart : allDarts) {
					Direction d = Direction.reverse(dart.getDrawDirectionFrom(vs));
					df.dartsInFace.add(new DartDirection(dart, d));
					vs = dart.otherEnd(vs);
				} 
				
				Collections.reverse(df.dartsInFace);
				return;
			}
		}
	}

	/**
	 * Works around the face and looks for places where darts don't meet.
	 * Inserts missing darts from the new vertex face to fill the gaps.
	 */
	private void repairFace(DartFace f, List<Dart> rotDarts, List<Boundary> interveningDarts, DiagramElement fixingFor) {
		boolean faceChanged = false;
		List<DartDirection> face = f.dartsInFace;

		for (int i = 0; i < face.size(); i++) {
			DartDirection a = face.get(i);
			DartDirection b = face.get((i + 1) % face.size());
			int ai = rotDarts.indexOf(a.getDart());
			int bi = rotDarts.indexOf(b.getDart());

			if (((ai > -1) && (bi > -1))) {
				// regular fixing between two different dart
				boolean ok1 = (ai != bi) && (interveningDarts.size() > 1);
				// dart is the only thing entering and leaving the vertex
				boolean ok2 = (ai == bi) && (interveningDarts.size() == 1);

				if (ok1 || ok2) {
					List<DartDirection> toInsert = findDartsToInsert(interveningDarts, a, b);
					if (toInsert != null) {
						log.send(log.go() ? null : "Repair needed: " + a + " " + ai + " " + b + " " + bi);
						log.send(log.go() ? null : "inserting: " + toInsert + " at " + i);
						Tools.insertIntoList(face, i, toInsert);
						faceChanged = true;
					}
				}
			}
		}

		if (faceChanged) {
			log.send(log.go() ? null : "Face fixed:", face);
		}
	}

	/**
	 * Works out which darts are needed from the vertex to fill a gap between in
	 * and out in the face. Always proceeds in an anti-clockwise direction.
	 * 
	 * @param outerFace
	 */
	private List<DartDirection> findDartsToInsert(List<Boundary> interveningDarts, DartDirection in, DartDirection out) {
		for (Boundary b : interveningDarts) {
			if (b.used)
				continue;
//
//			if ((out.getDart() == b.to) && (in.getDart() == b.from)) {
//				if ((out.getDirection() == b.toDir) && (in.getDirection() == b.fromDir)) {
//					b.used = true;
//					return b.toInsert;
//				}
//			} else 
//				
			if ((in.getDart() == b.to) && (out.getDart() == b.from)) {
				if ((in.getDirection() == Direction.reverse(b.toDir)) && (out.getDirection() == Direction.reverse(b.fromDir))) {
					b.used = true;
					Collections.reverse(b.toInsert);
					for (DartDirection dd : b.toInsert) {
						dd.setDirection(Direction.reverse(dd.getDirection()));
					}
					return b.toInsert;
				}
			}

		}

		return null;
	}

	/**
	 * Divides up the darts around the vertex between the darts entering the
	 * vertex.
	 * @param originalUnderlying 
	 */
	protected List<Boundary> getInterveningDarts(Set<Vertex> allNewVertices, Set<Dart> sideDarts, List<Dart> rotDartsIn, DiagramElement forItem) {
		List<Boundary> out = new ArrayList<Boundary>();

		for (int i = 0; i < rotDartsIn.size(); i++) {
			Boundary b = new Boundary();
			Dart d1 = rotDartsIn.get(i);
			Dart d2 = rotDartsIn.get((i + 1) % rotDartsIn.size());
			b.from = d1;
			b.to = d2;
			
			// incoming direction
			b.fromDir = d1.getTo().getOriginalUnderlying() == forItem ? d1.getDrawDirection() : Direction.reverse(d1.getDrawDirection());
			
			// outgoing direction
			b.toDir = d2.getFrom().getOriginalUnderlying() == forItem ? d2.getDrawDirection() : Direction.reverse(d2.getDrawDirection());
			out.add(b);

			boolean fromIncident = allNewVertices.contains(d1.getFrom());
			boolean toIncident = allNewVertices.contains(d1.getTo());
			if ((fromIncident && toIncident) || (!fromIncident && !toIncident)) {
				throw new LogicException("One end of the dart " + d1 + " must meet " + allNewVertices);
			}
			Vertex incidentVertex = fromIncident ? d1.getFrom() : d1.getTo();
			Direction incidentDirection = Direction.reverse(d1.getDrawDirectionFrom(incidentVertex));
			Direction aroundDirection = Direction.rotateAntiClockwise(incidentDirection);
			Dart nextDart = getNextDart(incidentVertex, aroundDirection);
			do {
				b.toInsert.add(new DartDirection(nextDart, aroundDirection));
				incidentVertex = nextDart.otherEnd(incidentVertex);
				nextDart = getOtherDart(incidentVertex, nextDart, sideDarts);
				aroundDirection = nextDart.getDrawDirectionFrom(incidentVertex);
			} while (!(incidentVertex instanceof SideVertex));

		}

		return out;
	}

	private Dart getNextDart(Vertex incidentVertex, Direction aroundDirection) {
		for (Edge e : incidentVertex.getEdges()) {
			if ((e instanceof Dart) && (e.getDrawDirectionFrom(incidentVertex) == aroundDirection)) {
				return (Dart) e;
			}
		}

		throw new LogicException("Couldn't find dart leaving " + incidentVertex + " going " + aroundDirection);
	}

	private Dart getOtherDart(Vertex incidentVertex, Dart d, Set<Dart> sideDarts) {
		for (Edge e : incidentVertex.getEdges()) {
			if ((e instanceof Dart) && (e != d) && (sideDarts.contains(e))) {
				return (Dart) e;
			}
		}

		throw new LogicException("Couldn't find dart leaving " + incidentVertex + " other than " + d);
	}

	protected Map<Direction, List<Dart>> getDartsInDirection(List<Dart> processOrder, Vertex from) {
		Map<Direction, List<Dart>> out = new HashMap<Direction, List<Dart>>();
		out.put(Direction.DOWN, new ArrayList<Dart>());
		out.put(Direction.UP, new ArrayList<Dart>());
		out.put(Direction.LEFT, new ArrayList<Dart>());
		out.put(Direction.RIGHT, new ArrayList<Dart>());
		
		List<Dart> remainders = new ArrayList<Dart>();
		
		Direction firstDirection = null;
		boolean finishedFirst = false;
		
		for (Dart dart : processOrder) {
			if (dart.meets(from)) {
				Direction d = dart.getDrawDirectionFrom(from);
				if (firstDirection == null) {
					// first go
					out.get(d).add(dart);
					firstDirection = d;
				} else if (d == firstDirection) {
					if (finishedFirst) {
						remainders.add(dart);
					} else {
						out.get(d).add(dart);
					}
				} else {
					// direction has moved away from first
					finishedFirst = true;
					out.get(d).add(dart);
				}
			}
		}
		
		if (firstDirection!=null) {
			out.get(firstDirection).addAll(0, remainders);
		}
		return out;

	}

	protected Side createSide(CornerVertex tl, CornerVertex tr, Direction d, Vertex from, List<Dart> onSide,
			Orthogonalization o,int oppDarts, double lengthOpt, boolean requiresMinSize) {
		int i = 0;
		Side out = new Side();
		out.vertices.add(tl);
		Vertex last = tl;
		Direction segmentDirection = Direction.rotateClockwise(d);
		Edge lastEdge = null;
		Dart dart = null;

		for (int j = 0; j < onSide.size(); j++) {
			dart = onSide.get(j);
			Edge thisEdge = (Edge) dart.getUnderlying();
			Vertex vsv;
			boolean invisible = thisEdge instanceof DirectionEnforcingElement;
			if (lastEdge != thisEdge) {
				// need to add a dart for this segment
				vsv = createSideVertex(d, from, i, invisible);
				i++;

				Dart sideDart = createSideDart(from, o, last, segmentDirection, oppDarts, lengthOpt, j==0, vsv, onSide.size(), thisEdge, lastEdge, requiresMinSize);
				sideDart.setOrthogonalPositionPreference(d);
				out.newEdgeDarts.add(sideDart);
				out.vertices.add(vsv);
			} else {
				// reuse last vertex side
				vsv = out.vertices.get(out.vertices.size() - 1);
			}

			updateWaypointMap(thisEdge, from, vsv, o);

			from.removeEdge(dart);
			vsv.addEdge(dart);
			if (dart.getFrom().equals(from)) {
				dart.setFrom(vsv);
			} else if (dart.getTo().equals(from)) {
				dart.setTo(vsv);
			} else {
				throw new LogicException("logic error");
			}
			
			double len = dart.getLength();
			double minLen = getMinimumDartLength(dart, thisEdge); 
			dart.setLength(Math.max(len, minLen));
			
			last = vsv;
			lastEdge = thisEdge;
		}

		// finally, join to corner
		Dart sideDart = createSideDart(from, o, last, segmentDirection, oppDarts, lengthOpt, true, tr, onSide.size(), null, lastEdge, requiresMinSize);
		sideDart.setOrthogonalPositionPreference(d);
		out.newEdgeDarts.add(sideDart);
		return out;
	}

	protected Vertex createSideVertex(Direction d, Vertex from, int i, boolean invisible) {
		Vertex vsv;
		if (invisible) {
			vsv = new HiddenSideVertex(from.getID() + "/" + d.toString() + i, from
					.getOriginalUnderlying());
		} else {
			vsv = new SideVertex(from.getID() + "/" + d.toString() + i, from.getOriginalUnderlying());
		}
		return vsv;
	}

	protected Dart createSideDart(Vertex from, Orthogonalization o, Vertex last, Direction segmentDirection,
			int oppDarts, double lengthOpt, boolean endDart, Vertex vsv, int sideDarts, Edge currentEdge, Edge lastEdge, boolean requiresMinSize) {
	
		// dist can be set for the first and last darts only if fixed length and
		// one dart on side
		boolean knownLength = oppDarts<=1 && sideDarts <= 1;
		double dist = requiresMinSize ? sizer.getLinkMargin(from.getOriginalUnderlying(), segmentDirection) : 0;
		if (knownLength) {
			double distDueToSize = Math.ceil(lengthOpt / (sideDarts + 1.0));
			dist = Math.max(distDueToSize, dist); 
		}

		Dart out =  o.createDart(last, vsv, from, segmentDirection, dist);
		out.setVertexLengthKnown(knownLength);
		return out;
	}

	protected void updateWaypointMap(Edge partOf, Vertex from, Vertex to, Orthogonalization o) {
		List<Vertex> waypoints = o.getWaypointMap().get(partOf);
		int index = waypoints.indexOf(from);
		if (index > -1) {
			waypoints.set(index, to);
		}
	}
	
	/**
	 * Ensures that there is enough length on the dart to include the terminator shape
	 */
	protected double getMinimumDartLength(Dart d, Edge underlyingEdge) {
		LinkTerminator terminatorFrom = getTerminator(underlyingEdge, d.getFrom());
		LinkTerminator terminatorTo = getTerminator(underlyingEdge, d.getTo());
		
		double fromSize = sizer.getTerminatorLength(terminatorFrom);
		double toSize = sizer.getTerminatorLength(terminatorTo);
		
		return fromSize + toSize;
	}

	protected LinkTerminator getTerminator(Edge underlyingEdge, Vertex v) {
		DiagramElement underlyingDestination = v.getOriginalUnderlying();
		DiagramElement underlyingLink = underlyingEdge.getOriginalUnderlying();
		if (underlyingLink instanceof Connection) {
			boolean fromEnd = ((Connection) underlyingLink).getFrom() == underlyingDestination;
			boolean toEnd = ((Connection) underlyingLink).getTo() == underlyingDestination;
			
			if (fromEnd) {
				return ((Connection) underlyingLink).getFromDecoration();
			}
			
			if (toEnd) {
				return ((Connection) underlyingLink).getToDecoration();
			}
		}
		
		return null;
	}
	

	public String getPrefix() {
		return "FACE";
	}

	public boolean isLoggingEnabled() {
		return false;
	}

}
