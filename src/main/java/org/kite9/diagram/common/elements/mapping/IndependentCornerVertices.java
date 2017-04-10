package org.kite9.diagram.common.elements.mapping;

import org.kite9.diagram.model.DiagramElement;

public class IndependentCornerVertices extends AbstractBaseCornerVertices {
		
	public IndependentCornerVertices(DiagramElement c, int depth) {
		super(c, FULL_RANGE, FULL_RANGE, depth);
		createInitialVertices(c);
	}


}