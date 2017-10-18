package org.kite9.diagram.batik.templater;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface Templater {

	interface ValueReplacer {
		
		public String getReplacementValue(String prefix, String attr);	
		
		public String getText();
		
	}

	public void performReplace(Node child, ValueReplacer valueReplacer);

	
	/**
	 * This needs to copy the template XML source into the destination.
	 * This is used when we construct the DiagramElement o.
	 */
	public void handleTemplateElement(Kite9XMLElement in, DiagramElement o);

	/**
	 * Used for constructing the output XML, in the SVG namespace.
	 */
	Node transcribeNode(Document dest, Node source, boolean removePrefix);


	/**
	 * Used for copying to the output XML file.
	 */
	void transcode(Node from, Node to, boolean expandKite9);

}