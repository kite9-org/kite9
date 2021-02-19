package org.kite9.diagram.batik.bridge;

import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GenericBridge;
import org.kite9.diagram.visualization.pipeline.BasicArrangementPipeline;
import org.kite9.diagram.batik.BatikDisplayer;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.elements.XMLDiagramElementFactory;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimatedLength;

/**
 * Handles top level <diagram> element from Kite9.
 * 
 * @author robmoffat
 *
 */
public class Kite9DiagramBridge implements GenericBridge {

	private XMLDiagramElementFactory factory;

	public Kite9DiagramBridge(XMLDiagramElementFactory factory) {
		super();
		this.factory = factory;
	}

	private BasicArrangementPipeline createPipeline() {
		return new BasicArrangementPipeline(factory, new BatikDisplayer(false));
	}
    
    public static BasicArrangementPipeline lastPipeline;
    public static Kite9XMLElement lastDiagram;


	@Override
	public String getLocalName() {
		return XMLHelper.DIAGRAM_ELEMENT;
	}

	@Override
	public String getNamespaceURI() {
		return XMLHelper.KITE9_NAMESPACE;
	}

	@Override
	public Bridge getInstance() {
		return new Kite9DiagramBridge(factory);
	}

	@Override
	public void handleElement(BridgeContext ctx, Element e) {
		Kite9XMLElement d = (Kite9XMLElement) e;
       	
       	// work out the positions of all the elements in te diagram, 
       	BasicArrangementPipeline pipeline = createPipeline();
       	
       	DiagramElement de = d.getDiagramElement();
       	
       	if (de instanceof Diagram) {
       		pipeline.arrange((Diagram) de);
           	// used in testing nowhere else
           	lastDiagram = d;
            lastPipeline = pipeline;
            
            ensureSvgSize((SVGOMSVGElement) e.getOwnerDocument().getDocumentElement(), 
            		((Diagram) de).getRenderingInformation());
       	} else {
       		throw new Kite9XMLProcessingException("Outermost element-type of kite9 element must be a diagram "+e.getTagName()+ " is a "+de.getClass(), d);
       	}
	}

	private void ensureSvgSize(SVGOMSVGElement svg, RectangleRenderingInformation ri) {
		SVGAnimatedLength width = svg.getWidth();
		SVGAnimatedLength height = svg.getHeight();
		width.getBaseVal().setValueAsString(ri.getSize().getW()+"px");
		height.getBaseVal().setValueAsString(ri.getSize().getH()+"px");
	}
	

    
}
