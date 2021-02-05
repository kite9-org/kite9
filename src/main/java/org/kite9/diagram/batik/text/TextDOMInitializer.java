package org.kite9.diagram.batik.text;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.anim.dom.SVGOMFlowDivElement;
import org.apache.batik.anim.dom.SVGOMFlowParaElement;
import org.apache.batik.anim.dom.SVGOMFlowRegionElement;
import org.apache.batik.anim.dom.SVGOMFlowRootElement;
import org.apache.batik.anim.dom.SVGOMRectElement;
import org.apache.batik.util.SVG12Constants;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

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
public class TextDOMInitializer {


	public static void setupElementXML(StyledKite9XMLElement e) {
		// convert the flow element into regular svg:text
		Document d = e.getOwnerDocument();
		SVGOMFlowRootElement flowRoot = createFlowRootElement(d, e);

		// remove old content
		NodeList old = e.getChildNodes();
		while (old.getLength() > 0) {
			e.removeChild(old.item(0));
		}
		
		// replace with new flowRoot
		e.appendChild(flowRoot);
		
	}	
	

	protected static SVGOMFlowRootElement createFlowRootElement(Document d, StyledKite9XMLElement theElement) {
		SVGOMFlowRootElement flowRoot = (SVGOMFlowRootElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_ROOT_TAG);
		setupFlowRegions(d, theElement, flowRoot);
		setupFlowDiv(d, theElement, flowRoot);
		return flowRoot;
	}
	


	protected static void setupFlowDiv(Document d, StyledKite9XMLElement theElement, SVGOMFlowRootElement in) {
		NodeList toAdd = theElement.getElementsByTagNameNS(SVG12OMDocument.SVG_NAMESPACE_URI,SVG12Constants.SVG_FLOW_DIV_TAG);
		
		if (toAdd.getLength() > 0) {
			for (int i = 0; i < toAdd.getLength(); i++) {
				in.appendChild(toAdd.item(i));
			}
		} else {
			String theText = theElement.getTextContent().trim();
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
	protected static void setupFlowRegions(Document d, StyledKite9XMLElement theElement, SVGOMFlowRootElement in) {
		NodeList toAdd = theElement.getElementsByTagNameNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_REGION_TAG);
		boolean alreadyExist = false;
		for (int i = 0; i < toAdd.getLength(); i++) {
			in.appendChild(toAdd.item(i));
			alreadyExist = true;
		}
		
		if (!alreadyExist) {
			String width = theElement.getCSSStyleProperty(CSSConstants.TEXT_BOUNDS_WIDTH).getCssText();
			String height = theElement.getCSSStyleProperty(CSSConstants.TEXT_BOUNDS_HEIGHT).getCssText();
			
			SVGOMFlowRegionElement flowRegion = (SVGOMFlowRegionElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_REGION_TAG);
			SVGOMRectElement rect = (SVGOMRectElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_RECT_TAG);
			flowRegion.appendChild(rect);
			rect.setAttribute("width", width);
			rect.setAttribute("height", height);
			in.appendChild(flowRegion);
		}
	}
}
