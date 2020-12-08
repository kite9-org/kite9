package org.kite9.diagram.dom.elements;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.common.elements.factory.DiagramElementFactory;

public interface XMLDiagramElementFactory extends DiagramElementFactory<Kite9XMLElement> {

    public void setBridgeContext(Kite9BridgeContext c);
}