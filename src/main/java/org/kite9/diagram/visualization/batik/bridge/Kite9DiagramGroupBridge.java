package org.kite9.diagram.visualization.batik.bridge;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGPathElementBridge;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.visualization.batik.node.GraphicsNodeLookup;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.serialization.CSSConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles all {@link XMLElement}s that turn into composite 'g' elements in SVG.
 * 
 * @author robmoffat
 *
 */
public class Kite9DiagramGroupBridge extends AbstractKite9GraphicsNodeBridge {
	
	public Kite9DiagramGroupBridge(GraphicsNodeLookup lookup) {
		super(lookup);
	}

	/**
	 * We are overriding this because we are going to control our 
	 * own compositing approach based on graphics layers.
	 */
	@Override
	public boolean isComposite() {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void processChildren(Element e, CompositeGraphicsNode into, BridgeContext ctx) {
		GVTBuilder builder = ctx.getGVTBuilder();
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element ref = (Element)n;
                GraphicsNode refNode = builder.build(ctx, ref);
                into.getChildren().add(refNode);
            }
        }
	}
	
	/**
     * Creates the `g` for the diagram, plus layers that it contains.
     */
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
    	CompositeGraphicsNode out = (CompositeGraphicsNode) super.createGraphicsNode(ctx, e);
    	
    	// does it have a path?
//    	if (e instanceof StyledKite9SVGElement) {
//    		//Value v = ((StyledKite9SVGElement) e).getCSSStyleProperty(CSSConstants.PATH);
//    		
//    		GraphicsNode node = new SVGPathElementBridge().createGraphicsNode(ctx, e) {
//    			
//    			
//    			
//    			
//    			
//    		}
//    		
//    		
//    	}
    	
    	processChildren(e, out, ctx);
        return out;
    }
	

    
}
