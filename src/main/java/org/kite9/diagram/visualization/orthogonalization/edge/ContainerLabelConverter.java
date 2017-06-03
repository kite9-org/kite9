package org.kite9.diagram.visualization.orthogonalization.edge;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

public interface ContainerLabelConverter {

	public void handleContainerLabels(DartFace innerFace, DiagramElement partOf, Orthogonalization o);
	
}
