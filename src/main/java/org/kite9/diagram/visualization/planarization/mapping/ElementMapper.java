package org.kite9.diagram.visualization.planarization.mapping;

import java.util.Collection;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;

/**
 * Given original (user defined) diagram attr, returns the planarization element relating to it.
 */
public interface ElementMapper {
	
	public boolean hasNestedConnections(DiagramElement c);
	
	public boolean hasCornerVertices(DiagramElement c);
		
	public boolean requiresCornerVertices(DiagramElement c);
	
	public CornerVertices getCornerVertices(DiagramElement c);
    
    public Vertex getVertex(DiagramElement c); 
    
    public PlanarizationEdge getEdge(Connected from, Vertex vfrom, Connected to, Vertex vto, BiDirectional<Connected> element);
    
    /**
     * Used for debugging purposes
     */
    public Collection<Vertex> allVertices();

    /**
     * Useful function for returning the depth of a given element.
     */
    public int getContainerDepth(DiagramElement c);
    
}
