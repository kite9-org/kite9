package org.kite9.diagram.common.elements.grid;

import java.util.List;

import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Layout;

/**
 * Handles positioning of elements for {@link Layout}.GRID.  
 * 
 * @author robmoffat
 *
 */
public interface GridPositioner {

	/**
	 * Works out the position of the elements within the grid for a given container.
	 * 
	 * Should cache too.
	 * 
	 * @param allowSpanning Set to true if we should consider the full span of the "occupies-x" and "occupies-y" directive, or just the lower bound.
	 */
	public DiagramElement[][] placeOnGrid(Container gridContainer, boolean allowSpanning);
	
	/**
	 * Used for creating the perimeter of a grid.  Returns the perimeter vertices in clockwise
	 * order.
	 */
	public List<MultiCornerVertex> getClockwiseOrderedContainerVertices(CornerVertices cv);

}