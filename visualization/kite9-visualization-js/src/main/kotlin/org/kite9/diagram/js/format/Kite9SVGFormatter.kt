import org.kite9.diagram.dom.processors.DiagramPositionProcessor
import org.kite9.diagram.dom.processors.DiagramStructureProcessor
import org.kite9.diagram.dom.processors.TextWrapProcessor
import org.kite9.diagram.dom.processors.xpath.XPathValueReplacer
import org.kite9.diagram.js.bridge.JSElementContext
import org.kite9.diagram.js.logging.JSKite9Log
import org.kite9.diagram.js.model.JSDiagramElementFactory
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.visualization.display.BasicCompleteDisplayer
import org.kite9.diagram.visualization.pipeline.BasicArrangementPipeline
import org.w3c.dom.Element

/**
 * Top-level function that can be called from javascript to render
 * Kite9 SVG properly in-browser
 */

@JsName("formatSVG")
fun formatSVG(e: Element) {
    Kite9Log.Companion.factory = { l -> JSKite9Log(l) }
    val context = JSElementContext()
    val ef = JSDiagramElementFactory(context)

    // handle text-wrapping
    val wrapProcessor = TextWrapProcessor(context)
    wrapProcessor.processContents(e)

    // process structure into diagram elements
    var p = DiagramStructureProcessor(ef, context)
    p.processContents(e)

    // process diagrams
    for (d in p.diagrams) {
        val pipeline = BasicArrangementPipeline(ef, BasicCompleteDisplayer(false))
        pipeline.arrange(d)
        val p2 = DiagramPositionProcessor(context, XPathValueReplacer(context))
        p2.processContents(e)
    }
}


