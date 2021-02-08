package org.kite9.diagram.dom.transform;

import org.kite9.diagram.dom.painter.LeafPainter;
import org.kite9.diagram.model.position.Dimension2D;

public interface LeafTransformer extends SVGTransformer {

	public Dimension2D getBounds(LeafPainter p);
}
