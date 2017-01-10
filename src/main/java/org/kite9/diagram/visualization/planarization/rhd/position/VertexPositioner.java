package org.kite9.diagram.visualization.planarization.rhd.position;

import java.util.List;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.elements.mapping.CornerVertices;

/**
 * This places vertices on the diagram, based on the {@link PositionRoutableHandler2D} position of them.
 * 
 * @author robmoffat
 *
 */
public interface VertexPositioner {

	/**
	 * Keeps track of the sizes of elements in the grid, so we can ensure we don't put things on that are too small.
	 */
	void checkMinimumGridSizes(RoutingInfo ri);

	void setPerimeterVertexPositions(Connected before, DiagramElement c, Connected after, CornerVertices cvs, List<Vertex> out);

	void setCentralVertexPosition(DiagramElement c, List<Vertex> out);


}
