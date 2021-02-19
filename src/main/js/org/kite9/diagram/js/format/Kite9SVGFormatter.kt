package org.kite9.diagram.js.format

import org.kite9.diagram.js.bridge.JSElementContext
import org.kite9.diagram.js.model.JSDiagramElementFactory
import org.kite9.diagram.js.processors.DiagramStructureProcessor
import org.kite9.diagram.visualization.display.BasicCompleteDisplayer
import org.kite9.diagram.visualization.pipeline.BasicArrangementPipeline
import org.w3c.dom.Element

/**
 * Top-level function that can be called from javascript to render
 * Kite9 SVG properly in-browser
 */

fun formatSVG(e: Element) {
    val context = JSElementContext()
    val ef = JSDiagramElementFactory(context)
    var p = DiagramStructureProcessor(ef)
    p.processContents(e)
    val d = p.first!!;
    val pipeline = BasicArrangementPipeline(ef, BasicCompleteDisplayer(false))
    pipeline.arrange(d)
}


