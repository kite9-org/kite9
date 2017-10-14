package org.kite9.diagram.batik.format;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.element.Templater;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Wraps BridgeContext methods to give element-node mappings.
 */
public interface ElementNodeMapper {

    public Element getElement(GraphicsNode gn);
    
    public GraphicsNode getGraphicsNode(Node node);
    
    public Templater getTemplater();
}
