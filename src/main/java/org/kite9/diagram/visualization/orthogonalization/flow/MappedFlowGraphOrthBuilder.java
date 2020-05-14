package org.kite9.diagram.visualization.orthogonalization.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.vertex.ConnectedVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.ConnectionEdgeBendVertex;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.OrthogonalizationImpl;
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger;
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger.TurnInformation;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;
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
public class MappedFlowGraphOrthBuilder implements Logable, OrthBuilder {

	private Kite9Log log = new Kite9Log(this);
	private VertexArranger va;
	private EdgeConverter clc;
	private MappedFlowGraph fg;
	private Map<Vertex, TurnInformation> turnInfoMap;
	
	public MappedFlowGraphOrthBuilder(VertexArranger va, MappedFlowGraph flowGraph, EdgeConverter clc) {
		this.va = va;
		this.clc = clc;
		this.fg = flowGraph;
		this.turnInfoMap = new HashMap<>();
	}

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
	
	public OrthogonalizationImpl build(Planarization pln) {
		OrthogonalizationImpl o = new OrthogonalizationImpl(pln);

		Map<Face, DartFace> doneFaces = new HashMap<Face, DartFace>();
		Map<Edge, List<Vertex>> doneEdges = new HashMap<>();
		List<StartPoint> startPoints = selectBestStartPoints(pln);
		
		for (StartPoint startPoint : startPoints) {
			if (!doneFaces.containsKey(startPoint.f)) {
				log.send("Processing face: "+startPoint);
				processFace(startPoint.f, pln, o, startPoint.d, doneFaces, startPoint.v, (PlanarizationEdge) startPoint.e, doneEdges);	
			}
		}
		
		// handle single-vertex faces
		for (Face f : pln.getFaces()) {
			Face container = f.getContainedBy();
			DartFace dfContainer = container != null ? doneFaces.get(container) : null;
			
			if (!doneFaces.containsKey(f)) {
				// single-vertex face
				
				ConnectedVertex corner = (ConnectedVertex) f.getCorner(0);
				List<DartDirection> innerFaceDarts = va.returnAllDarts(corner, o);
				Vertex topLeft = innerFaceDarts.get(0).getDart().getFrom();
				DartFace df = va.convertToOuterFace(o, topLeft, corner.getOriginalUnderlying());
				df.setContainedBy(dfContainer);
				
			} else if (f.isOuterFace()) {
				DartFace df = doneFaces.get(f);
				df.setContainedBy(dfContainer);
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
	
	
	
	static class SingleVertexTurnInformation implements TurnInformation {

		final Map<Edge, Direction> map;
		final Map<Edge, Boolean> turns;
		final Edge start;
		
		public SingleVertexTurnInformation(Map<Edge, Direction> map, Edge start, Map<Edge, Boolean> turns) {
			super();
			this.map = map;
			this.start = start;
			this.turns = turns;
		}

		@Override
		public Direction getIncidentDartDirection(Edge e) {
			return map.get(e);
		}

		@Override
		public String toString() {
			return "SingleVertexTurnInformation [map=" + map + "]";
		}

		@Override
		public Edge getFirstEdgeClockwiseEdgeOnASide() {
			return start;
		}

		@Override
		public boolean doesEdgeHaveTurns(Edge e) {
			return turns.get(e);
		}
		
		
	}
	
	/**
	 * Given an incident edge, and a known direction, and vertex, works out the exit directions for all edges
	 * surrounding the vertex.
	 */
	private TurnInformation getTurnInformationFor(Edge e1, Vertex sv, Direction d, Planarization pln) {
		TurnInformation out = turnInfoMap.get(sv);
		if (out != null) {
			return out;
		}
		
		Map<Edge, Direction> directionMap = new HashMap<>();
		Map<Edge, Boolean> turnsMap = new HashMap<>();
		List<Face> faces = pln.getVertexFaceMap().get(sv);
		EdgeOrdering eo = pln.getEdgeOrderings().get(sv);
		Iterator<PlanarizationEdge> it = eo.getIterator(true, (PlanarizationEdge) e1, (PlanarizationEdge) e1, false);
		Edge startEdge = null;
		while (it.hasNext()) {
			PlanarizationEdge e2 = it.next();
			
			if (e2 == e1) {
				directionMap.put(e1, d);
			} else {
				Face f = getCorrectFace(faces, sv, e1, e2);
				int outCap = calculateTurns(f, fg, sv, e2, e1);
				for (int i = 0; i < Math.abs(outCap); i++) {
					d = rotate90(d, outCap);
				}
				if (outCap != -2) {
					startEdge = e2;
				}
				d = Direction.reverse(d);
				directionMap.put(e2, d);
			}
			
			e1 = e2;
			
			// now calculate turn map
			List<Face> faces2 = pln.getEdgeFaceMap().get(e2);
			int edgeBends = calculateEdgeBends(faces2.get(0), fg, e2, sv);
			turnsMap.put(e2, (edgeBends != 0));
		}
		
		out = new SingleVertexTurnInformation(directionMap, startEdge, turnsMap);
		turnInfoMap.put(sv, out);

		return out;
	}

	private Face getCorrectFace(List<Face> faces, Vertex v, Edge following, Edge before) {
		for (Face face : faces) {
			if ((face.indexOf(v, following) > -1) 
					&& (face.contains(before))) {
				return face;
			}
		}
		
		throw new LogicException();
	}
	
	private void processFace(Face f, Planarization pln, OrthogonalizationImpl o, Direction processingEdgeStartDirection,
			Map<Face, DartFace> doneFaces, Vertex start, PlanarizationEdge leaving, Map<Edge, List<Vertex>> doneEdges) {
		
		if (doneFaces.containsKey(f)) {
			return;
		}


		log.send(log.go() ? null : "Processing face: " + f.getId());

		doneFaces.put(f, null);
		List<DartDirection> dartsInFace = new ArrayList<>();
		
		int startIndex = f.indexOf(start, leaving);
	
		Vertex processingEdgeDartFromVertex = null, processingEdgeDartToVertex = null;
		
		for (int i = 0; i < f.size()+1; i++) {
			// should be lastVertex - processingEdge - processingVertex - nextEdge
			int ei = startIndex + i;
			Vertex lastVertex = f.getCorner(ei);
			PlanarizationEdge processingEdge = f.getBoundary(ei);
			Vertex processingVertex = f.getCorner(ei+1);
			PlanarizationEdge nextEdge = f.getBoundary(ei+1);

			int edgeBends = calculateEdgeBends(f, fg, processingEdge, lastVertex);
			Direction processingEdgeEndDirection = turn(processingEdgeStartDirection, edgeBends);
			TurnInformation ti = getTurnInformationFor(processingEdge, processingVertex, processingEdgeEndDirection, pln);
			Direction nextEdgeStartDirection = Direction.reverse(ti.getIncidentDartDirection(nextEdge));
			
			List<DartDirection> vertexDarts;
			if (va.needsConversion(processingVertex)) {
				vertexDarts = va.returnDartsBetween(processingEdge, nextEdgeStartDirection, processingVertex, nextEdge, o, ti);
				processingEdgeDartToVertex = firstVertex(vertexDarts);
			} else {
				vertexDarts = Collections.emptyList();
				processingEdgeDartToVertex = processingVertex;
			}

			
			if (i > 0) {
				// process the edge - we need to do this after processing the first vertex 
				List<DartDirection> created = processEdge(f, pln, o, fg, processingEdgeStartDirection, 
						processingEdgeDartFromVertex, processingEdgeDartToVertex, processingEdge, doneFaces, doneEdges, lastVertex, processingVertex, edgeBends);
				dartsInFace.addAll(created);
				processingEdgeStartDirection = created.get(created.size()-1).getDirection();
			}
			
			if (i < f.size()) {
				if (va.needsConversion(processingVertex)) {
					processingEdgeDartFromVertex = lastVertex(vertexDarts);
				} else {
					processingEdgeDartFromVertex = processingVertex;
				}
				// prevents the vertex from being processed twice
				dartsInFace.addAll(vertexDarts);
			}

			
			// set for next round
			processingEdgeStartDirection = Direction.reverse(ti.getIncidentDartDirection(nextEdge));
			
			log.send("Face  "+f.getId()+" darts so far: "+dartsInFace);
		} 

		DartFace df = o.createDartFace(f.getPartOf(), f.isOuterFace(), dartsInFace);
		doneFaces.put(f, df);

		log.send(log.go() ? null : "Done face: " + f.getId() + " " + df.getDartsInFace());
	}

	private Direction turn(Direction d, int edgeBends) {
		for (int i = 0; i < Math.abs(edgeBends); i++) {
			d = rotate90(d, edgeBends);
		}
		
		return d;
	}

	private Vertex firstVertex(List<DartDirection> toBefore) {
		Dart d = toBefore.get(0).getDart();
		if (d.getDrawDirection() == toBefore.get(0).getDirection()) {
			return d.getFrom();
		} else {
			return d.getTo();
		}
	}
	
	private Vertex lastVertex(List<DartDirection> toBefore) {
		DartDirection dartDirection = toBefore.get(toBefore.size()-1);
		Dart d = dartDirection.getDart();
		if (d.getDrawDirection() == dartDirection.getDirection()) {
			return d.getTo();
		} else {
			return d.getFrom();
		}
	}

	public Node getFaceToVertexNode(MappedFlowGraph fg, Face from, Vertex to, Edge before, Edge after) {
		return fg.getNodeFor(AbstractFlowOrthogonalizer.createFaceVertex(from, to, before, after));
	}

	/**
	 * Returns the number of turns in this face for incoming last, outgoing next at vertex ev.
	 * Positive numbers mean clockwise turns, negative mean anti-clockwise.
	 * 
	 * The output number can vary between -2 and 2 inclusive.
	 */
	private int calculateTurns(Face f, MappedFlowGraph fg, Vertex ev, Edge last, Edge next) {
		Node helperNode  = getFaceToVertexNode(fg, f, ev, last, next);

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

		log.send(log.go() ? null : "turn on " + ev + " is " + outCap + " due to " + helperNode);
		return outCap;
	}

	private List<DartDirection> processEdge(Face f, Planarization pln, OrthogonalizationImpl o, MappedFlowGraph fg,
			Direction nextDir, Vertex startVertex, Vertex endVertex, PlanarizationEdge e, Map<Face, DartFace> doneFaces, Map<Edge, List<Vertex>> doneEdges, Vertex startPlanVertex, Vertex endPlanVertex, int arcCost) {
		log.send(log.go() ? null : "Processing edge "+e.toString()+" from: " + startVertex + " to " + endVertex + " in direction " + nextDir);

		if (e.otherEnd(startPlanVertex) != endPlanVertex) {
			throw new LogicException("We have a problem");
		}


		List<Vertex> waypoints = doneEdges.get(e);
		int wpCount = Math.abs(arcCost) + 1;

		if (waypoints == null) {
			waypoints = new ArrayList<Vertex>(wpCount);
			doneEdges.put(e, waypoints);
			waypoints.add(startVertex);
			for (int i = 0; i < wpCount - 1; i++) {
				ConnectionEdgeBendVertex bv = new ConnectionEdgeBendVertex(startVertex.getID() + "-" + i + "-" + endVertex.getID(), (ConnectionEdge) e);
				waypoints.add(bv);
			}
			waypoints.add(endVertex);
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
			createEdgePart(o, nextDir, start, end,  e.getDiagramElements(), out);
			if (i < (wpCount - 1)) {
				// rotate ready for next dart.
				nextDir = rotate90(nextDir, arcCost);
			}
		}

		Direction opposite = Direction.reverse(nextDir);
		Face outerFace = getOuterFace(e, f, pln);
		processFace(outerFace, pln, o, opposite, doneFaces, endPlanVertex, e, doneEdges);

		return out;
	}
	
	private void createEdgePart(Orthogonalization o, Direction direction, Vertex start, Vertex end, Map<DiagramElement, Direction> underlyings, List<DartDirection> out) {
		 List<Dart> darts = clc.buildDartsBetweenVertices(underlyings, o, start, end, direction);

		// convert to dart directions
		for (Dart d : darts) {
			out.add(new DartDirection(d, d.getDrawDirectionFrom(start)));
			start = d.otherEnd(start);
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
