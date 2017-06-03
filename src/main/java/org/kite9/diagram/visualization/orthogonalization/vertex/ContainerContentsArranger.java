package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.mapping.SubGridCornerVertices;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.edge.ContainerLabelConverter;
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.edge.IncidentDart;
import org.kite9.diagram.visualization.orthogonalization.edge.Side;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
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
				outer.setContainedBy(inner);
				log.send("Created container contents outer face: "+outer);
			}
		} else {
			for (DiagramElement de : c.getContents()) {
				if (de instanceof Connected) {
					DartFace df = convertDiagramElementToInnerFace(de, o);
					DartFace outerFace = convertGridToOuterFace(o, df.getStartVertex(), (Rectangular) de);
					outerFace.setContainedBy(inner);
					log.send("Created face: "+df);
					log.send("Created face: "+outerFace);
				}
			}
		}
	}
	
	public DartFace convertGridToOuterFace(Orthogonalization o, Vertex startVertex, Rectangular partOf) {
		Vertex current = startVertex, orig = current;
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
		

		DartFace result = o.createDartFace(partOf, true, out);
		return result;
	}

	
	private Vertex getTopLeftVertex(Set<MultiCornerVertex> createdVertices) {
		for (MultiCornerVertex multiCornerVertex : createdVertices) {
			if ((MultiCornerVertex.isMin(multiCornerVertex.getXOrdinal())) &&
					(MultiCornerVertex.isMin(multiCornerVertex.getYOrdinal()))) {
				return multiCornerVertex;
			}
		}
		
		throw new Kite9ProcessingException();
	}

	/**
	 * This is a bit like duplication of the code in {@link RHDPlanarizationBuilder},
	 * but I think I'll live with it for now.
	 * 
	 * @return the outerface to embed in the container.
	 */
	private DartFace createGridFaceForContainerContents(Orthogonalization o, Container c) {
		Map<Direction, List<IncidentDart>> emptyMap = getDartsInDirection(Collections.emptyList(), null);
		Set<MultiCornerVertex> createdVertices = new LinkedHashSet<>();
		placeContainerContentsOntoGrid(o, c, emptyMap, createdVertices);
		Vertex startVertex = getTopLeftVertex(createdVertices);
		return convertGridToOuterFace(o, startVertex, c);
	}
	

	private void placeContainerContentsOntoGrid(Orthogonalization o, Container c, 
			Map<Direction, List<IncidentDart>> emptyMap, Set<MultiCornerVertex> createdVertices) {

		gp.placeOnGrid(c, true);

		// set up vertices for each grid element
		for (DiagramElement de : c.getContents()) {
			if ((de instanceof Connected) || (de instanceof Label)) {
				SubGridCornerVertices cv = (SubGridCornerVertices) em.getOuterCornerVertices(de);
				createdVertices.addAll(cv.getVerticesAtThisLevel());
			}
		}
				
		// link them together
		for (DiagramElement de : c.getContents()) {
			if (de instanceof Connected) {
				SubGridCornerVertices cv = (SubGridCornerVertices) em.getOuterCornerVertices(de);
				
				// having created all the vertices, join them to form faces
				List<MultiCornerVertex> perimeterVertices = gp.getClockwiseOrderedContainerVertices(cv);

				MultiCornerVertex prev = null, start = null;
				Side s = new Side();
				for (MultiCornerVertex current : perimeterVertices) {
					if (prev != null) {
						// create a dart between prev and current
						Direction d = getDirection(prev, current);
						ec.convertContainerEdge(de, o, prev, current, Direction.rotateAntiClockwise(d), d, s);
					} else {
						start = current;
					}
					
					prev = current;
				}
				
				Direction d = getDirection(prev, start);
				ec.convertContainerEdge(de, o, prev, start, Direction.rotateAntiClockwise(d), d, s);
				DartFace inner = createInnerFace(o, s.getDarts(), start, de);
				clc.handleContainerLabels(inner, de, o);

				
				if (de instanceof Container) {
					convertContainerContents(o, (Container) de, inner);
				}
				
			}
		}
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
	

	
}
