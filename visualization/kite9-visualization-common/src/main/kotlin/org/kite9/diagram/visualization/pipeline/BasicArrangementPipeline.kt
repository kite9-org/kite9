package org.kite9.diagram.visualization.pipeline

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.visualization.display.CompleteDisplayer

class BasicArrangementPipeline(
    override val diagramElementFactory: DiagramElementFactory<*>,
    override val displayer: CompleteDisplayer
) : AbstractArrangementPipeline()