package org.kite9.diagram.dom.transform

import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Leaf
import org.kite9.diagram.model.style.ContentTransform

object TransformFactory {

	fun initializeTransformer(
        diagramElement: DiagramElement,
        t: ContentTransform,
        defaultTransform: ContentTransform
    ): SVGTransformer {
        var t = t
        if (t === ContentTransform.DEFAULT) {
            t = defaultTransform
        }
        return when (t) {
            ContentTransform.RESCALE, ContentTransform.CROP -> {
                if (diagramElement is Leaf) {
                    if (t === ContentTransform.RESCALE) {
                        return RescalingTransformer(diagramElement)
                    } else if (t === ContentTransform.CROP) {
                        return CroppingTransformer(diagramElement)
                    }
                }
                PositioningTransformer(diagramElement)
            }
            ContentTransform.POSITION -> PositioningTransformer(diagramElement)
            ContentTransform.NONE -> NoopTransformer()
            ContentTransform.DEFAULT -> throw Kite9ProcessingException("No transform defined for $diagramElement")
            else -> throw Kite9ProcessingException("No transform defined for $diagramElement")
        }
    }
}