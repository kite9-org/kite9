package org.kite9.diagram.dom.model;

import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.model.DiagramElement;

/**
 * Allows for the conversion from an `XMLElement` to a Kite9 `DiagramElement` (used in layout).
 * 
 * @author robmoffat
 *
 */
public interface DiagramElementFactory {

	public DiagramElement createDiagramElement(Kite9XMLElement in, DiagramElement parent);
	
}
