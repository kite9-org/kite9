package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.DiagramElement;

/**
 * Represent all or part of one or more underlying (real) diagram elements.
 * 
 * @author robmoffat
 *
 */
public interface ArtificialElement {
	
	public DiagramElement getOriginalUnderlying();
	
}
