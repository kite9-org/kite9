package org.kite9.diagram.common.elements;

import org.kite9.diagram.style.DiagramElement;
import org.kite9.diagram.visualization.orthogonalization.Dart;

/**
 * A vertex modelling the join between a {@link Dart} and a Glyph or Arrow, created in the 
 * process of giving a vertex a dimensioned shape in orthogonalization.
 * 
 * @author robmoffat
 *
 */
public class SideVertex extends AbstractVertex {
	
	public SideVertex(String name, DiagramElement underlying) {
		super(name);
		this.originalUnderlying = underlying;
	}

	DiagramElement originalUnderlying;

	public DiagramElement getOriginalUnderlying() {
		return originalUnderlying;
	}
}
