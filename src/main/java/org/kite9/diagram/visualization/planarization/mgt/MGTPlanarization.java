package org.kite9.diagram.visualization.planarization.mgt;

import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarization;

/**
 * Specific methods for GT-style planarization, in which the initial planarization is based on 
 * an ordered list of vertices.
 * 
 * @author moffatr
 * 
 */
public interface MGTPlanarization extends RHDPlanarization {

    public boolean isAdjacency(Edge edge);

    public boolean crosses(Edge edge, boolean above);

    public boolean crosses(float i1, float i2, boolean above);

    /**
	 * Introduces a new vertex into the ordering after point i.
	 */
	public void addVertexToOrder(int i, Vertex insert);
	
	public void removeVertexFromOrder(Vertex v);
	
    public int getVertexIndex(Vertex v);
    
    public List<Vertex> getVertexOrder();
    
    public void addEdge(Edge toAdd, boolean above, Edge outsideOf);
    
    /**
     * For a given vertex, returns edges leaving vertex above the line of the planarization
     * going forwards, in inside-most to outside-most order.
     */
    public List<Edge> getAboveForwardLinks(Vertex v);
	
	public List<Edge> getAboveBackwardLinks(Vertex v);
	
	public List<Edge> getBelowForwardLinks(Vertex v);
	
	public List<Edge> getBelowBackwardLinks(Vertex v);
	
	public Set<Edge> getAboveLineEdges();
	
	public Set<Edge> getBelowLineEdges();
	
	public Edge getFirstEdgeAfterPlanarizationLine(Vertex from, boolean forwardSet, boolean aboveSet);
}
