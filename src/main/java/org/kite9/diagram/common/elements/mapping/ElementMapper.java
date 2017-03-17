package org.kite9.diagram.common.elements.mapping;

import java.util.Collection;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.DiagramElement;

/**
 * Given original (user defined) diagram element, returns the planarization element(s) relating to it.
 */
public interface ElementMapper {
	
	public GridPositioner getGridPositioner();
		
	public boolean hasOuterCornerVertices(DiagramElement c);
		
	public boolean requiresPlanarizationCornerVertices(DiagramElement c);
	
	public CornerVertices getOuterCornerVertices(DiagramElement c);
    
    public Vertex getPlanarizationVertex(DiagramElement c); 
    
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
