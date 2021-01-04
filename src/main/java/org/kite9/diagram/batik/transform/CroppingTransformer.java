package org.kite9.diagram.batik.transform;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This makes sure the content is positioned correctly inside it's container.
 * Content is all cropped between top-left and bottom right positions.
 */
public class CroppingTransformer extends AbstractRectangularTransformer implements LeafTransformer {

	private Leaf owner;
	
	public CroppingTransformer(Leaf l) {
		this.owner = l;
	}
	
	DecimalFormat oneDForm = new DecimalFormat("#.0");


	@Override
	public Element postProcess(Painter p, Document d, XMLProcessor postProcessor) {
		// work out translation
		Dimension2D position = getRenderedRelativePosition(owner);

		Element out = p.output(d, postProcessor);

		if (position == null) {
			return out;
		}

		if ((p instanceof LeafPainter) && (out != null)) {
			Rectangle2D content = ((LeafPainter) p).bounds();
			if (content != null) {
				position = position.minus(new Dimension2D(content.getX(), content.getY()));

				if (owner instanceof SizedRectangular) {
					double left = ((SizedRectangular) owner).getPadding(Direction.LEFT);
					double top = ((SizedRectangular) owner).getPadding(Direction.UP);
					position = position.add(new Dimension2D(left, top));
				}
				
				
				if ((position.x() != 0) || (position.y() != 0)) {
					out.setAttribute("transform", "translate(" + oneDForm.format(position.x()) + "," + oneDForm.format(position.y()) + ")");
				}
			}
		}

		return out;
	}

	@Override
	public Dimension2D getBounds(LeafPainter p) {
		Rectangle2D r = p.bounds();
		if (r == null) {
			return CostedDimension.Companion.getZERO();
		}
		return new Dimension2D(r.getWidth(), r.getHeight());
	}	
}
