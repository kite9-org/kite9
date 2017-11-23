package org.kite9.diagram.batik.bridge;

import static org.apache.batik.util.SVGConstants.SVG_G_TAG;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

import java.awt.geom.Rectangle2D;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.anim.dom.SVGOMFlowDivElement;
import org.apache.batik.anim.dom.SVGOMFlowParaElement;
import org.apache.batik.anim.dom.SVGOMFlowRegionElement;
import org.apache.batik.anim.dom.SVGOMFlowRootElement;
import org.apache.batik.anim.dom.SVGOMRectElement;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.SVG12Constants;
import org.kite9.diagram.batik.format.ExtendedSVGGeneratorContext;
import org.kite9.diagram.batik.format.ExtendedSVGGraphics2D;
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
public class TextRectangularPainter extends AbstractGraphicsNodePainter<Leaf> implements RectangularPainter<Leaf> {
	
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

		// convert the flow element into regular svg:text
		SVGOMFlowRootElement flowRoot = createFlowRootElement(d, lines);
		GraphicsNode gn = getGraphicsNode(flowRoot);
		Element group = graphicsNodeToXML(d, gn);
		theElement.appendChild(group);

		return theElement;
	}
	
	
	private Element graphicsNodeToXML(Document d, GraphicsNode node) {
		Element groupElem = d.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		ExtendedSVGGeneratorContext genCtx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(d, null, null);
		ExtendedSVGGraphics2D g2d = new ExtendedSVGGraphics2D(genCtx, groupElem);
		//g2d.transform(node.getInverseTransform());
		node.paint(g2d);
		groupElem = g2d.getTopLevelGroup(true);
		
		return groupElem;
	}


	private SVGOMFlowRootElement createFlowRootElement(Document d, String[] lines) {
		SVGOMFlowRootElement flowRoot = (SVGOMFlowRootElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_ROOT_TAG);
		SVGOMFlowDivElement flowDiv = (SVGOMFlowDivElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_DIV_TAG);
		SVGOMFlowRegionElement flowRegion = (SVGOMFlowRegionElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_REGION_TAG);
		SVGOMRectElement rect = (SVGOMRectElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_RECT_TAG);
		
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
		return flowRoot;
	}
	
	@Override
	public Rectangle2D bounds(StyledKite9SVGElement in, Leaf l) {
		GraphicsNode gn = getGraphicsNode(getContents(in, l));
		return gn.getBounds();
	}

	/**
	 * Rather than returning the top-level graphics node, we are going
	 * to return the Flow node within it, as otherwise we'll get the text container too.
	 */
	@Override
	protected GraphicsNode initGraphicsNode(Element theElement) {
		return super.initGraphicsNode(theElement);
	}
	
	
	
}
