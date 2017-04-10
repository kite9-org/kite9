package org.kite9.diagram.visualization.orthogonalization.vertices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.algorithms.Tools;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.mapping.SubGridCornerVertices;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.SideVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.compaction.insertion.SubGraphInsertionCompactionStep;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarizationBuilder;
import org.kite9.framework.common.Kite9ProcessingException;
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
	
	protected GridPositioner gp;
	
	protected ElementMapper em;
	
	public BasicVertexArranger(CompleteDisplayer cd, ElementMapper em) {
		super();
		this.gp = em.getGridPositioner();
		this.em = em;
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
		boolean sized = true; //sizer.requiresDimension(v.getOriginalUnderlying());
		
		boolean mulitpleHorizDarts = (dartDirections.get(Direction.LEFT).size() > 1) || 
			(dartDirections.get(Direction.RIGHT).size() > 1);
		
		boolean multipleVertDarts = (dartDirections.get(Direction.UP).size() > 1) || 
			(dartDirections.get(Direction.DOWN).size() > 1);
		
		convertDiagramElementToInnerFace(v.getOriginalUnderlying(), v, o, dartDirections, dartOrdering, sized || mulitpleHorizDarts || multipleVertDarts);
		RectangleRenderingInformation rri = (RectangleRenderingInformation) (v.getOriginalUnderlying()).getRenderingInformation();
		// temporarily set
		rri.setMultipleHorizontalLinks(mulitpleHorizDarts);
		rri.setMultipleVerticalLinks(multipleVertDarts);
	}
	
	protected void convertContainerContents(Orthogonalization o, Container c, DartFace inner) {
		if (c.getLayout() == Layout.GRID) {
			if (c.getContents().size() > 0) {
				DartFace outer = createGridFaceForContainerContents(o, c);
				inner.getUnderlying().getContainedFaces().add(outer.getUnderlying());
				outer.getUnderlying().setContainedBy(inner.getUnderlying());
				log.send("Created container contents outer face: "+outer.getUnderlying().id+" "+outer);
			}
		} else {
			Map<Direction, List<Dart>> emptyMap = getDartsInDirection(Collections.emptyList(), null);
			for (DiagramElement de : c.getContents()) {
				if (de instanceof Connected) {
					DartFace df = convertDiagramElementToInnerFace(de, null, o, emptyMap, Collections.emptyList(), false);
					DartFace outerFace = convertGridToOuterFace(o, getMultiCornerVertices(df), (Rectangular) de);
					inner.getUnderlying().getContainedFaces().add(outerFace.getUnderlying());
					outerFace.getUnderlying().setContainedBy(inner.getUnderlying());
					log.send("Created face: "+df);
					log.send("Created face: "+outerFace);
				}
			}
		}
	}
	
	private Set<MultiCornerVertex> getMultiCornerVertices(DartFace df) {
		Set<MultiCornerVertex> out = new HashSet<MultiCornerVertex>();
		for (Vertex v : df.getUnderlying().cornerIterator()) {
			if (v instanceof MultiCornerVertex) {
				out.add((MultiCornerVertex) v);
			}
		}
		
		return out;
	}


	/**
	 * This is a bit like duplication of the code in {@link RHDPlanarizationBuilder},
	 * but I think I'll live with it for now.
	 * 
	 * @return the outerface to embed in the container.
	 */
	private DartFace createGridFaceForContainerContents(Orthogonalization o, Container c) {
		Map<Direction, List<Dart>> emptyMap = getDartsInDirection(Collections.emptyList(), null);
		Set<MultiCornerVertex> createdVertices = new LinkedHashSet<>();
		placeContainerContentsOntoGrid(o, c, emptyMap, createdVertices);
		o.getAllVertices().addAll(createdVertices);
		return convertGridToOuterFace(o, createdVertices, c);
	}
	
	private void placeContainerContentsOntoGrid(Orthogonalization o, Container c, 
			Map<Direction, List<Dart>> emptyMap, Set<MultiCornerVertex> createdVertices) {

		gp.placeOnGrid(c, true);

		
		for (DiagramElement de : c.getContents()) {
			if (de instanceof Connected) {
				SubGridCornerVertices cv = (SubGridCornerVertices) em.getOuterCornerVertices(de);
				createdVertices.addAll(cv.getVerticesAtThisLevel());
				
				// having created all the vertices, join them to form faces
				MultiCornerVertex fromv, tov = null;
				List<MultiCornerVertex> perimeterVertices = gp.getClockwiseOrderedContainerVertices(cv);
				Iterator<MultiCornerVertex> iterator = perimeterVertices.iterator();
				Face f = o.getPlanarization().createFace();
				f.setPartOf((Rectangular) de);
				while (iterator.hasNext()) {
					fromv = tov;
					tov = iterator.next();
					if (fromv != null) {
						Edge e = createOrReuse(de, fromv, tov,  o, cv.getGridContainer());
						f.add(fromv, e);
					}
				}
				
				// join back into a circle
				Edge e = createOrReuse(de, 
						tov, 
						perimeterVertices.get(0), 
						o, cv.getGridContainer());
				f.add(tov, e);
					
				DartFace done = convertDiagramElementToInnerFaceWithCorners(de, null, o, emptyMap, Collections.emptyList(), de instanceof Leaf, f, cv);
				log.send("Created (grid) face: "+done); 
					
				if (de instanceof Container) {
					convertContainerContents(o, (Container) de, done); 
				}
			}
		}
	}



	private DartFace convertGridToOuterFace(Orthogonalization o, Set<MultiCornerVertex> createdVertices, Rectangular partOf) {
		Vertex current = getTopLeftVertex(createdVertices), orig = current;
		Direction d = Direction.DOWN;
		List<DartDirection> out = new ArrayList<>(); 
		do {
			Dart dart = getDartInDirection(current, d);
			if (dart == null) {
				// turn the corner when we reach the end of the side
				d = Direction.rotateAntiClockwise(d);
				dart = getDartInDirection(current, d);
			}
			
			out.add(new DartDirection(dart, d));
			
			if (dart == null) {
				throw new Kite9ProcessingException("Can't follow perimeter !"+current);
			}

			current = dart.otherEnd(current);
			
		} while (current != orig);
		
		Face outer = o.getPlanarization().createFace();
		outer.setOuterFace(true);
		outer.setPartOf(partOf);
		DartFace result = o.createDartFace(outer);
		result.dartsInFace = out;
		return result;
	}

	private MultiCornerVertex getTopLeftVertex(Set<MultiCornerVertex> createdVertices) {
		for (MultiCornerVertex vertex : createdVertices) {
			if ((vertex.getXOrdinal().equals(BigFraction.ZERO)) && (vertex.getYOrdinal().equals(BigFraction.ZERO))) {
				return vertex;
			}
		}
		
		throw new LogicException("Could not find top-left vertex");
	}
	
	private Edge getEdgeTo(MultiCornerVertex from, MultiCornerVertex to) {
		for (Edge e : from.getEdges()) {
			if (e.otherEnd(from) == to) {
				return e;
			}
		}
		
		return null;
	}
	
	private BorderEdge createOrReuse(DiagramElement de, MultiCornerVertex from, MultiCornerVertex to, Orthogonalization o, DiagramElement root) {
		BorderEdge be = (BorderEdge) getEdgeTo(from, to);
		Direction d = getDirection(from, to);
		
		if (be == null) {
			be = new BorderEdge(from, to, "be"+from+"-"+to, d, false, root, new LinkedHashMap<>());
			log.send("Created border edge"+be);
		}
		Direction borderSide = Direction.rotateAntiClockwise(d);
		be.getDiagramElements().put(de, borderSide);
		return be;
	}

	private Direction getDirection(MultiCornerVertex from, MultiCornerVertex to) {
		int horiz = from.getXOrdinal().compareTo(to.getXOrdinal());
		int vert = from.getYOrdinal().compareTo(to.getYOrdinal());
		if ((horiz == 0) && (vert == 0)) {
			throw new LogicException("Vertex overlap");
		} else if ((horiz != 0) && (vert != 0)) {
			throw new LogicException("Vertices are diagonal");
		} else if (horiz == 0) {
			return vert == -1 ? Direction.DOWN : Direction.UP;
		} else {
			return horiz == -1 ? Direction.RIGHT : Direction.LEFT;
		}
	}

	private Dart getDartInDirection(Vertex current, Direction d) {
		for (Edge e : current.getEdges()) {
			if ((e instanceof Dart) && (e.getDrawDirectionFrom(current) == d)) {
				return (Dart) e;
			}
		}
		
		return null;
	}

	protected DartFace convertDiagramElementToInnerFace(DiagramElement originalUnderlying, Vertex optionalExistingVertex, Orthogonalization o, Map<Direction, List<Dart>> dartDirections, List<Dart> dartOrdering, boolean requiresMinSize) {
		log.send(log.go() ? null : "Converting: " + originalUnderlying + " with edges: ", dartOrdering);
		
		CornerVertices cv = em.getOuterCornerVertices(originalUnderlying);
		
		o.getAllVertices().addAll(cv.getVerticesAtThisLevel());
		
		Face f = o.getPlanarization().createFace();
		f.setPartOf((Rectangular) originalUnderlying);
		Vertex tl = cv.getTopLeft(), tr = cv.getTopRight(), br = cv.getBottomRight(), bl = cv.getBottomLeft();
		
		f.add(tl, new BorderEdge(tl, tr, "be-"+tl+"-"+tr, Direction.RIGHT, false, originalUnderlying, mapFor(originalUnderlying, Direction.UP)));
		f.add(tr, new BorderEdge(tr, br, "be"+tr+"-"+br, Direction.DOWN, false, originalUnderlying, mapFor(originalUnderlying, Direction.RIGHT)));
		f.add(br, new BorderEdge(br, bl, "be"+br+"-"+bl, Direction.LEFT, false, originalUnderlying, mapFor(originalUnderlying, Direction.DOWN)));
		f.add(bl, new BorderEdge(bl, tl, "be"+bl+"-"+tl, Direction.UP, false, originalUnderlying, mapFor(originalUnderlying, Direction.LEFT)));
		f.checkFaceIntegrity();
		
		DartFace inner = convertDiagramElementToInnerFaceWithCorners(originalUnderlying, optionalExistingVertex, o, dartDirections, dartOrdering, requiresMinSize, f, cv);
		log.send("Created face: "+inner);
		
		// convert content
		if (originalUnderlying instanceof Container) {
			convertContainerContents(o, (Container) originalUnderlying, inner); 
		}
		
		return inner;
	}
	
	private Map<DiagramElement, Direction> mapFor(DiagramElement de, Direction d) {
		 Map<DiagramElement, Direction> out = new LinkedHashMap<>();
		 out.put(de, d);
		 return out;
	}

	private DartFace convertDiagramElementToInnerFaceWithCorners(DiagramElement originalUnderlying, Vertex optionalExistingVertex, Orthogonalization o, Map<Direction, List<Dart>> dartDirections, List<Dart> dartOrdering,
			boolean requiresMinSize, Face elementFace, CornerVertices cv) {
								
//		// create darts for the minimum size of the vertex
//		double xSize = 0;
//		double ySize = 0;
//		CostedDimension minimumSize = sizer.size(originalUnderlying, CostedDimension.UNBOUNDED);
//		if (minimumSize != CostedDimension.NOT_DISPLAYABLE) {
//			xSize = minimumSize.x();
//			ySize = minimumSize.y();
//			Dart dx = o.createDart(cv.getTopLeft(), cv.getTopRight(), null, Direction.RIGHT,  xSize);
//			Dart dy = o.createDart(cv.getTopLeft(), cv.getBottomLeft(), null, Direction.DOWN, ySize);
//			o.getAllDarts().add(dx);
//			o.getAllDarts().add(dy);
//		}
		
		Set<Vertex> allNewVertices = new UnorderedSet<Vertex>();
		LinkedHashSet<Dart> allSideDarts = new LinkedHashSet<Dart>();

		List<MultiCornerVertex> perimeter = gp.getClockwiseOrderedContainerVertices(cv);
		MultiCornerVertex start = perimeter.get(0);
		int done = 0;
		while (done < perimeter.size()) {
			MultiCornerVertex fromv = perimeter.get(done);
			MultiCornerVertex tov = perimeter.get((done+1) % perimeter.size());
			BorderEdge e = (BorderEdge) getEdgeFor(fromv, tov);
			Direction outwardsDirection = Direction.rotateAntiClockwise(e.getDrawDirectionFrom(fromv));
			List<Dart> leavers = dartDirections.get(outwardsDirection);
			List<Dart> oppositeSide = dartDirections.get(Direction.reverse(outwardsDirection));
			//double size = Direction.isVertical(outwardsDirection) ? xSize : ySize;
			double size = 0;	// since we don't always know whether the dart takes up the whole side of the perimeter
			Side s = convertEdgeToDarts(fromv, tov, outwardsDirection, originalUnderlying, optionalExistingVertex, leavers, o, oppositeSide.size(), size, requiresMinSize, e);
			allNewVertices.addAll(s.vertices);
			allSideDarts.addAll(s.newEdgeDarts);
			done ++;
		}

		if (allSideDarts.size() != allNewVertices.size()+perimeter.size())
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

		// remove the converted vertex from the orthogonalization
		o.getAllVertices().remove(optionalExistingVertex);

		if (dartOrdering.size() == 0) {
			createOuterFace(o, optionalExistingVertex, allSideDarts, start);
		}
		
		DartFace inner = createInnerFace(o, allSideDarts, start, elementFace);
		
		return inner;
	}

	private Edge getEdgeFor(MultiCornerVertex fromv, MultiCornerVertex tov) {
		for (Edge e : fromv.getEdges()) {
			if (e.meets(tov)) {
				return e;
			}
		}
		
		throw new LogicException("Couldn't find matching edge");
	}

	private DartFace createInnerFace(Orthogonalization o, LinkedHashSet<Dart> allSideDarts, Vertex start, Face f) {
		f.setOuterFace(false);
		DartFace inner = o.createDartFace(f);
		dartsToDartFace(allSideDarts, start, inner, false);
		return inner;
	}

	/**
	 * An unconnected vertex should also be an outer dart face in the diagram.
	 * The face exists, but the dart face currently does not.  By adding this, we ensure the vertex 
	 * will be added to the diagram by the {@link SubGraphInsertionCompactionStep}. 
	 */
	private DartFace createOuterFace(Orthogonalization o, Vertex v, LinkedHashSet<Dart> allDarts, Vertex vs) {
		DartFace df = null;
		if (v != null) {
			for (Face f : o.getPlanarization().getFaces()) {
				if (f.contains(v) && f.isOuterFace()) {
					df = o.createDartFace(f);
					break;
				}
			}
		} else {
			Face outer = o.getPlanarization().createFace();
			outer.setOuterFace(true);
			df = o.createDartFace(outer);
		}
		
		if (df != null) {
			dartsToDartFace(allDarts, vs, df, true); 
			
			return df;
		} else {
			throw new Kite9ProcessingException("Couldn't find dart face for "+v);
		}
	}

	private void dartsToDartFace(LinkedHashSet<Dart> allDarts, Vertex vs, DartFace df, boolean reverse) {
		df.dartsInFace = new ArrayList<DartDirection>(allDarts.size());
		
		for (Dart dart : allDarts) {
			Direction d = dart.getDrawDirectionFrom(vs);
			d = reverse ? Direction.reverse(d) : d;
			df.dartsInFace.add(new DartDirection(dart, d));
			vs = dart.otherEnd(vs);
		}
		
		if (reverse) {
			Collections.reverse(df.dartsInFace);
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

	protected Side convertEdgeToDarts(Vertex tl, Vertex tr, Direction d, DiagramElement underlying, Vertex optionalOriginal, List<Dart> onSide,
			Orthogonalization o,int oppDarts, double lengthOpt, boolean requiresMinSize, BorderEdge borderEdge) {
		int i = 0;
		Side out = new Side();
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
				vsv = createSideVertex(d, underlying, i, invisible);
				i++;

				Dart sideDart = createSideDart(underlying, o, last, segmentDirection, oppDarts, lengthOpt, j==0, vsv, onSide.size(), thisEdge, lastEdge, requiresMinSize, borderEdge);
				sideDart.setOrthogonalPositionPreference(d);
				out.newEdgeDarts.add(sideDart);
				out.vertices.add(vsv);
			} else {
				// reuse last vertex side
				vsv = out.vertices.get(out.vertices.size() - 1);
			}

			if (optionalOriginal != null) {
				replaceOriginal(o, dart, thisEdge, vsv, optionalOriginal);
			}
			
//			double len = dart.getLength();
//			double minLen = getMinimumDartLength(dart, thisEdge); 
//			dart.setLength(Math.max(len, minLen));
			
			last = vsv;
			lastEdge = thisEdge;
		}

		// finally, join to corner
		Dart sideDart = createSideDart(underlying, o, last, segmentDirection, oppDarts, lengthOpt, true, tr, onSide.size(), null, lastEdge, requiresMinSize, borderEdge);
		sideDart.setOrthogonalPositionPreference(d);
		out.newEdgeDarts.add(sideDart);
		return out;
	}

	private void replaceOriginal(Orthogonalization o, Dart dart, Edge thisEdge, Vertex vsv, Vertex from) {
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
	}

	protected Vertex createSideVertex(Direction d, DiagramElement underlying, int i, boolean invisible) {
		Vertex vsv;
		if (invisible) {
			vsv = new HiddenSideVertex(underlying.getID() + "/" + d.toString() + i, underlying);
		} else {
			vsv = new SideVertex(underlying.getID() + "/" + d.toString() + i, underlying);
		}
		return vsv;
	}

	protected Dart createSideDart(DiagramElement underlying, Orthogonalization o, Vertex last, Direction segmentDirection,
			int oppDarts, double lengthOpt, boolean endDart, Vertex vsv, int sideDarts, Edge currentEdge, Edge lastEdge, boolean requiresMinSize, Edge borderEdge) {
	
		// dist can be set for the first and last darts only if fixed length and
		// one dart on side
//		boolean knownLength = oppDarts<=1 && sideDarts <= 1;
//		double dist = requiresMinSize ? sizer.getLinkPadding(underlying, segmentDirection) : 0;
//		if (knownLength) {
//			double distDueToSize = Math.ceil(lengthOpt / (sideDarts + 1.0));
//			dist = Math.max(distDueToSize, dist); 
//		}

		Dart out =  o.createDart(last, vsv, borderEdge, segmentDirection);
//		out.setVertexLengthKnown(knownLength);
		return out;
	}

	protected void updateWaypointMap(Edge partOf, Vertex from, Vertex to, Orthogonalization o) {
		List<Vertex> waypoints = o.getWaypointMap().get(partOf);
		int index = waypoints.indexOf(from);
		if (index > -1) {
			waypoints.set(index, to);
		}
	}
	
//	/**
//	 * Ensures that there is enough length on the dart to include the terminator shape
//	 */
//	protected double getMinimumDartLength(Dart d, Edge underlyingEdge) {
//		Terminator terminatorFrom = getTerminator(underlyingEdge, d.getFrom());
//		Terminator terminatorTo = getTerminator(underlyingEdge, d.getTo());
//		
//		double fromSize = sizer.getTerminatorLength(terminatorFrom);
//		double toSize = sizer.getTerminatorLength(terminatorTo);
//		
//		return fromSize + toSize;
//	}

	protected Terminator getTerminator(Edge underlyingEdge, Vertex v) {
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
		return true;
	}

}
