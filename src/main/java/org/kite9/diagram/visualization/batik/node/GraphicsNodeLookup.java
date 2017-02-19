package org.kite9.diagram.visualization.batik.node;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.w3c.dom.Element;

public interface GraphicsNodeLookup {

	GraphicsNode getNode(GraphicsLayerName gl, Element element);
	
	void storeNode(GraphicsLayerName gl, Element element, GraphicsNode node);
}
