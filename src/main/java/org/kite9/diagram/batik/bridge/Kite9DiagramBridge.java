package org.kite9.diagram.batik.bridge;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.BatikArrangementPipeline;
import org.kite9.diagram.batik.BatikDisplayer;
import org.kite9.diagram.batik.HasGraphicsNode;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.model.Diagram;
import org.kite9.framework.dom.XMLHelper;
import org.kite9.framework.xml.DiagramKite9XMLElement;
import org.w3c.dom.Element;

/**
 * Handles top level <diagram> element from Kite9.
 * 
 * @author robmoffat
 *
 */
public class Kite9DiagramBridge extends Kite9GBridge {
	
	public Kite9DiagramBridge(Kite9BridgeContext kite9BridgeContext) {
		super();
	}

	private BatikArrangementPipeline createPipeline() {
		return new BatikArrangementPipeline(new BatikDisplayer(false, 20));
	}
	
	/**
	 * We are overriding this because we are going to control our 
	 * own compositing approach based Kite9 rendering.
	 */
	@Override
	public boolean isComposite() {
		return false;
	}
	
	/**
     * Creates the `g` for the diagram, plus layers that it contains.
     */
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
    	DiagramKite9XMLElement d = (DiagramKite9XMLElement) e;
    	Diagram de = d.getDiagramElement();
       	
       	// work out the positions of all the elements in te diagram, 
       	BatikArrangementPipeline pipeline = createPipeline();
		pipeline.arrange(d);

       	// used in testing nowhere else
       	lastDiagram = d;
        lastPipeline = pipeline;
       	
       	return ((HasGraphicsNode) de).getGraphicsNode();
    }
    
    public static BatikArrangementPipeline lastPipeline;
    public static DiagramKite9XMLElement lastDiagram;


	@Override
	public String getLocalName() {
		return XMLHelper.DIAGRAM_ELEMENT;
	}

	@Override
	public String getNamespaceURI() {
		return XMLHelper.KITE9_NAMESPACE;
	}
	

    
}
