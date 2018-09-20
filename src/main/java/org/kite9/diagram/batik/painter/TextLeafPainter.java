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
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.SVG12Constants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.text.ExtendedSVGGeneratorContext;
import org.kite9.diagram.batik.text.ExtendedSVGGraphics2D;
import org.kite9.diagram.batik.text.LocalRenderingFlowRootElementBridge;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.model.style.DiagramElementType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handles painting for {@link DiagramElementType.TEXT} using SVG1.2's flowRoot
 * (supported by Batik, but not implemented in most browsers). 
 * 
 * With the help of {@link LocalRenderingFlowRootElementBridge} we convert this into 
 * regular svg text elements.
 * 
 * @author robmoffat
 *
 */
public class TextLeafPainter extends AbstractGraphicsNodePainter implements LeafPainter {
	
	public TextLeafPainter(StyledKite9SVGElement theElement, Kite9BridgeContext ctx) {
		super(theElement, ctx);
	}
	
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
	public StyledKite9SVGElement getContents() {
		if (textContents != null) {
			return textContents;
		}
		
		StyledKite9SVGElement theElement = super.getContents();
		
		Document d = theElement.getOwnerDocument();

		// convert the flow element into regular svg:text
		SVGOMFlowRootElement flowRoot = createFlowRootElement(d, theElement);

		// remove old content
		NodeList old = theElement.getChildNodes();
		while (old.getLength() > 0) {
			theElement.removeChild(old.item(0));
		}
		
		theElement.appendChild(flowRoot);
		GraphicsNode gn = LocalRenderingFlowRootElementBridge.getFlowNode(initGraphicsNode(flowRoot, ctx));
		Element group = graphicsNodeToXML(d, gn);
		
		theElement.removeChild(flowRoot);
		
		if (group != null) {
			theElement.appendChild(group);
		}
		
		textContents = theElement;

		return textContents;
	}

	private Element graphicsNodeToXML(Document d, GraphicsNode node) {
		Element groupElem = d.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		ExtendedSVGGeneratorContext genCtx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(d);
		ExtendedSVGGraphics2D g2d = new ExtendedSVGGraphics2D(genCtx, groupElem);
		node.paint(g2d);
		groupElem = g2d.getTopLevelGroup(true);
		return (Element) groupElem.getFirstChild();
	}


	private SVGOMFlowRootElement createFlowRootElement(Document d, StyledKite9SVGElement theElement) {
		SVGOMFlowRootElement flowRoot = (SVGOMFlowRootElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_ROOT_TAG);
		setupFlowRegions(d, theElement, flowRoot);
		setupFlowDiv(d, theElement, flowRoot);
		return flowRoot;
	}

	public void setupFlowDiv(Document d, StyledKite9SVGElement theElement, SVGOMFlowRootElement in) {
		NodeList toAdd = theElement.getElementsByTagNameNS(SVG12OMDocument.SVG_NAMESPACE_URI,SVG12Constants.SVG_FLOW_DIV_TAG);
		
		if (toAdd.getLength() > 0) {
			for (int i = 0; i < toAdd.getLength(); i++) {
				in.appendChild(toAdd.item(i));
			}
		} else {
			String theText = theElement.getTextContent();
			String[] lines = theText.split("\n");

			// create one from text
			SVGOMFlowDivElement flowDiv = (SVGOMFlowDivElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_DIV_TAG);
			
			for (String line : lines) {
				SVGOMFlowParaElement flowPara = (SVGOMFlowParaElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_PARA_TAG);
				flowDiv.appendChild(flowPara);
				flowPara.setTextContent(line);
			}
			
			in.appendChild(flowDiv);
		}
	}


	/**
	 * If a flowRegion exists within the input node, use that. Otherwise create
	 * one with a really large bounding box.
	 */
	protected void setupFlowRegions(Document d, StyledKite9SVGElement theElement, SVGOMFlowRootElement in) {
		NodeList toAdd = theElement.getElementsByTagNameNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_REGION_TAG);
		boolean alreadyExist = false;
		for (int i = 0; i < toAdd.getLength(); i++) {
			in.appendChild(toAdd.item(i));
			alreadyExist = true;
		}
		
		if (!alreadyExist) {
			SVGOMFlowRegionElement flowRegion = (SVGOMFlowRegionElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_REGION_TAG);
			SVGOMRectElement rect = (SVGOMRectElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_RECT_TAG);
			flowRegion.appendChild(rect);
			rect.setAttribute("width", "10000");
			rect.setAttribute("height", "10000");
			in.appendChild(flowRegion);
		}
	}
	
	@Override
	public Rectangle2D bounds() {
		GraphicsNode gn = getGraphicsNode();
		return gn.getBounds();
	}
}
