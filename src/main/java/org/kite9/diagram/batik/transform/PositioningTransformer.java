package org.kite9.diagram.batik.transform;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.BasicDimension2D;
import org.kite9.diagram.model.position.Dimension2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This makes sure the content is positioned correctly inside it's container.
 * 
 * Content is expected to be defined from 0,0
 * 
 */
public class PositioningTransformer extends AbstractRectangularTransformer implements LeafTransformer {

	private DiagramElement owner;
	
	public PositioningTransformer(DiagramElement r) {
		this.owner = r;
	}
	
	DecimalFormat oneDForm = new DecimalFormat("#.0");
	
	@Override
	public Element postProcess(Painter p, Document d, XMLProcessor postProcessor) {	
		// work out translation
		Dimension2D position = getRenderedRelativePosition(owner);
		Element out = p.output(d, postProcessor);
		
		if (((position.x() != 0) || (position.y() != 0)) && (out != null)) {
			out.setAttribute("transform", "translate(" + oneDForm.format(position.x()) + "," + oneDForm.format(position.y()) + ")");
		}
		
		return out;
	}

	@Override
	public Dimension2D getBounds(LeafPainter p) {
		Rectangle2D r = p.bounds();
		return new BasicDimension2D(r.getMaxX(), r.getMaxY());
	}	
}
