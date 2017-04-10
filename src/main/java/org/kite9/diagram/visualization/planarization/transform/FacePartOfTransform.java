package org.kite9.diagram.visualization.planarization.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * This adds the partOf indication to faces.
 * 
 * @author robmoffat
 *
 */
public class FacePartOfTransform implements PlanarizationTransform, Logable {

	@Override
	public void transform(Planarization pl) {
		for (Face face : pl.getFaces()) {
			assignRectangular(face, pl, new HashSet<>());
		}
	}
	
	private Kite9Log log = new Kite9Log(this);


	@Override
	public String getPrefix() {
		return "FACE";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}


	private Rectangular assignRectangular(Face face, Planarization pl, Set<Face> visited) {
		visited.add(face);
		if (face.getPartOf() != null) {
			return face.getPartOf();
		}

		if (face.isOuterFace()) {
			Face container = face.getContainedBy();
			if (container != null) {
				Rectangular r = assignRectangular(container, pl, visited);
				face.setPartOf(r);
				log.send("Face " + face.id + " is part of " + r);
				return r;
			} else {
				return null;
			}
			
		} else {
			// we are going clockwise
			int size = face.vertexCount();
			for (int i = 0; i < size; i++) {
				Vertex from = face.getCorner(i);
				Edge leave = face.getBoundary(i);
				if (leave instanceof BorderEdge) {
					return determineInsideElementFromBorderEdge(face, from, leave);
				} else {
					// we can cross this edge into the other face
					List<Face> faces = pl.getEdgeFaceMap().get(leave);
					if (faces.size() != 2) {
						throw new Kite9ProcessingException("Why no 2 faces? " + faces);
					}
					
					if (!visited.contains(faces.get(0))) {
						Rectangular r = assignRectangular(faces.get(0), pl, visited);
						log.send("Face " + face.id + " is part of " + r); 
						face.setPartOf(r);
						return r;
					} else if (!visited.contains(faces.get(1))) {
						Rectangular r = assignRectangular(faces.get(1), pl, visited);
						log.send("Face " + face.id + " is part of " + r); 
						face.setPartOf(r);
						return r;
					}
					
				}

			}

			throw new Kite9ProcessingException("Couldn't determine rectangular for "+face);
		}
	}

	private Rectangular determineInsideElementFromBorderEdge(Face face, Vertex from, Edge leave) {
		Direction d = leave.getDrawDirectionFrom(from);

		if (d == null) {
			throw new Kite9ProcessingException("Borders should always have direction set: " + leave);
		}

		Direction inside = Direction.rotateAntiClockwise(d);
		Map<DiagramElement, Direction> meetsMap = ((BorderEdge) leave).getDiagramElements();
		DiagramElement insideElement = getKeyForValue(meetsMap, inside);
		Rectangular rect = (Rectangular) insideElement;
		face.setPartOf(rect);
		log.send("Face " + face.id + " is part of " + rect);
		return rect;
	}

	private DiagramElement getKeyForValue(Map<DiagramElement, Direction> meetsMap, Direction inside) {
		for (Map.Entry<DiagramElement, Direction> ent : meetsMap.entrySet()) {
			if (ent.getValue() == inside) {
				return ent.getKey();
			}
		}
		
		throw new Kite9ProcessingException("Couldn't determine inside element: "+inside+" "+meetsMap);
	}
}
