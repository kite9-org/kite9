package org.kite9.diagram.visualization.planarization.mapping;

import java.util.Collection;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.style.DiagramElement;

/**
 * Given original (user defined) diagram attr, returns the planarization element relating to it.
 */
public interface ElementMapper {
	
	public ContainerVertices getContainerVertices(Container c);
    
    public Vertex getVertex(Connected c); 
    
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
