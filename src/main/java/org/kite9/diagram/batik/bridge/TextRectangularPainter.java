package org.kite9.diagram.batik.bridge;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.anim.dom.SVGOMFlowDivElement;
import org.apache.batik.anim.dom.SVGOMFlowParaElement;
import org.apache.batik.anim.dom.SVGOMFlowRegionElement;
import org.apache.batik.anim.dom.SVGOMFlowRootElement;
import org.apache.batik.anim.dom.SVGOMRectElement;
import org.apache.batik.util.SVG12Constants;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles painting for {@link DiagramElementType.TEXT}
 * 
 * @author robmoffat
 *
 */
public class TextRectangularPainter extends AbstractRectangularGraphicsNodePainter<Leaf> {
	
	public TextRectangularPainter(Kite9BridgeContext ctx) {
		super(ctx);
	}
	
	float totalHeight = 0;
	String theText;

	/**
	 * Turn the text that's in the input element into a bunch of paragraphs in a SVG 1.2 flow.
	 * <flowRoot>
	 *   <flowRegion>
	 *     (whatever)
	 *   </flowRegion>
	 *   <flowDiv>
	 *     <flowPara>text</flowPara>
	 *   </flowDiv>
	 * </flowRoot>
	 */
	protected StyledKite9SVGElement initializeSourceContents(StyledKite9SVGElement theElement, Leaf r) {
		theElement = super.initializeSourceContents(theElement, r);
		Document d = theElement.getOwnerDocument();

		theText = theElement.getTextContent();
		String[] lines = theText.split("\n");

		// remove old content
		NodeList old = theElement.getChildNodes();
		while (old.getLength() > 0) {
			theElement.removeChild(old.item(0));
		}

		SVGOMFlowRootElement flowRoot = (SVGOMFlowRootElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_ROOT_TAG);
		SVGOMFlowDivElement flowDiv = (SVGOMFlowDivElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_DIV_TAG);
		SVGOMFlowRegionElement flowRegion = (SVGOMFlowRegionElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_REGION_TAG);
		SVGOMRectElement rect = (SVGOMRectElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_RECT_TAG);
		
		theElement.appendChild(flowRoot);
		flowRoot.appendChild(flowRegion);
		flowRoot.appendChild(flowDiv);
		flowRegion.appendChild(rect);
		rect.setAttribute("width", "100");
		rect.setAttribute("height", "100");
		
		for (String line : lines) {
			SVGOMFlowParaElement flowPara = (SVGOMFlowParaElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_PARA_TAG);
			flowDiv.appendChild(flowPara);
			flowPara.setTextContent(line);
		}

		return theElement;
	}

	@Override
	public Element output(Document d, StyledKite9SVGElement theElement, Leaf r) {
		return outputViaGraphicsNode(d, theElement, r);
	}
	
}
