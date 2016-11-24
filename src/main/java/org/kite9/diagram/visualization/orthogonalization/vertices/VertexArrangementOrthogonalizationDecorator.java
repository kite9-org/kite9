package org.kite9.diagram.visualization.orthogonalization.vertices;

import java.util.Collection;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.Displayer;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;

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
	private CompleteDisplayer sizer;
	private VertexArranger cvtf;

	
	public static void setInitialSizes(Collection<Vertex> vertices, Displayer ded) {
		for (Vertex vertex : vertices) {
			setInitialSize(ded, vertex);
		}
	}

	public static void setInitialSize(Displayer ded, Vertex tl) {
		if (tl instanceof ContainerVertex) {
			return;
		}
		
		DiagramElement originalUnderlying = tl.getOriginalUnderlying();
		
		if (originalUnderlying instanceof Leaf) {
			Dimension2D size = ded.size(originalUnderlying, CostedDimension.UNBOUNDED);
			RectangleRenderingInformation ri = (RectangleRenderingInformation) ((Leaf)originalUnderlying).getRenderingInformation();
			ri.setSize(size);
		}
	}

	public Orthogonalization createOrthogonalization(Planarization pln) {
		Orthogonalization orth = decorated.createOrthogonalization(pln);
		setInitialSizes(orth.getAllVertices(), sizer);
		cvtf.convertAllVerticesWithDimension(orth);
		return orth;
	}

	public VertexArrangementOrthogonalizationDecorator(Orthogonalizer decorated,
			CompleteDisplayer sizer, VertexArranger cvtf) {
		super();
		this.decorated = decorated;
		this.sizer = sizer;
		this.cvtf = cvtf;
	}
	
}
