package org.kite9.diagram.visualization.orthogonalization.vertices;

import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer;
import org.kite9.diagram.visualization.planarization.Planarization;

/**
 * Decorates a regular {@link Orthogonalizer} by converting all vertices to faces, which allows
 * their individual incoming edges to be displayed around their perimeter, using the
 * {@link VertexArranger} strategy.
 * 
 * @author robmoffat
 *
 */
public class VertexArrangementOrthogonalizationDecorator implements Orthogonalizer {

	private Orthogonalizer decorated;
	private VertexArranger cvtf;

	public Orthogonalization createOrthogonalization(Planarization pln) {
		Orthogonalization orth = decorated.createOrthogonalization(pln);
		cvtf.convertAllVerticesWithDimension(orth);
		return orth;
	}

	public VertexArrangementOrthogonalizationDecorator(Orthogonalizer decorated, VertexArranger cvtf) {
		super();
		this.decorated = decorated;
		this.cvtf = cvtf;
	}
	
}
