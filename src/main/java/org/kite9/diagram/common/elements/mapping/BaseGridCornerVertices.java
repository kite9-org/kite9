package org.kite9.diagram.common.elements.mapping;

import org.kite9.diagram.model.DiagramElement;

public class BaseGridCornerVertices extends IndependentCornerVertices {

	public BaseGridCornerVertices(DiagramElement container, int depth) {
		super(container, depth);
	}

	@Override
	protected String getVertexIDStem() {
		return super.getVertexIDStem()+"-g";
	}

	public DiagramElement getGridContainer() {
		return rootContainer;
	}

	
}
