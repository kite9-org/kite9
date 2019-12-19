package org.kite9.diagram.dom.painter;

import org.apache.batik.util.SVGConstants;
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
	public Element output(Document d) {
		RectangleRenderingInformation rri = (RectangleRenderingInformation) r.getRenderingInformation();
		Dimension2D size = rri.getSize();

		if ((size.getWidth() > 0) && (size.getHeight() > 0)) {
			Element out = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_RECT_TAG);
			out.setAttribute("width", ""+size.getWidth()+"px");
			out.setAttribute("height", ""+size.getHeight()+"px");
			out.setAttribute("class", classes);
			
			DiagramElement parent = r.getParent();
			
			Dimension2D position = rri.getPosition();
			Dimension2D parentPosition = ((RectangleRenderingInformation) parent.getRenderingInformation()).getPosition();
			Dimension2D offsetPosition = position.minus(parentPosition);
			
			out.setAttribute("x", ""+offsetPosition.getWidth()+"px");
			out.setAttribute("y", ""+offsetPosition.getHeight()+"px");
			
			addInfoAttributes(out);
			return out;
		} else {
			return null;
		}
	}

}
