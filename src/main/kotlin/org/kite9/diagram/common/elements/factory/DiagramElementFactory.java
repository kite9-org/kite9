package org.kite9.diagram.common.elements.factory;

import org.kite9.diagram.model.DiagramElement;

/**
 * Allows for the conversion from an `XMLElement` to a Kite9 `DiagramElement` (used in layout).
 * 
 * @author robmoffat
 *
 */
public interface DiagramElementFactory<X> {

	public DiagramElement createDiagramElement(X in, DiagramElement parent);

	public TemporaryConnected createTemporaryConnected(DiagramElement parent, String idSuffix);
	
}
