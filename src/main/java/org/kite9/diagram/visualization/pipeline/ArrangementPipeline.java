package org.kite9.diagram.visualization.pipeline;

import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.framework.xml.DiagramKite9XMLElement;

public interface ArrangementPipeline {

	/**
	 * Performs the process of arranging elements on a diagram, giving them 
	 * all {@link RenderingInformation} elements so that they can be rendered.
	 */
	public abstract DiagramKite9XMLElement arrange(DiagramKite9XMLElement d);
}
