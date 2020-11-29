package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.Collections;
import java.util.Set;

import org.kite9.diagram.common.elements.vertex.AbstractVertex;
import org.kite9.diagram.common.elements.vertex.NoElementVertex;
import org.kite9.diagram.model.DiagramElement;



/**
 * This is used where a routing goes from one side of the planarization axis to the other.
 * 
 * @author robmoffat
 */
public class PlanarizationCrossingVertex extends AbstractVertex implements NoElementVertex {
	
	public PlanarizationCrossingVertex(String name) {
		super(name);
	}

	public DiagramElement getOriginalUnderlying() {
		return null;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return false;
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return Collections.emptySet();
	}

	
	
}