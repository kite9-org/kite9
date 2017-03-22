package org.kite9.diagram.visualization.compaction;

import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 *	Compaction is the process of taking an Orthogonalization of a diagram and setting the RenderingInformation
 *	objects on each DiagramElement, effectively giving dimensions to each item in the diagram so that it can be 
 *	rendered.
 * 
 * @author robmoffat
 */
public interface Compactor {
	
	public Compaction compactDiagram(Orthogonalization o);
	
	void compact(Rectangular r, Compaction c);

}
