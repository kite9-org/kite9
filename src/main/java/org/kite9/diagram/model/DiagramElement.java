package org.kite9.diagram.model;

import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.BoxShadow;

/**
 * Parent class for all elements of the diagram.
 */
public interface DiagramElement extends Comparable<DiagramElement> {

	public DiagramElement getParent();
	
	/**
	 * ID should be a project-unique ID to describe this element.  It is also used within the 
	 * XML to allow references between the elements of the XML file. 
	 * 
	 * ID is also used for hashcode and equals.  Set an ID to ensure sorting, maps
	 * and therefore diagram layouts, are deterministic.
	 * 
	 * IDs are expected for most elements, but are optional.
	 * 
	 */
	public String getID();

	public RenderingInformation getRenderingInformation();
	
	public HintMap getPositioningHints();
		
	/**
	 * Returns the number of levels deep which this element is embedded in the diagram hierarchy, with zero the top level.
	 */
	public int getDepth();
	
	public BoxShadow getShadow();
}