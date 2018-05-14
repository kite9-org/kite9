package org.kite9.diagram.batik.transform;

import org.kite9.diagram.batik.model.AbstractBatikDiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.framework.common.Kite9ProcessingException;

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
			if (diagramElement instanceof Rectangular) {
				return new PositioningTransformer((Rectangular) diagramElement);
			}
			//$FALL-THROUGH$
		case NONE:
			return new NoopTransformer();
		case DEFAULT:
		default:
			throw new Kite9ProcessingException("No transform defined");
		}
	}
}
