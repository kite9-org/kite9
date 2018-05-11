package org.kite9.diagram.batik.transform;

import org.kite9.diagram.dom.model.HasSVGGraphics;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.framework.common.Kite9ProcessingException;

public class TransformFactory {

	public static SVGTransformer initializeTransformer(HasSVGGraphics diagramElement) {
		ContentTransform t = diagramElement.getTransform();
		
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
		default:
			throw new Kite9ProcessingException("Not dealt with enum value: "+t);
		}
	}
}
