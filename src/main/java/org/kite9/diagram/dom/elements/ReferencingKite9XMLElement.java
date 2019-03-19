package org.kite9.diagram.dom.elements;

import org.w3c.dom.Node;

/**
 * This includes functionality for connections, which reference other parts of the diagram.
 * For these, it's useful to be able to pull back a referenced element.
 */
public interface ReferencingKite9XMLElement extends StyledKite9XMLElement {

	String getIDReference(String referenceName);
	
	Node getNode(String referenceName);
}
