package org.kite9.diagram.batik.transform;

import org.kite9.diagram.batik.model.AbstractBatikDiagramElement;
import org.kite9.diagram.dom.transform.*;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.ContentTransform;

public class TransformFactory {

	public static SVGTransformer initializeTransformer(AbstractBatikDiagramElement diagramElement, ContentTransform t, ContentTransform defaultTransform) {
		
		if (t == ContentTransform.DEFAULT) {
			t = defaultTransform;
		}
		
		// note we are using the fall-through approach here.
		switch (t) {
		case RESCALE:
		case CROP:
			if (diagramElement instanceof Leaf) {
				if (t==ContentTransform.RESCALE) {
					return new RescalingTransformer((Leaf) diagramElement);
				} else if (t==ContentTransform.CROP) {
					return new CroppingTransformer((Leaf) diagramElement);
				}
			}
			//$FALL-THROUGH$
		case POSITION:
			return new PositioningTransformer(diagramElement);
		case NONE:
			return new NoopTransformer();
		case DEFAULT:
		default:
			throw new Kite9ProcessingException("No transform defined for "+diagramElement);
		}
	}
}
