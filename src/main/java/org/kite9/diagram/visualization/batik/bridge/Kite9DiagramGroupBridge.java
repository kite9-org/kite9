package org.kite9.diagram.visualization.batik.bridge;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.visualization.batik.BatikArrangementPipeline;
import org.kite9.diagram.visualization.batik.BatikDisplayer;
import org.kite9.diagram.visualization.batik.element.DiagramImpl;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.visualization.pipeline.full.ArrangementPipeline;
import org.kite9.diagram.xml.DiagramXMLElement;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.Element;

/**
 * Handles top level <diagram> element from Kite9.
 * 
 * @author robmoffat
 *
 */
public class Kite9DiagramGroupBridge extends Kite9GBridge {
	
	public Kite9DiagramGroupBridge(Kite9BridgeContext kite9BridgeContext) {
		super();
	}

	private ArrangementPipeline createPipeline() {
		return new BatikArrangementPipeline(new BatikDisplayer(false, 20));
	}
	
	/**
     * Creates the `g` for the diagram, plus layers that it contains.
     */
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
    	DiagramXMLElement d = (DiagramXMLElement) e;
    	DiagramImpl de = (DiagramImpl) d.getDiagramElement();
       	IdentifiableGraphicsNode out = (IdentifiableGraphicsNode) super.createGraphicsNode(ctx, e);
       	out.setId(de.getID());
        createPipeline().arrange(d);

        	//for (GraphicsLayerName layer : GraphicsLayerName.values()) {
        		addLayer(out, GraphicsLayerName.MAIN, de);
    		//}
        	
       	processChildren(e, out, ctx);
        return out;
    }

	private void addLayer(CompositeGraphicsNode out, GraphicsLayerName l, Diagram de) {
		out.add(de.getGraphicsForLayer(l));
	}

	@Override
	public String getLocalName() {
		return XMLHelper.DIAGRAM_ELEMENT;
	}

	@Override
	public String getNamespaceURI() {
		return XMLHelper.KITE9_NAMESPACE;
	}
	

    
}
