package org.kite9.diagram.adl;

import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;

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
	
	@Deprecated
	public void setRenderingInformation(RenderingInformation ri);
	
	public HintMap getPositioningHints();
		
	@Deprecated
	public String getShapeName();
	
	/**
	 * This method returns the container.  Since in the future, not all `Connected` objects will be immediately 
	 * in containers, we should have this deprecated.
	 * @return
	 */
	@Deprecated
	Container getContainer();
	
	public double getMargin(Direction d);
		
}