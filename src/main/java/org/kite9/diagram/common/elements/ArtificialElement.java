package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.DiagramElement;

/**
 * Represent all or part of an underlying (real) diagram element.
 * 
 * @author robmoffat
 *
 */
public interface ArtificialElement {
	
	public DiagramElement getOriginalUnderlying();
}
