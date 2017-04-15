package org.kite9.diagram.visualization.orthogonalization.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.ConnectionEdgeBendVertex;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.OrthogonalizationImpl;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This creates an orthogonalisation using the definition contained in the flow
 * graph.
 * 
 * @author robmoffat
 * 
 */
public class MappedFlowGraphOrthBuilder implements Logable, OrthBuilder<MappedFlowGraph> {

	private Kite9Log log = new Kite9Log(this);

	static enum StartPointType {
		
		DIRECTED, INFERRED, NONE;
		
	}

	static class StartPoint implements Comparable<StartPoint> {
		
		Edge e;
		Vertex v;
		Direction d;
		StartPointType type = StartPointType.NONE;
		Face f;
		double tolerance;
		
		@Override
		public int compareTo(StartPoint o) {
			if (type.ordinal() != o.type.ordinal()) {
				return ((Integer)type.ordinal()).compareTo(o.type.ordinal());
			} else if (type == StartPointType.INFERRED) {
				return ((Double)tolerance).compareTo(o.tolerance);
			} else {
				return 0;
			}
		}

		@Override
		public String toString() {
			return "f="+f.id+",t="+type+",s="+tolerance+",d="+d;
		}				
	}
	
	public OrthogonalizationImpl build(Planarization pln, MappedFlowGraph fg) {
		OrthogonalizationImpl o = new OrthogonalizationImpl(pln);

		Set<Face> doneFaces = new UnorderedSet<Face>();
		List<StartPoint> startPoints = selectBestStartPoints(pln);
		
		for (StartPoint startPoint : startPoints) {
			if (!doneFaces.contains(startPoint.f)) {
				log.send("Processing face: "+startPoint);
				processFace(startPoint.f, pln, o, fg, startPoint.d, doneFaces, startPoint.v, startPoint.e);	
			}
		}
		
		return o;
	}

	private List<StartPoint> selectBestStartPoints(Planarization pln) {
		// figure out somewhere to starts
		List<StartPoint> out = new ArrayList<MappedFlowGraphOrthBuilder.StartPoint>(pln.getFaces().size());

		// scans through the faces, stops when it finds a suitable directed edge.
		for (Face f : pln.getFaces()) {
			StartPoint best = null;

			for (int index = 0; index < f.edgeCount(); index++) {
				StartPoint sp = new StartPoint();
				sp.e = f.getBoundary(index);
				sp.v = f.getCorner(index);
				sp.d = sp.e.getDrawDirectionFrom(sp.v);
				
				if (AbstractFlowOrthogonalizer.isConstrained(sp.e)) {
					sp.type = StartPointType.DIRECTED;
				} else {
					sp.d = Direction.RIGHT;  // in case nothing else gets set
					RoutingInfo sri = sp.v.getRoutingInfo();
					RoutingInfo eri = sp.e.otherEnd(sp.v).getRoutingInfo() ;
					if ((sri != null) && (eri != null)) {
						if (sri.centerX() < eri.centerX()) {
							sp.tolerance = Math.abs(sri.centerY() - eri.centerY());
							sp.type = StartPointType.INFERRED;
							sp.d = Direction.RIGHT;
						} else {
							sp.tolerance = Math.abs(sri.centerY() - eri.centerY());
							sp.type = StartPointType.INFERRED;
							sp.d = Direction.LEFT;
						} 
					}
				}
				
				sp.f = f;
				
				if ((best == null) || (best.compareTo(sp) == 1)) {
					best = sp;
				}
			}
			
			if (best != null)
				out.add(best);
		}
		
		Collections.sort(out);
		
		return out;
	}


	private void processFace(Face f, Planarization pln, OrthogonalizationImpl o, MappedFlowGraph fg, Direction d,
			Set<Face> doneFaces, Vertex startVertex, Edge incoming) {
		if (doneFaces.contains(f)) {
			return;
		}

		doneFaces.add(f);

		log.send(log.go() ? null : "Processing face: " + f.getId());
		int index = f.indexOf(startVertex, incoming);

		DartFace df = o.createDartFace(f);
		df.dartsInFace = new ArrayList<DartDirection>();

		for (int i = 0; i < f.vertexCount(); i++) {
			int ei = (index + i);
			Edge e = f.getBoundary(ei);
			Vertex sv = f.getCorner(ei);
			Vertex ev = f.getCorner(ei + 1);
			List<DartDirection> created = processEdge(f, pln, o, fg, d, sv, ev, e, doneFaces);
			Edge next = f.getBoundary(ei + 1);
			d = getDirectionForNextDart(f, fg, ev, created, e, next, o);
			df.dartsInFace.addAll(created);
		}
		log.send(log.go() ? null : "Done face: " + f.getId() + " " + df.dartsInFace);
	}
	
	public Node getFaceToVertexNode(MappedFlowGraph fg, Face from, Vertex to, Edge before, Edge after) {
		return fg.getNodeFor(AbstractFlowOrthogonalizer.createFaceVertex(from, to, before, after));
	}


	private Direction getDirectionForNextDart(Face f, MappedFlowGraph fg, Vertex ev, List<DartDirection> created,
			Edge last, Edge next, OrthogonalizationImpl orth) {
		Direction d;
		Node helperNode = getFaceToVertexNode(fg, f, ev, last, next);

		int cap = 0;
		int outCap = 0;

		for (Arc a : helperNode.getArcs()) {
			// helper node is always 'To' node
			if (a.getFrom().getType() == AbstractFlowOrthogonalizer.PORTION_NODE) {
				outCap -= a.getFlow();
			} else if (a.getFrom().getType() == AbstractFlowOrthogonalizer.VERTEX_NODE) {
				cap -= a.getFlow();
			} else {
				throw new LogicException("This should be a face or vertex arc");
			}
		}

		if (cap + outCap != helperNode.getSupply()) {
			throw new LogicException("No parity between incoming and outgoing helper arcs");
		}

		if ((outCap > 2) || (outCap < -2)) {
			throw new LogicException("some strange maths: " + outCap + " " + cap);
		}

		Dart lastDart = created.get(created.size() - 1).getDart();
		d = lastDart.getDrawDirectionFrom(ev);
		d = Direction.reverse(d);
		if (outCap > -2) {
			// corner node
			addCorner(ev, lastDart, orth);
		}

		for (int i = 0; i < Math.abs(outCap); i++) {
			d = rotate90(d, -outCap);
		}

		log.send(log.go() ? null : "turn on " + ev + " is " + outCap + " to " + d + " due to " + helperNode);

		return d;
	}

	private void addCorner(Vertex ev, Dart lastDart, OrthogonalizationImpl orth) {
		List<Dart> dlist = orth.cornerDarts.get(ev);
		if (dlist == null) {
			dlist = new ArrayList<Dart>();
			orth.cornerDarts.put(ev, dlist);
		}
		dlist.add(lastDart);
	}

	/**
	 * Creates darts for a single Vertex and Edge in a face. Returns the angle
	 * of the last dart.
	 * 
	 * @return
	 */
	private List<DartDirection> processEdge(Face f, Planarization pln, OrthogonalizationImpl o, MappedFlowGraph fg,
			Direction nextDir, Vertex startVertex, Vertex endVertex, Edge e, Set<Face> doneFaces) {
		log.send(log.go() ? null : "Processing edge "+e.toString()+" from: " + startVertex + " to " + endVertex + " in direction " + nextDir);

		if (e.otherEnd(startVertex) != endVertex) {
			throw new LogicException("We have a problem");
		}

		Face outerFace = getOuterFace(e, f, pln);
		int arcCost = calculateEdgeBends(f, fg, e, startVertex);

		List<Vertex> waypoints = o.getWaypointsForEdge(e);
		int wpCount = Math.abs(arcCost) + 1;

		if (waypoints == null) {
			waypoints = new ArrayList<Vertex>(wpCount);
			waypoints.add(startVertex);
			for (int i = 0; i < wpCount - 1; i++) {
				ConnectionEdgeBendVertex bv = new ConnectionEdgeBendVertex(startVertex.getID() + "-" + i + "-" + endVertex.getID(), (ConnectionEdge) e);
				waypoints.add(bv);
				o.getAllVertices().add(bv);
			}
			waypoints.add(endVertex);
			o.setWaypointsForEdge(e, waypoints);
		} else if (waypoints.get(0) == endVertex) {
			waypoints = new ArrayList<Vertex>(waypoints);
			Collections.reverse(waypoints);
		} else if (waypoints.get(0) != startVertex) {
			throw new LogicException("Waypoints don't match vertices");
		}

		List<DartDirection> out = new ArrayList<DartDirection>();

		for (int i = 0; i < waypoints.size() - 1; i++) {
			Vertex start = waypoints.get(i);
			Vertex end = waypoints.get(i + 1);
			Dart dart = o.createDart(start, end, e, nextDir);
			dart.setChangeCost(getChangeCostForEdge(e), null);
			DartDirection dd = new DartDirection(dart, nextDir);
			log.send(log.go() ? null : "Created dart " + dart + ", "+dart.getID()+" for cost " + arcCost);
			if (i < (wpCount - 1)) {
				// rotate ready for next dart.
				nextDir = rotate90(nextDir, arcCost);
			}
			out.add(dd);
		}

		Direction opposite = Direction.reverse(nextDir);

		processFace(outerFace, pln, o, fg, opposite, doneFaces, endVertex, e);

		return out;
	}

	private int getChangeCostForEdge(Edge e) {
		if (e instanceof ConnectionEdge) {
			return Dart.CONNECTION_DART;
		} else {
			return Dart.EXTEND_IF_NEEDED;
		}
	}


	public Node getEdgeVertexNode(MappedFlowGraph fg, Edge e, Vertex v) {
		return fg.getNodeFor(AbstractFlowOrthogonalizer.createEdgeVertex(e, v));
	}

	
	/**
	 * Returns the number of bends in the edge, where positive is concave bends,
	 * and negative is convex, wrt to the face.
	 * 
	 * @param endVertex
	 * @param startVertex
	 */
	private int calculateEdgeBends(Face f, MappedFlowGraph fg, Edge e, Vertex startVertex) {
		List<Node> edgeNodes = new ArrayList<Node>();
		Node a = getEdgeVertexNode(fg, e, e.getFrom());
		Node b = getEdgeVertexNode(fg, e, e.getTo());
		Node c = getEdgeVertexNode(fg, e, null);
		if (a != null)
			edgeNodes.add(a);
		if (b != null)
			edgeNodes.add(b);
		if (c != null)
			edgeNodes.add(c);

		Collection<Node> ports = fg.getNodesForEdgePart(f, e, startVertex);

		// we are now going to sum up arcs leading to and from the edge to
		// figure out how many turns it has
		int arcConcaveCost = 0; // these are flow units pushed into the face
		int arcConvexCost = 0; // these are flow units pushed out of the Face

		for (Node edgeNode : edgeNodes) {
			for (Arc r : edgeNode.getArcs()) {
				Node faceEndNode = r.otherEnd(edgeNode);
				
				boolean out = r.getFrom() == edgeNode ? true : false;
				// we only care about nodes leading to the current portion
				if (ports.contains(faceEndNode)) {
					if (out) {
						arcConvexCost += r.getFlow();
					} else {
						arcConcaveCost += r.getFlow();
					}
				}
			}
		}
		// if ((arcConcaveCost>0) && (arcConvexCost>0)) {
		// throw new LogicException("Don't know what to do here");
		// }

		int arcCost = arcConcaveCost - arcConvexCost;

		log.send(log.go() ? null : e + " " + f.getId() + (f.isOuterFace() ? "outer" : "inner") + " cost " + arcCost);
		return arcCost;
	}


	public static Face getOuterFace(Edge e, Face f, Planarization pln) {
		List<Face> faces = pln.getEdgeFaceMap().get(e);
		if (faces.size() != 2) {
			throw new LogicException("Edge should only have 2 faces");
		}
		if (faces.get(0) == f) {
			return faces.get(1);
		} else if (faces.get(1) == f) {
			return faces.get(0);
		} else {
			throw new LogicException("Face map should contain f");
		}
	}

	private Direction rotate90(Direction d, int cost) {
		if (cost > 0) {
			d = Direction.rotateClockwise(d);
		} else if (cost < 0) {
			d = Direction.rotateAntiClockwise(d);
		}
		return d;
	}

	public String getPrefix() {
		return "FLOW";
	}

	public boolean isLoggingEnabled() {
		return true;
	}
}
