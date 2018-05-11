package org.kite9.diagram.batik.bridge;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GenericBridge;
import org.kite9.diagram.batik.BatikArrangementPipeline;
import org.kite9.diagram.batik.BatikDisplayer;
import org.kite9.framework.dom.XMLHelper;
import org.kite9.framework.dom.elements.DiagramKite9XMLElement;
import org.w3c.dom.Element;

/**
 * Handles top level <diagram> element from Kite9.
 * 
 * @author robmoffat
 *
 */
public class Kite9DiagramBridge implements GenericBridge {
	
	public Kite9DiagramBridge() {
		super();
	}

	private BatikArrangementPipeline createPipeline() {
		return new BatikArrangementPipeline(new BatikDisplayer(false, 20));
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

	@Override
	public Bridge getInstance() {
		return new Kite9DiagramBridge();
	}

	@Override
	public void handleElement(BridgeContext ctx, Element e) {
		DiagramKite9XMLElement d = (DiagramKite9XMLElement) e;
       	
       	// work out the positions of all the elements in te diagram, 
       	BatikArrangementPipeline pipeline = createPipeline();
		pipeline.arrange(d);

       	// used in testing nowhere else
       	lastDiagram = d;
        lastPipeline = pipeline;
	}
	

    
}
