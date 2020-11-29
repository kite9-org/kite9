package org.kite9.diagram.common.elements;

import org.kite9.diagram.model.DiagramElement;

/**
 * Represent all or part of one or more underlying (real) diagram elements.
 * 
 * @author robmoffat
 *
 */
public interface ConstructionElement {
		
	public boolean isPartOf(DiagramElement de);
	
}
