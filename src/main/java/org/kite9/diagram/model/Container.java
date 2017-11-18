package org.kite9.diagram.model;

import java.util.List;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.DiagramElementSizing;


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
public interface Container extends Rectangular {

	public List<DiagramElement> getContents();
	
	public Layout getLayout();
	
	public BorderTraversal getTraversalRule(Direction d);
	
	public int getGridColumns();
	
	public int getGridRows();
	
	public DiagramElementSizing getSizing();
	
}
