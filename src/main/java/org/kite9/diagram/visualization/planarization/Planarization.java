package org.kite9.diagram.visualization.planarization;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;


/**
 * A {@link Planarization} is a one dimensional ordering of Vertices, as well as the ordering of edges around each vertex
 * and the dual (i.e list of faces).
 * 
 * @author robmoffat
 *
 */
public interface Planarization {

	/**
	 * Returns the ordering of vertices within the Planarization
	 */
	public Collection<Vertex> getAllVertices();
	
	/**
	 * Returns the list of faces within the Planarization 
	 */
	public List<Face> getFaces();
	
	/**
	 * Each edge connects to one face, or two faces after the temporary directed edge are inserted.
	 */
	public Map<Edge, List<Face>> getEdgeFaceMap();
	
	/**
	 * Gets the details of which vertices belong to which faces.
	 */
	public Map<Vertex, List<Face>> getVertexFaceMap();
	
	/**
	 * Needs to return the clockwise ordering of edges meeting a vertex or a container.  
	 */
	public Map<Object, EdgeOrdering> getEdgeOrderings();
	
	/**
	 * Returns all edges in the planarization.
	 */
	public List<Edge> getAllEdges();
	
	/**
	 * Any connections or layout that haven't yet, or can't be introduced into the planar embedding.
	 */
	public Collection<BiDirectional<Connected>> getUninsertedConnections();
	
	/**
	 * Simply removes the edge from a planarization, without respect for any 
	 * data structures.
	 */
	public void removeEdge(Edge e);
	
	/**
	 * Manages the mapping of edges to diagram attr.   
	 */
	public Map<DiagramElement, EdgeMapping> getEdgeMappings();
	
	/**
	 * Returns the diagram itself
	 */
	public Diagram getDiagram();
	
	/**
	 * Creates an empty face in the planarization
	 */
	public Face createFace();
	
	/**
	 * Returns details about where the attr in the diagram have been placed for routing purposes
	 */
	public RoutingInfo getPlacedPosition(DiagramElement de);
}
