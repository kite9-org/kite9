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
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.SVG12Constants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.text.ExtendedSVGGeneratorContext;
import org.kite9.diagram.batik.text.ExtendedSVGGraphics2D;
import org.kite9.diagram.batik.text.LocalRenderingFlowRootElementBridge;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.kite9.diagram.model.style.DiagramElementType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSCharsetRule;

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
public class TextLeafPainter extends DirectSVGGroupPainter implements LeafPainter {
	
	public TextLeafPainter(StyledKite9XMLElement theElement, Kite9BridgeContext ctx) {
		super(theElement, ctx.getXMLProcessor());
		this.ctx = ctx;
		this.theElement = theElement;
	}
	
	private Kite9BridgeContext ctx;
	private Rectangle2D bounds;
	private GraphicsNode gn;
	private StyledKite9XMLElement theElement;
	
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
	@Override
	protected void setupElementXML(StyledKite9XMLElement e) {
		if (bounds == null) {
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
			
			// render the flow root
			gn = LocalRenderingFlowRootElementBridge.getFlowNode(initGraphicsNode(flowRoot, ctx));
			bounds = gn.getBounds();
			
			// replace rendered flow root with regular svg text.
			Element group = graphicsNodeToXML(d, gn);
			e.removeChild(flowRoot);
			
			if (group != null) {
				e.appendChild(group);
			}
		}
		
	}	
	
	private Element graphicsNodeToXML(Document d, GraphicsNode node) {
		Element groupElem = d.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		ExtendedSVGGeneratorContext genCtx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(d);
		ExtendedSVGGraphics2D g2d = new ExtendedSVGGraphics2D(genCtx, groupElem);
		node.paint(g2d);
		groupElem = g2d.getTopLevelGroup(true);
		return (Element) groupElem.getFirstChild();
	}


	private SVGOMFlowRootElement createFlowRootElement(Document d, StyledKite9XMLElement theElement) {
		SVGOMFlowRootElement flowRoot = (SVGOMFlowRootElement) d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12Constants.SVG_FLOW_ROOT_TAG);
		setupFlowRegions(d, theElement, flowRoot);
		setupFlowDiv(d, theElement, flowRoot);
		return flowRoot;
	}

	public void setupFlowDiv(Document d, StyledKite9XMLElement theElement, SVGOMFlowRootElement in) {
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
	protected void setupFlowRegions(Document d, StyledKite9XMLElement theElement, SVGOMFlowRootElement in) {
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
	
	@Override
	public Rectangle2D bounds() {
		setupElementXML(theElement);
		return bounds;
	}

	public static GraphicsNode initGraphicsNode(Element e, Kite9BridgeContext ctx) {
		GVTBuilder builder = ctx.getGVTBuilder();
		CompositeGraphicsNode out = (CompositeGraphicsNode) builder.build(ctx, e);
		return out;
	}
}
