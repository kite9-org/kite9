package org.kite9.diagram.visualization.pipeline

import org.kite9.diagram.model.Diagram

interface ArrangementPipeline {
    /**
     * Performs the process of arranging elements on a diagram, giving them
     * all [RenderingInformation] elements so that they can be rendered.
     */
    fun arrange(d: Diagram): Diagram

}