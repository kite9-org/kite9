package org.kite9.diagram.batik.painter;

import static org.apache.batik.util.SVGConstants.SVG_G_TAG;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

import java.awt.geom.Rectangle2D;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.anim.dom.SVGOMFlowDivElement;
import org.apache.batik.anim.dom.SVGOMFlowParaElement;
import org.apache.batik.anim.dom.SVGOMFlowRegionElement;
import org.apache.batik.anim.dom.SVGOMFlowRootElement;
import org.apache.batik.anim.dom.SVGOMRectElement;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVG12Constants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.text.ExtendedSVGGeneratorContext;
import org.kite9.diagram.batik.text.ExtendedSVGGraphics2D;
import org.kite9.diagram.batik.text.LocalRenderingFlowRootElementBridge;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.dom.elements.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles painting for {@link DiagramElementType.TEXT}
 * 
 * @author robmoffat
 *
 */
public class TextRectangularPainter extends AbstractGraphicsNodePainter implements LeafPainter {
	
	public TextRectangularPainter(StyledKite9SVGElement theElement, Kite9BridgeContext ctx) {
		super(theElement, ctx);
	}
	
	private Rectangle2D bounds;
	private String theText;
	private StyledKite9SVGElement textContents;
	
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
	protected StyledKite9SVGElement getContents() {
		if (textContents != null) {
			return textContents;
		}
		
		Document d = theElement.getOwnerDocument();

		theText = theElement.getTextContent();
		String[] lines = theText.split("\n");

		// remove old content
		NodeList old = theElement.getChildNodes();
		while (old.getLength() > 0) {
			theElement.removeChild(old.item(0));
		}

		// convert the flow element into regular svg:text
		SVGOMFlowRootElement flowRoot = createFlowRootElement(d, lines, theElement);
		addFontSizeAndFamily(theElement, (Leaf) r, flowRoot);
		GraphicsNode gn = LocalRenderingFlowRootElementBridge.getFlowNode(initGraphicsNode(flowRoot, ctx));
		Element group = graphicsNodeToXML(d, gn);
		
		if (group != null ) {
			removeFontSizeAndFamily(group);
			theElement.appendChild(group);
		}
		
		textContents = theElement;

		return textContents;
	}
	
	
	/**
	 * Remove the temporary attributes
	 */
	private void removeFontSizeAndFamily(Element group) {
		group.removeAttribute("font-family");
		group.removeAttribute("font-size");
		
	}


	/**
	 * Font size and family really affect the positioning and size of the text, so these are 2 attributes
	 * we need to set correctly, temporarily, for the layout
	 */
	private void addFontSizeAndFamily(StyledKite9SVGElement theElement, Leaf r, SVGOMFlowRootElement flowRoot) {
		Value size = theElement.getCSSStyleProperty(CSSConstants.CSS_FONT_SIZE_PROPERTY);
		Value family = theElement.getCSSStyleProperty(CSSConstants.CSS_FONT_FAMILY_PROPERTY);
		flowRoot.setAttribute("font-size", ""+size.getFloatValue());
		flowRoot.setAttribute("font-family", ""+family.getCssText());
	}


	private Element graphicsNodeToXML(Document d, GraphicsNode node) {
		Element groupElem = d.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		ExtendedSVGGeneratorContext genCtx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(d);
		ExtendedSVGGraphics2D g2d = new ExtendedSVGGraphics2D(genCtx, groupElem);
		node.paint(g2d);
		groupElem = g2d.getTopLevelGroup(true);
		bounds = g2d.getTextBounds();
		return (Element) groupElem.getFirstChild();
	}


	private SVGOMFlowRootElement createFlowRootElement(Document d, String[] lines, StyledKite9SVGElement theElement) {
		SVGOMFlowRootElement flowRoot = (SVGOMFlowRootElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_ROOT_TAG);
		SVGOMFlowDivElement flowDiv = (SVGOMFlowDivElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_DIV_TAG);
		SVGOMFlowRegionElement flowRegion = (SVGOMFlowRegionElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_REGION_TAG);
		flowRoot.appendChild(flowRegion);
		flowRoot.appendChild(flowDiv);

		setupFlowRegion(d, flowRegion, theElement);
		
		for (String line : lines) {
			SVGOMFlowParaElement flowPara = (SVGOMFlowParaElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_PARA_TAG);
			flowDiv.appendChild(flowPara);
			flowPara.setTextContent(line);
		}
		return flowRoot;
	}


	/**
	 * By default, this uses a bounding box, but that can be overridden.
	 * Later, we can do some cool stuff in here with flow shapes, paths, width/height etc.
	 */
	protected void setupFlowRegion(Document d, SVGOMFlowRegionElement flowRegion, StyledKite9SVGElement theElement) {
		SVGOMRectElement rect = (SVGOMRectElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_RECT_TAG);
		//SVGOMPathElement path = (SVGOMPathElement) d.createElementNS(SVG_NAMESPACE_URI, SVG12Constants.SVG_PATH_TAG);
		
//		flowRegion.appendChild(path);
		flowRegion.appendChild(rect);
		rect.setAttribute("width", "10000");
		rect.setAttribute("height", "10000");
//		rect.setAttribute("x", "30");
//		rect.setAttribute("y", "30");
//		path.setAttribute("d", "M100,50L50,300L250,300L200,50z");
	}
	
	@Override
	public Rectangle2D bounds() {
		GraphicsNode gn = getGraphicsNode();
		return bounds;
	}

}
