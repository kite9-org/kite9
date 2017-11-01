package org.kite9.diagram.batik.templater;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.Kite9XMLElement;

public interface Templater {
	
	/**
	 * This needs to copy the template XML source into the destination, expanding and applying any
	 * template logic as we go.
	 * 
	 * This is used when we construct the DiagramElement o.
	 */
	public void handleTemplateElement(Kite9XMLElement in, DiagramElement o);

}