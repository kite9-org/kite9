package org.kite9.diagram.visualization.orthogonalization;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;


public interface Orthogonalization extends Serializable {

	/**
	 * Order of darts around a dimensioned vertex, from top-left corner clockwise.
	 */
	public Map<Vertex, List<Dart>> getDartOrdering();

	/**
	 * Intermediate turning-point vertices in from-to edge order for each edge.
	 */
	public Map<Edge, List<Vertex>> getWaypointMap();

	/**
	 * List of all darts constructing the Orthogonalization.
	 */
	public Set<Dart> getAllDarts();

	/**
	 * All vertices used in the Orthogonalization, including edge corner vertices and vertex boundary vertices
	 * @return
	 */
	public Collection<Vertex> getAllVertices();

	/**
	 * Dart-perimeter faces of the Orthogonalization
	 */		
	public List<DartFace> getFaces();
	
	/**
	 * All edges (i.e. not darts) taken from the planarization.
	 */
	public Set<Edge> getEdges();
	
	/**
	 * Orthogonalization acts as a factory for darts. Always returns a dart, even if it 
	 * is an existing one
	 */
	public Dart createDart(Vertex from, Vertex to, Object partOf, Direction d);

	/**
	 * In the same way as a {@link Face} is a clockwise ordering of edges, a {@link DartFace} is a clockwise
	 * ordering of darts, created in the orthogonalization process
	 */
	public DartFace createDartFace(Face f);
	
	/**
	 * Helper vertices are added by the compaction process to ensure separation of all attr
	 * @return
	 */
	public Vertex createHelperVertex();
	
	/**
	 * Gets the underlying planarization for this orthogonalization
	 */
	public Planarization getPlanarization();

	/**
	 * Returns the DartFace representing this planarization face.
	 */
	public DartFace getDartFaceForFace(Face f);

}
