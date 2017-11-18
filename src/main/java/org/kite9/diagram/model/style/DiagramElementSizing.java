package org.kite9.diagram.model.style;

/**
 * This is used to choose the right approach for laying out the diagram element.
 * 
 * 
 * 
 * @author robmoffat
 *
 */
public enum DiagramElementSizing {

	MINIMIZE, MAXIMIZE, 	// these apply to Container elements
	
	// these appply to decals
	SCALED, 		// used for decals, where the decal is scaled to the size of the parent
	ADAPTIVE, 		// used for decals, where the decal is sized at display time.
	
	UNSPECIFIED		// for anything else
	
}
