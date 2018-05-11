package org.kite9.framework.dom;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.dom.elements.Kite9XMLElement;

/**
 * Allows for the conversion from an `XMLElement` to a Kite9 `DiagramElement` (used in layout).
 * 
 * @author robmoffat
 *
 */
public interface DiagramElementFactory {

	public DiagramElement createDiagramElement(Kite9XMLElement in, DiagramElement parent);
	
}
