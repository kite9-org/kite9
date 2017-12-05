package org.kite9.diagram.model.style;

/**
 * This is used to choose the right approach for laying out the diagram element.
 * Currently, this is only used for containers.
 * 
 * When elements are part of a grid, MINIMIZE has priority over MAXIMIZE.
 * 
 * @author robmoffat
 *
 */
public enum DiagramElementSizing {

	MINIMIZE, MAXIMIZE 	// these apply to Container elements
	
}
