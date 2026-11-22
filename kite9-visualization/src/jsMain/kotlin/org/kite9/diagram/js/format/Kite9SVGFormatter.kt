import org.kite9.diagram.dom.processors.DiagramPositionProcessor
import org.kite9.diagram.dom.processors.DiagramStructureProcessor
import org.kite9.diagram.dom.processors.TextWrapProcessor
import org.kite9.diagram.dom.processors.xpath.XPathValueReplacer
import org.kite9.diagram.js.bridge.JSElementContext
import org.kite9.diagram.js.logging.JSKite9Log
import org.kite9.diagram.js.model.JSDiagramElementFactory
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.visualization.display.BasicCompleteDisplayer
import org.kite9.diagram.visualization.pipeline.NGArrangementPipeline
import org.w3c.dom.Element

/**
 * Top-level function that can be called from javascript to render
 * Kite9 SVG properly in-browser
 */

@JsExport
@JsName(name = "kite9Context")
val context = JSElementContext()

@JsExport
@JsName(name = "getCssStyleDoubleProperty")
fun getCssStyleDoubleProperty(p: String, e: Element) = context.getCssStyleDoubleProperty(p, e)

@JsExport
@JsName("formatSVG")
fun formatSVG(e: Element) {
    Kite9Log.Companion.factory = { l -> JSKite9Log(l) }
    val ef = JSDiagramElementFactory(context)

    // handle text-wrapping
    val wrapProcessor = TextWrapProcessor(context)
    wrapProcessor.processContents(e)

    // process structure into diagram elements
    var p = DiagramStructureProcessor(ef, context)
    p.processContents(e)

    // process diagrams
    for (d in p.diagrams) {
        val pipeline = NGArrangementPipeline(ef, BasicCompleteDisplayer(false))
        pipeline.arrange(d)
        val p2 = DiagramPositionProcessor(context, XPathValueReplacer(context))
        p2.processContents(e)
    }
}


