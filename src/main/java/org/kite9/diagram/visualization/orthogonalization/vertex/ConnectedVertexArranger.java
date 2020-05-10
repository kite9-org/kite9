package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.ConnectedVertex;
import org.kite9.diagram.common.elements.vertex.DartJunctionVertex;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.segment.Side;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.edge.FanningEdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.edge.IncidentDart;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This converts a ConnectedVertex to a face, and darts, which can then be added to the Orthogonalization.
 * 
 * @author robmoffat
 */
public class ConnectedVertexArranger extends AbstractVertexArranger implements Logable, VertexArranger {
	
	protected GridPositioner gp;
	
	protected EdgeConverter ec;
	
	protected EdgeConverter clc;
	
	protected Kite9Log log = new Kite9Log(this);
	
	public EdgeConverter getContainerLabelConverter() {
		return clc;
	}

	public ConnectedVertexArranger(ElementMapper em) {
		super(em);
		this.gp = em.getGridPositioner();
		FanningEdgeConverter ec = new FanningEdgeConverter(this, em);
//		LabellingEdgeConverter ec = new LabellingEdgeConverter(this, em);  
		this.ec = ec;
		this.clc = ec;
	}

	public static final int INTER_EDGE_SEPARATION = 0;

	protected int newVertexId;

	/**
	 * This implementation handles ConnectedVertex implementations embedded in the planarization.
	 */
	protected DartFace convertVertex(Orthogonalization o, Vertex v, TurnInformation ti) {
		if (!(v instanceof ConnectedVertex)) 
			throw new LogicException();
	
		Map<Direction, List<IncidentDart>> dartDirections = getDartsInDirection(v, o, ti);
		Connected originalUnderlying = ((ConnectedVertex) v).getOriginalUnderlying();
		DartFace out = convertDiagramElementToInnerFace(originalUnderlying, v, o, dartDirections);
		List<IncidentDart> allIncidentDarts = convertToDartList(dartDirections);
		log.send("Converting vertex: "+v, allIncidentDarts);
		setupBoundariesFromIncidentDarts(allIncidentDarts, v);
		return out;
	}
	
	protected List<IncidentDart> convertToDartList(Map<Direction, List<IncidentDart>> dartDirections) {
		List<IncidentDart> out = new ArrayList<>();
		out.addAll(dartDirections.get(Direction.UP));
		out.addAll(dartDirections.get(Direction.RIGHT));
		out.addAll(dartDirections.get(Direction.DOWN));
		out.addAll(dartDirections.get(Direction.LEFT));
		return out;
	}
	
	@Override
	public boolean needsConversion(Vertex v) {
		return (v instanceof ConnectedVertex);
	}

	@Override
	public List<DartDirection> returnAllDarts(Vertex v, Orthogonalization o) {
		if (!(v instanceof ConnectedVertex)) 
			throw new LogicException();
		
		Connected c = ((ConnectedVertex) v).getOriginalUnderlying();
		List<DartFace> faces = o.getDartFacesForRectangular(c);
		
		if (faces.size() == 1) {
			return faces.get(0).getDartsInFace();
		} else if (faces.size() > 1) {
			throw new LogicException();
		} else {
			DartFace out = convertVertex(o, v, new TurnInformation() {
				
				@Override
				public Direction getIncidentDartDirection(Edge e) {
					return null;
				}

				@Override
				public Edge getFirstEdgeClockwiseEdgeOnASide() {
					return null;
				}

				@Override
				public boolean doesEdgeHaveTurns(Edge e) {
					return false;
				}
			});
			
			return out.getDartsInFace();
		}
	}
	
	
	protected void setupBoundariesFromIncidentDarts(List<IncidentDart> dartOrdering, Vertex v) {
		Set<Vertex> externalVertices = dartOrdering.stream().map(id -> id.getExternal()).collect(Collectors.toSet());
		setupBoundaries(externalVertices, v);
	}
	
	/**
	 * Creates a dart or darts that arrives at the vertex.  Where more than one dart is created, return just the dart hitting
	 * the vertex.
	 */
	protected IncidentDart convertEdgeToIncidentDart(PlanarizationEdge e, Set<DiagramElement> cd, Orthogonalization o, Direction incident, int idx, Vertex und, int straightCount) {
		Vertex sideVertex = createSideVertex(cd, und);
		Vertex externalVertex = createExternalVertex(e, und);
		Direction fan = null;
		if ((idx != -1) && (straightCount > 1)) {
			Direction lowerOrders = Direction.rotateClockwise(incident);
			Direction higherOrders = Direction.rotateAntiClockwise(incident);

			int lower = (int) Math.floor(((double) straightCount / 2d) - 1d);
			int higher = (int) Math.ceil((double) straightCount / 2d);

			fan = idx <= lower ? lowerOrders : ((idx >= higher) ? higherOrders : null);
		}

		return ec.convertPlanarizationEdge(e, o, incident, externalVertex, sideVertex, und, fan);
	}

	protected Vertex createSideVertex(Set<DiagramElement> cd, Vertex und) {
		Set<DiagramElement> underlyings = new HashSet<>();
		underlyings.addAll(cd);
		underlyings.addAll(und.getDiagramElements());
		
		Vertex sideVertex = new DartJunctionVertex(und.getID()+"-dv-"+newVertexId++, underlyings);
		return sideVertex;
	}
	
	public DartFace convertDiagramElementToInnerFace(DiagramElement original, Orthogonalization o) {
		return convertDiagramElementToInnerFace(original, null, o, new HashMap<>());
	}

	protected DartFace convertDiagramElementToInnerFace(DiagramElement originalUnderlying, Vertex optionalExistingVertex, Orthogonalization o, Map<Direction, List<IncidentDart>> dartDirections) {
		log.send(log.go() ? null : "Converting: " + originalUnderlying + " with edges: ", dartDirections);
		
		CornerVertices cv = em.getOuterCornerVertices(originalUnderlying);

		List<MultiCornerVertex> perimeter = gp.getClockwiseOrderedContainerVertices(cv);
		return convertDiagramElementToInnerFaceWithCorners(originalUnderlying, o, dartDirections, perimeter);
	}

	protected DartFace convertDiagramElementToInnerFaceWithCorners(DiagramElement originalUnderlying, Orthogonalization o, Map<Direction, List<IncidentDart>> dartDirections, List<MultiCornerVertex> perimeter) {
		List<Dart> allSideDarts = new ArrayList<Dart>();
		Direction sideDirection = Direction.RIGHT;  // initial direction of perimeter
		MultiCornerVertex start = perimeter.get(0);
		int done = 0;
		while (done < perimeter.size()) {
			MultiCornerVertex fromv = perimeter.get(done);
			MultiCornerVertex tov = perimeter.get((done+1) % perimeter.size());
			Direction outwardsDirection = Direction.rotateAntiClockwise(sideDirection);
			List<IncidentDart> leavers = dartDirections.get(outwardsDirection);
			leavers = leavers == null ? Collections.emptyList() : leavers;
			Map<DiagramElement, Direction> underlyings = Collections.singletonMap(originalUnderlying, outwardsDirection);
			List<Dart> s = createSide(fromv, tov, leavers, o, sideDirection, underlyings);
			allSideDarts.addAll(s);
			done ++;
			sideDirection = Direction.rotateClockwise(sideDirection);
		}

		DartFace inner = createInnerFace(o, allSideDarts, start, originalUnderlying);
		log.send("Created face: "+inner); 
		
		// convert content
		if (originalUnderlying instanceof Container) {
			convertContainerContents(o, (Container) originalUnderlying, inner); 
		}
		
		return inner;
	}
	
	protected void convertContainerContents(Orthogonalization o, Container originalUnderlying, DartFace inner) {
		// does nothing in this implementation - see ContainerContentsArranger
	}
	
	@Override
	public DartFace convertToOuterFace(Orthogonalization o, Vertex startVertex, Rectangular partOf) {
		throw new LogicException("Not implemented");
	}

	protected DartFace createInnerFace(Orthogonalization o, List<Dart> allSideDarts, Vertex start, DiagramElement de) {
		List<DartDirection>  dd = dartsToDartFace(allSideDarts, start, false);
		DartFace inner = o.createDartFace((Rectangular) de, false, dd);
		return inner;
	}
	
	private List<DartDirection> dartsToDartFace(List<Dart> allDarts, Vertex vs, boolean reverse) {
		List<DartDirection> dartsInFace = new ArrayList<DartDirection>(allDarts.size());
		
		for (Dart dart : allDarts) {
			Direction d = dart.getDrawDirectionFrom(vs);
			d = reverse ? Direction.reverse(d) : d;
			dartsInFace.add(new DartDirection(dart, d));
			vs = dart.otherEnd(vs);
		}
		
		if (reverse) {
			Collections.reverse(dartsInFace);
		}
		
		return dartsInFace;
	}

	/**
	 * Note - we can't just group otherwise order gets broken.  We must ensure the sides are still in order
	 */
	protected Map<Direction, List<IncidentDart>> getDartsInDirection(Vertex from, Orthogonalization o, TurnInformation ti) {
		EdgeOrdering eo = o.getPlanarization().getEdgeOrderings().get(from);
		Set<DiagramElement> cd = from.getDiagramElements();
		
		List<PlanarizationEdge> processOrder = eo.getEdgesAsList();
		Edge startEdge = ti.getFirstEdgeClockwiseEdgeOnASide();
		int startPoint = startEdge != null ? processOrder.indexOf(startEdge) : 0;
		
		Map<Direction, List<PlanarizationEdge>> in = new LinkedHashMap<>();
		in.put(Direction.UP, new ArrayList<>());
		in.put(Direction.DOWN, new ArrayList<>());
		in.put(Direction.LEFT, new ArrayList<>());
		in.put(Direction.RIGHT, new ArrayList<>());
		
		for (int i = 0; i < processOrder.size(); i++) {
			PlanarizationEdge planarizationEdge = processOrder.get((i + startPoint ) % processOrder.size());
			in.get(Direction.reverse(ti.getIncidentDartDirection(planarizationEdge))).add(planarizationEdge);
		}
		
		Map<Direction, List<IncidentDart>> out = new HashMap<>();
		
		for (Direction dir : in.keySet()) {
			List<PlanarizationEdge> list = in.get(dir);
			List<IncidentDart> outList = new ArrayList<>(in.size());
			List<Set<PlanarizationEdge>> fanBuckets = createFanBuckets(list, ti);
			long straightCount = fanBuckets.size();
			for (int i = 0; i < list.size(); i++) {
				PlanarizationEdge current = list.get(i);
				int idx = getFanBucket(current, fanBuckets);
				IncidentDart id = convertEdgeToIncidentDart(current, cd, o, ti.getIncidentDartDirection(current), idx, from, (int) straightCount);
				outList.add(id);
			}
			out.put(dir, outList);
		}
		
		return out;
	}

	private int getFanBucket(PlanarizationEdge current, List<Set<PlanarizationEdge>> fanBuckets) {
		for (int i = 0; i < fanBuckets.size(); i++) {
			if (fanBuckets.get(i).contains(current)) {
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * Arranges the {@link PlanarizationEdge}s into buckets of same destination.   All elements to same destination
	 * will get the same fan-style.
	 */
	private List<Set<PlanarizationEdge>> createFanBuckets(List<PlanarizationEdge> in, TurnInformation ti) {
		List<Set<PlanarizationEdge>> out = new ArrayList<>();
		Set<PlanarizationEdge> currentSet = new HashSet<>();
		PlanarizationEdge last = null;
		
		for (PlanarizationEdge pe : in) {
			if (!ti.doesEdgeHaveTurns(pe)) {
				if ((last == null) || (!last.meets(pe.getFrom())) || (!last.meets(pe.getTo()))) {
					// new bucket required
					currentSet = new HashSet<>();
					out.add(currentSet);
					currentSet.add(pe);
					last = pe;
				} else {
					currentSet.add(pe);
				}
			}
		}
		
		return out;
	}

	/**
	 * Links corner elements with the incident darts into the whole side of the underlying.
	 */
	protected List<Dart> createSide(Vertex from, Vertex to, List<IncidentDart> onSide, Orthogonalization o, Direction goingIn, Map<DiagramElement, Direction> underlyings) {
		List<Dart> out = new ArrayList<Dart>();
		Vertex last = from;
		IncidentDart incidentDart = null;
		if (underlyings.size() > 1) {
			throw new LogicException("Doesn't support multiple underlyings");
		}

		DiagramElement singleUnderlying = underlyings.keySet().iterator().next();
		Map<DiagramElement, List<Dart>> singletonMap = Collections.singletonMap(singleUnderlying, out);
		
		if (onSide != null) {
			for (int j = 0; j < onSide.size(); j++) {
				incidentDart = onSide.get(j);
				Vertex vsv = incidentDart.getInternal();
				ec.convertContainerEdge(underlyings, o, last, vsv, goingIn, out);
				addLabelsToContainerDart(o, singleUnderlying, singletonMap);
				last = vsv;
			}
		}

		// finally, join to corner
		ec.convertContainerEdge(underlyings, o, last, to, goingIn, out);
		addLabelsToContainerDart(o, singleUnderlying, singletonMap);
		return out;
	}

	public String getPrefix() {
		return "FACE";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

	
	/**
	 * Adds labels to container edges, if the container has a label.
	 * We add the label by splitting the first dart we find.
	 */
	public void addLabelsToContainerDart(Orthogonalization o, DiagramElement de, Map<DiagramElement, List<Dart>> sides) {
		List<Dart> originals = new ArrayList<Dart>(sides.get(de));
		for (Dart dart : originals) {
			Direction sideDirection = dart.getDiagramElements().get(de);
			
			if (sideDirection != null) {
				List<Dart> s = sides.get(de);
				
				// clockwise direction around container
				Direction d = Direction.rotateClockwise(sideDirection);
				Vertex end1 = dart.getDrawDirection() == d ? dart.getFrom() : dart.getTo();
				Vertex end2 = dart.getDrawDirection() == d ? dart.getTo() : dart.getFrom();
	
				// only label non-grid elements. This is because grid edges can be shared
				// between containers, and we don't know how to
				// figure out label positioning in this case yet.
				Label l = findUnprocessedLabel(de, sideDirection);
				while (l != null) {
					CornerVertices cv = em.getOuterCornerVertices(l);
					Vertex[] waypoints = rotateWaypointsCorrectly(cv, d);
					
					Pair<Dart> p1 = o.splitDart(dart, waypoints[0]);
					Dart p1keep = p1.getA().meets(end1) ? p1.getA() : p1.getB();
					Dart p1change = p1.getA().meets(end1) ? p1.getB() : p1.getA();
					Pair<Dart> p2 = o.splitDart(p1change, waypoints[3]);
					Dart p2keep = p2.getA().meets(end2) ? p2.getA() : p2.getB();
										
					DartFace df = convertDiagramElementToInnerFace(l, o);
					
					// now we need to modify the dart face to skirt around this new label
					List<Dart> newElements = clockwiseElementsBetween(df, waypoints[0], waypoints[3]);
					List<Dart> otherSideElements = clockwiseElementsBetween(df, waypoints[3], waypoints[0]);
					newElements.add(0, p1keep);
					newElements.add(p2keep);
					
					int idx = s.indexOf(dart);
					s.addAll(idx, newElements);
					s.remove(dart);
					
					// check for dart in other sides.  Only gets called for grid.
					for (List<Dart> otherSide : sides.values()) {
						if ((otherSide != s) && (otherSide.contains(dart))) {
							otherSideElements.add(0, p2keep);
							otherSideElements.add(p1keep);
							idx = otherSide.indexOf(dart);
							otherSide.addAll(idx, otherSideElements);
							otherSide.remove(dart);
						}
					}
					
					l = findUnprocessedLabel((Container) de, sideDirection);
					dart = p1keep;
				}
			}
		}

	}

	private List<Dart> clockwiseElementsBetween(DartFace df, Vertex from, Vertex to) {
		int startIndex = 0;
		int endIndex = 0;
		List<Dart> out = new ArrayList<Dart>();
	
		for (int i = 0; i < df.getDartsInFace().size(); i++) {
			DartDirection dd = df.getDartsInFace().get(i);
			Vertex ddTo = dd.getDirection() == dd.getDart().getDrawDirection() ? dd.getDart().getTo() : dd.getDart().getFrom();
			if (ddTo == from) {
				startIndex = i;
			} 
			
			if (ddTo == to) {
				endIndex = i;
			}
		}
		
		int i = startIndex;
		
		while (i != endIndex) {
			out.add(df.getDartsInFace().get(i).getDart());
			i = i == 0 ? df.getDartsInFace().size() - 1 : i - 1;
		}
		
		return out;
	}

	/**
	 * Waypoints is ordered if d is left (i.e. the label is at the bottom) However,
	 * it could be in any direction.
	 */
	private Vertex[] rotateWaypointsCorrectly(CornerVertices cv, Direction d) {
		List<Vertex> wp = Arrays.asList(cv.getBottomRight(), cv.getTopRight(), cv.getTopLeft(), cv.getBottomLeft());
		while (d != Direction.LEFT) {
			Collections.rotate(wp, -1);
			d = Direction.rotateClockwise(d);
		}

		return (Vertex[]) wp.toArray(new Vertex[wp.size()]);
	}

	private Label findUnprocessedLabel(DiagramElement c, Direction d) {
		if (c instanceof Container) {
			for (DiagramElement de : ((Container) c).getContents()) {
				if ((de instanceof Label) && (((Label) de).getLabelPlacement().containerLabelPlacement(d))) {
					if (!em.hasOuterCornerVertices(de)) {
						return (Label) de;
					}
				}
			}
		}

		return null;
	}

}
