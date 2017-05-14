package org.kite9.diagram.visualization.orthogonalization;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;


public interface Orthogonalization extends Serializable {

	/**
	 * List of all darts constructing the Orthogonalization.
	 */
	public Set<Dart> getAllDarts();

	/**
	 * All vertices used in the Orthogonalization, including edge corner vertices and vertex boundary vertices.
	 * i.e. vertices that meet darts.
	 */
	public Collection<Vertex> getAllVertices();

	/**
	 * Dart-perimeter faces of the Orthogonalization
	 */		
	public List<DartFace> getFaces();
	
	/**
	 * Orthogonalization acts as a factory for darts. Always returns a dart, even if it 
	 * is an existing one
	 */
	public Dart createDart(Vertex from, Vertex to, DiagramElement partOf, Direction d, Direction partOfSide);
	public Dart createDart(Vertex from, Vertex to, Set<DiagramElement> partOf, Direction d, Direction partOfSide);
	public Dart createDart(Vertex from, Vertex to, Map<DiagramElement, Direction> partOf, Direction d);

	/**
	 * In the same way as a {@link Face} is a clockwise ordering of edges, a {@link DartFace} is a clockwise
	 * ordering of darts, created in the orthogonalization process.  
	 * 
	 * Outer faces also still exist, in order to be embedded within other faces.  These are anti-clockwise 
	 * ordered (though it's irrelevant).
	 */
	public DartFace createDartFace(Rectangular partOf, boolean outerFace);

	/**
	 * Gets the underlying planarization for this orthogonalization
	 */
	public Planarization getPlanarization();
	
	public Set<Dart> getDartsForDiagramElement(DiagramElement e);
	
	public List<Vertex> getWaypointsForBiDirectional(DiagramElement e);

	public List<DartFace> getDartFacesForRectangular(Rectangular r);

}
