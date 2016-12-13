package org.kite9.diagram.visualization.orthogonalization.vertices;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.Displayer;
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
	private CompleteDisplayer sizer;
	private VertexArranger cvtf;

	
	public static void setInitialSizes(DiagramElement d, Displayer ded) {
		if (d instanceof Container) {
			for (DiagramElement child : ((Container)d).getContents()) {
				setInitialSizes(child, ded);
			}
			
			Label l = ((Container)d).getLabel();
			if (l != null) {
				setInitialSizes(l, ded);
			}
		}
		
		if (d instanceof Leaf) {
			Dimension2D size = ded.size((Leaf) d, CostedDimension.UNBOUNDED);
			RectangleRenderingInformation ri = (RectangleRenderingInformation) ((Leaf)d).getRenderingInformation();
			ri.setSize(size);
		}
	}

	public Orthogonalization createOrthogonalization(Planarization pln) {
		Orthogonalization orth = decorated.createOrthogonalization(pln);
		setInitialSizes(orth.getPlanarization().getDiagram(), sizer);
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
