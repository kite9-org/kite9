package org.kite9.diagram.visualization.batik;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.XMLElement;

public interface GraphicsNodeLookup {

	GraphicsNode getNode(GraphicsLayerName gl, XMLElement element);
	
	void storeNode(GraphicsLayerName gl, XMLElement element, GraphicsNode node);
}
