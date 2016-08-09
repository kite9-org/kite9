package org.kite9.diagram.visualization.pipeline.full;

import org.kite9.diagram.visualization.pipeline.rendering.RenderingPipeline;
import org.kite9.diagram.xml.Diagram;

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
