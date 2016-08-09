package org.kite9.diagram.visualization.pipeline.full;

import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.xml.Diagram;

public interface ArrangementPipeline {

	/**
	 * Performs the process of arranging elements on a diagram, giving them 
	 * all {@link RenderingInformation} elements so that they can be rendered.
	 */
	public abstract Diagram arrange(Diagram d);
}
