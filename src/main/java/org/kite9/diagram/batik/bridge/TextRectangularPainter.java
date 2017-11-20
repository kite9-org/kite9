package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.CSSConstants;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.TEXT}
 * 
 * @author robmoffat
 *
 */
public class TextRectangularPainter extends SVGLeafRectangularPainter {

	
	
	public TextRectangularPainter(Kite9BridgeContext ctx) {
		super(ctx);
	}

	protected void processContents(StyledKite9SVGElement theElement, Element out, Document d, Leaf l) {
		float lineHeight = getLineHeight(theElement);
		float lineDrop = getLineDrop(theElement);
		String theText = theElement.getTextContent();
		String[] lines = theText.split("\n");
		
		float currentLineYPosition = lineDrop;
		
		for (String line : lines) {
			Element text = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_TEXT_TAG);
			text.setTextContent(line.trim());
			out.appendChild(text);
			currentLineYPosition += lineHeight;
			text.setAttribute("y", ""+currentLineYPosition);
		}
		
		// does it respect the viewbox?
		out.setAttribute("viewBox", "0 0 300 200");
	}
	
	private float getLineDrop(StyledKite9SVGElement theElement) {
		Value v = theElement.getCSSStyleProperty(CSSConstants.CSS_FONT_SIZE_PROPERTY);
		return v.getFloatValue();
	}

	private float getLineHeight(StyledKite9SVGElement theElement) {
		return getLineDrop(theElement);
	}

	@Override
	public Rectangle2D bounds(Element theElement) {
		return new Rectangle2D.Double(0, 0, 100, 100);
	}

	
}
