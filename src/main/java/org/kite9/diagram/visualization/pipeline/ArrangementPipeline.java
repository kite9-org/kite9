package org.kite9.diagram.visualization.pipeline;

import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.position.RenderingInformation;

public interface ArrangementPipeline {

	/**
	 * Performs the process of arranging elements on a diagram, giving them 
	 * all {@link RenderingInformation} elements so that they can be rendered.
	 */
	public abstract Diagram arrange(Diagram d);
}
