package org.kite9.diagram.dom.painter;

import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGRectPainter extends AbstractPainter {

	private String classes;
	
	public SVGRectPainter(String classes) {
		this.classes = classes;
	}

	@Override
	public Element output(Document d, XMLProcessor postProcessor) {
		RectangleRenderingInformation rri = (RectangleRenderingInformation) r.getRenderingInformation();
		Dimension2D size = rri.getSize();

		if ((size.getW() > 0) && (size.getH() > 0)) {
			Element out = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_RECT_TAG);
			out.setAttribute("width", ""+size.getW()+"px");
			out.setAttribute("height", ""+size.getH()+"px");
			out.setAttribute("class", classes);
			
			DiagramElement parent = r.getParent();
			
			Dimension2D position = rri.getPosition();
			Dimension2D parentPosition = ((RectangleRenderingInformation) parent.getRenderingInformation()).getPosition();
			Dimension2D offsetPosition = position.minus(parentPosition);
			
			out.setAttribute("x", ""+offsetPosition.getW()+"px");
			out.setAttribute("y", ""+offsetPosition.getH()+"px");
			
			addInfoAttributes(out);
			return out;
		} else {
			return null;
		}
	}

}
