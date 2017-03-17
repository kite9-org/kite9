package org.kite9.diagram.style;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.xml.XMLElement;

/**
 * Allows for the conversion from an `XMLElement` to a Kite9 `DiagramElement` (used in layout).
 * 
 * @author robmoffat
 *
 */
public interface DiagramElementFactory {

	public DiagramElement createDiagramElement(XMLElement in, DiagramElement parent);
	
}
