package org.kite9.diagram.adl;

import java.util.List;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.style.BorderTraversal;


/**
 * Interface to say that this diagram element contains a 
 * variable number of others rendered within it.  The size of the element is in large part dependent
 * therefore on the elements within it.
 * 
 * Opposite of {@link Leaf}
 * 
 * @author robmoffat
 *
 */
public interface Container extends DiagramElement, Rectangular {

	public List<DiagramElement> getContents();
	
	public Layout getLayout();
	
	/**
	 * Not sure if we should have this method or not.  - Deprecated for now, needs further thought.
	 * There are plenty of containers now that don't have labels.
	 */
	@Deprecated
	public Label getLabel();
	
	public BorderTraversal getTraversalRule(Direction d);
	
	public int getGridColumns();
	
	public int getGridRows();
}
