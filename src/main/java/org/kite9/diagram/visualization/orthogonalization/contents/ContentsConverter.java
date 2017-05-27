package org.kite9.diagram.visualization.orthogonalization.contents;

import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 * Converts the contents of a vertex or a label into inner/outer faces.
 * 
 * @author robmoffat
 *
 */
public interface ContentsConverter {

	public DartFace convertDiagramElementToInnerFace(DiagramElement original, Orthogonalization o);
	
	public CornerVertices getCornerVertices(DiagramElement original);
}
