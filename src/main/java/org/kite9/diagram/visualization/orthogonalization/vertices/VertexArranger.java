package org.kite9.diagram.visualization.orthogonalization.vertices;

import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 * The vertex arranger is part of the Orthogonalization process
 * where a vertex with dimensionality is converted into a number of darts
 * 
 * @author robmoffat
 *
 */
public interface VertexArranger {

	/**
	 * Performs the dart transformation on the orthogonalization
	 * @param o
	 * @param sizer
	 */
	public void convertAllVerticesWithDimension(Orthogonalization o);
		
}
