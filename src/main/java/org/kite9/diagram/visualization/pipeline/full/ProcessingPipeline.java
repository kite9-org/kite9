package org.kite9.diagram.visualization.pipeline.full;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.visualization.pipeline.rendering.RenderingPipeline;

/**
 * Performs the complete process of arrangement then rendering.
 * 
 * @author robmoffat
 *
 * @param <X> the output format
 */
public interface ProcessingPipeline<X> extends ArrangementPipeline, RenderingPipeline<X> {

	public X process(Diagram d);
}
