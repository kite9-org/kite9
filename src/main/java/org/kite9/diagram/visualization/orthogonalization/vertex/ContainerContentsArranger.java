package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.mapping.SubGridCornerVertices;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.compaction.insertion.SubGraphInsertionCompactionStep;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarizationBuilder;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

/**
 * Handles the conversion of elements not in the planarization into Darts and DartFaces.  These could have been
 * ommitted from the planarization because they are text labels, or parts of the diagram that don't need 
 * connections.
 * 
 * @author robmoffat
 *
 */
public class ContainerContentsArranger extends MultiCornerVertexArranger {
	
	public ContainerContentsArranger(ElementMapper em) {
		super(em);
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
					DartFace df = convertDiagramElementToInnerFace(de, null, o, emptyMap, Collections.emptyList());
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
						PlanarizationEdge e = createOrReuse(de, fromv, tov,  o, cv.getGridContainer());
						f.add(fromv, e);
					}
				}
				
				// join back into a circle
				PlanarizationEdge e = createOrReuse(de, 
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


	private BorderEdge createOrReuse(DiagramElement de, MultiCornerVertex from, MultiCornerVertex to, Orthogonalization o, DiagramElement root) {
		BorderEdge be = (BorderEdge) getEdgeTo(from, to);
		Direction d = getDirection(from, to);
		
		if (be == null) {
			be = new BorderEdge(from, to, "be"+from+"-"+to, d, false, new LinkedHashMap<>());
			log.send("Created border edge"+be);
		}
		Direction borderSide = Direction.rotateAntiClockwise(d);
		be.getDiagramElements().put(de, borderSide);
		return be;
	}
	

	
	
	private Edge getEdgeTo(MultiCornerVertex from, MultiCornerVertex to) {
		for (Edge e : from.getEdges()) {
			if (e.otherEnd(from) == to) {
				return e;
			}
		}
		
		return null;
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
	

	private Edge getEdgeFor(MultiCornerVertex fromv, MultiCornerVertex tov) {
		for (Edge e : fromv.getEdges()) {
			if (e.meets(tov)) {
				return e;
			}
		}
		
		throw new LogicException("Couldn't find matching edge");
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


	private Map<DiagramElement, Direction> mapFor(DiagramElement de, Direction d) {
		 Map<DiagramElement, Direction> out = new LinkedHashMap<>();
		 out.put(de, d);
		 return out;
	}
	
}
